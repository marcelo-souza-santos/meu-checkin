import Foundation
import Security

/// Armazenamento seguro de credenciais e tokens no iOS Keychain (equivalente ao Keystore do Android)
public final class KeychainStore {
    public static let shared = KeychainStore()
    private let service = "br.com.meucheckin.session"

    private init() {}

    public var email: String? {
        get { read(key: "email") }
        set { save(key: "email", value: newValue) }
    }

    public var password: String? {
        get { read(key: "password") }
        set { save(key: "password", value: newValue) }
    }

    public var accessToken: String? {
        get { read(key: "access_token") }
        set { save(key: "access_token", value: newValue) }
    }

    public var refreshToken: String? {
        get { read(key: "refresh_token") }
        set { save(key: "refresh_token", value: newValue) }
    }

    public var hasSavedCredentials: Bool {
        guard let e = email, !e.trimmingCharacters(in: .whitespaces).isEmpty,
              let p = password, !p.trimmingCharacters(in: .whitespaces).isEmpty else {
            return false
        }
        return true
    }

    public func clear() {
        delete(key: "email")
        delete(key: "password")
        delete(key: "access_token")
        delete(key: "refresh_token")
    }

    // MARK: - Keychain Helpers

    private func save(key: String, value: String?) {
        guard let value = value, let data = value.data(using: .utf8) else {
            delete(key: key)
            return
        }

        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key
        ]

        SecItemDelete(query as CFDictionary)

        var newAttributes = query
        newAttributes[kSecValueData as String] = data
        newAttributes[kSecAttrAccessible as String] = kSecAttrAccessibleAfterFirstUnlock

        SecItemAdd(newAttributes as CFDictionary, nil)
    }

    private func read(key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]

        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)
        guard status == errSecSuccess, let data = item as? Data else {
            return nil
        }
        return String(data: data, encoding: .utf8)
    }

    private func delete(key: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key
        ]
        SecItemDelete(query as CFDictionary)
    }
}

