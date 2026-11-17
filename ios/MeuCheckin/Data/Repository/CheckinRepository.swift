import Foundation

public protocol CheckinRepository {
    var hasSavedCredentials: Bool { get }
    func login(email: String, pass: String) async throws
    func logout()
    func getAlunoPerfil() async -> AlunoPerfil?
    func getTreinosDisponiveis() async -> [TreinoOpcao]
    func getAlarms() -> [TrainingAlarm]
    func saveAlarm(_ alarm: TrainingAlarm)
    func deleteAlarm(id: UUID)
    func toggleAlarm(id: UUID, enabled: Bool)
    func getLogs() -> [CheckinLog]
}

public final class CheckinRepositoryImpl: CheckinRepository {
    public static let shared = CheckinRepositoryImpl()

    private let api: ApiClient
    private let alarmStore: TrainingAlarmStore
    private let logStore: CheckinLogStore

    public init(
        api: ApiClient = .shared,
        alarmStore: TrainingAlarmStore = .shared,
        logStore: CheckinLogStore = .shared
    ) {
        self.api = api
        self.alarmStore = alarmStore
        self.logStore = logStore
    }

    public var hasSavedCredentials: Bool {
        api.hasSavedCredentials
    }

    public func login(email: String, pass: String) async throws {
        try await api.login(email: email, pass: pass)
    }

    public func logout() {
        api.logout()
    }

    public func getAlunoPerfil() async -> AlunoPerfil? {
        do {
            let grad = try await api.get(path: "/api/graduacoes/me")
            if let alunoObj = grad["aluno"] as? [String: Any] {
                let nome = (alunoObj["nome_completo"] as? String) ?? ""
                let faixa = (alunoObj["cor_faixa"] as? String) ?? "branca"
                let graus = (alunoObj["qntd_graus"] as? Int) ?? 0
                let prox = (alunoObj["graduacao_status"] as? [String: Any])?["proxima_faixa"] as? String
                return AlunoPerfil(nome: nome, faixa: faixa, graus: graus, proximaFaixa: prox)
            } else {
                let me = try await api.get(path: "/api/auth/me?photoMode=metadata")
                return AlunoPerfil(
                    nome: (me["full_name"] as? String) ?? "",
                    faixa: (me["cor_faixa"] as? String) ?? "branca",
                    graus: (me["qntd_graus"] as? Int) ?? 0,
                    proximaFaixa: nil
                )
            }
        } catch {
            return nil
        }
    }

    public func getTreinosDisponiveis() async -> [TreinoOpcao] {
        do {
            let painel = try await api.get(path: "/api/checkins-treino")
            var lista: [TreinoOpcao] = []

            func extrair(_ obj: [String: Any]?) {
                guard let obj = obj else { return }
                let sched = (obj["schedule_id"] as? Int) ?? 0
                let tipo = (obj["tipo_treino"] as? String) ?? "Gi"
                let ini = (obj["horario_inicio"] as? String) ?? ""
                let fim = (obj["horario_fim"] as? String) ?? ""
                let dia = (obj["dia_semana"] as? Int) ?? 1
                if !ini.trimmingCharacters(in: .whitespaces).isEmpty {
                    lista.append(TreinoOpcao(scheduleId: sched, tipoTreino: tipo, horarioInicio: ini, horarioFim: fim, diaSemana: dia))
                }
            }

            if let mainObj = painel["main"] as? [String: Any] {
                extrair(mainObj["slot"] as? [String: Any])
            }

            if let upcoming = painel["upcoming"] as? [[String: Any]] {
                for item in upcoming { extrair(item) }
            }

            var unicos: [TreinoOpcao] = []
            var chavesVistas = Set<String>()
            for t in lista {
                let chave = "\(t.tipoTreino)_\(t.horarioInicio)"
                if !chavesVistas.contains(chave) {
                    chavesVistas.insert(chave)
                    unicos.append(t)
                }
            }

            let sorted = unicos.sorted { $0.horarioInicio < $1.horarioInicio }
            return sorted.isEmpty ? fallbackTreinos() : sorted
        } catch {
            return fallbackTreinos()
        }
    }

    private func fallbackTreinos() -> [TreinoOpcao] {
        return [
            TreinoOpcao(scheduleId: 26, tipoTreino: "Gi", horarioInicio: "06:00", horarioFim: "07:30", diaSemana: 2),
            TreinoOpcao(scheduleId: 2, tipoTreino: "Gi", horarioInicio: "09:00", horarioFim: "10:30", diaSemana: 1),
            TreinoOpcao(scheduleId: 11, tipoTreino: "Treino Extra", horarioInicio: "16:00", horarioFim: "17:30", diaSemana: 6),
            TreinoOpcao(scheduleId: 4, tipoTreino: "Gi", horarioInicio: "17:00", horarioFim: "18:30", diaSemana: 2),
            TreinoOpcao(scheduleId: 3, tipoTreino: "Gi", horarioInicio: "20:00", horarioFim: "21:30", diaSemana: 1),
            TreinoOpcao(scheduleId: 1, tipoTreino: "No-Gi", horarioInicio: "20:00", horarioFim: "21:30", diaSemana: 3)
        ]
    }

    public func getAlarms() -> [TrainingAlarm] { alarmStore.getAlarms() }
    public func saveAlarm(_ alarm: TrainingAlarm) { alarmStore.saveAlarm(alarm) }
    public func deleteAlarm(id: UUID) { alarmStore.deleteAlarm(id: id) }
    public func toggleAlarm(id: UUID, enabled: Bool) { alarmStore.toggleAlarm(id: id, enabled: enabled) }
    public func getLogs() -> [CheckinLog] { logStore.getLogs() }
}

