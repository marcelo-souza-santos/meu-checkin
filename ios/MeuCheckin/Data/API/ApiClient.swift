import Foundation

public enum AuthError: LocalizedError {
    case invalidCredentials
    case sessionExpired
    case networkError(String)
    case serverError(Int)

    public var errorDescription: String? {
        switch self {
        case .invalidCredentials: return "E-mail ou senha inválidos"
        case .sessionExpired: return "Sessão expirada. Faça login novamente"
        case .networkError(let msg): return "Erro de conexão: \(msg)"
        case .serverError(let code): return "Erro no servidor (HTTP \(code))"
        }
    }
}

public final class ApiClient {
    public static let shared = ApiClient()

    private let baseUrl = "https://academia-jiu-jitsu-api.onrender.com"
    private let session: URLSession
    private let store: KeychainStore

    private let defaultUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148"
    private let defaultReferer = "https://ct-pablo-forlan-bjj.pages.dev/"
    private let defaultOrigin = "https://ct-pablo-forlan-bjj.pages.dev"

    public init(store: KeychainStore = .shared) {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 90
        config.timeoutIntervalForResource = 90
        self.session = URLSession(configuration: config)
        self.store = store
    }

    public var hasSavedCredentials: Bool {
        store.hasSavedCredentials
    }

    // MARK: - Login / Logout

    public func login(email: String, pass: String) async throws {
        let cleanEmail = email.trimmingCharacters(in: .whitespaces)
        let body: [String: Any] = ["email": cleanEmail, "password": pass]
        let (code, data) = try await rawPost(path: "/api/auth/login?photoMode=metadata", body: body, token: nil)

        if code == 200, let json = data as? [String: Any] {
            storeTokens(json)
            store.email = cleanEmail
            store.password = pass
        } else if [400, 401, 403, 422].contains(code) {
            throw AuthError.invalidCredentials
        } else {
            throw AuthError.serverError(code)
        }
    }

    public func logout() {
        store.clear()
    }

    // MARK: - Authenticated Requests

    public func get(path: String) async throws -> [String: Any] {
        var token = store.accessToken
        if token == nil || token!.isEmpty {
            token = try await renewToken()
        }

        var (code, data) = try await rawGet(path: path, token: token)
        if code == 401 {
            token = try await renewToken()
            (code, data) = try await rawGet(path: path, token: token)
        }

        if code == 401 { throw AuthError.sessionExpired }
        guard (200...299).contains(code), let dict = data as? [String: Any] else {
            throw AuthError.serverError(code)
        }
        return dict
    }

    public func postAuthenticated(path: String, body: [String: Any]) async throws -> (statusCode: Int, body: [String: Any]) {
        var token = store.accessToken
        if token == nil || token!.isEmpty {
            token = try await renewToken()
        }

        var (code, data) = try await rawPost(path: path, body: body, token: token)
        if code == 401 {
            token = try await renewToken()
            (code, data) = try await rawPost(path: path, body: body, token: token)
        }

        if code == 401 { throw AuthError.sessionExpired }
        let dict = (data as? [String: Any]) ?? [:]
        return (code, dict)
    }

    // MARK: - Token Refresh / Re-login

    private func renewToken() async throws -> String {
        // 1. Tenta renovar pelo refresh_token
        if let refresh = store.refreshToken, !refresh.isEmpty {
            let (code, data) = try await rawPost(
                path: "/api/auth/refresh?photoMode=metadata",
                body: ["refresh_token": refresh],
                token: nil
            )
            if code == 200, let json = data as? [String: Any] {
                storeTokens(json)
                if let newAccess = store.accessToken {
                    return newAccess
                }
            }
        }

        // 2. Se falhar, faz fallback silencioso usando e-mail e senha salvos
        if let email = store.email, let pass = store.password, !email.isEmpty, !pass.isEmpty {
            let (code, data) = try await rawPost(
                path: "/api/auth/login?photoMode=metadata",
                body: ["email": email, "password": pass],
                token: nil
            )
            if code == 200, let json = data as? [String: Any] {
                storeTokens(json)
                if let newAccess = store.accessToken {
                    return newAccess
                }
            }
        }

        store.accessToken = nil
        store.refreshToken = nil
        throw AuthError.sessionExpired
    }

    private func storeTokens(_ json: [String: Any]) {
        if let access = json["access_token"] as? String {
            store.accessToken = access
        }
        if let refresh = json["refresh_token"] as? String, !refresh.isEmpty {
            store.refreshToken = refresh
        }
    }

    // MARK: - Raw Networking

    private func makeRequest(path: String, method: String, token: String?) -> URLRequest {
        let url = URL(string: baseUrl + path)!
        var req = URLRequest(url: url)
        req.httpMethod = method
        req.setValue(defaultUserAgent, forHTTPHeaderField: "User-Agent")
        req.setValue(defaultReferer, forHTTPHeaderField: "Referer")
        req.setValue(defaultOrigin, forHTTPHeaderField: "Origin")
        req.setValue("application/json, text/plain, */*", forHTTPHeaderField: "Accept")
        req.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")

        if let token = token, !token.isEmpty {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        return req
    }

    private func rawGet(path: String, token: String?) async throws -> (Int, Any?) {
        let req = makeRequest(path: path, method: "GET", token: token)
        let (data, response) = try await session.data(for: req)
        let status = (response as? HTTPURLResponse)?.statusCode ?? 0
        let json = try? JSONSerialization.jsonObject(with: data)
        return (status, json)
    }

    private func rawPost(path: String, body: [String: Any], token: String?) async throws -> (Int, Any?) {
        var req = makeRequest(path: path, method: "POST", token: token)
        req.httpBody = try? JSONSerialization.data(withJSONObject: body)
        let (data, response) = try await session.data(for: req)
        let status = (response as? HTTPURLResponse)?.statusCode ?? 0
        let json = try? JSONSerialization.jsonObject(with: data)
        return (status, json)
    }
}

