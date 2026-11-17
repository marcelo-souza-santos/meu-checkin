import Foundation

public enum CheckinElegibilidade {
    case cancelado(motivo: String)
    case jaRealizado(mensagem: String)
    case semAlarme(motivo: String)
    case janelaFechada(motivo: String)
    case aguardandoHorario(minutosRestantes: Int)
    case prontoParaCheckin(scheduleId: Int, dataTreino: String, desc: String)
}

public final class CheckinManager {
    public static let shared = CheckinManager()

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

    /// Extrai todos os slots disponíveis no painel da API (tanto em main.slot quanto em upcoming).
    public static func extrairTodosSlots(painel: [String: Any]) -> [[String: Any]] {
        var lista: [[String: Any]] = []

        if let main = painel["main"] as? [String: Any],
           let slot = main["slot"] as? [String: Any] {
            lista.append(slot)
        }

        if let upcoming = painel["upcoming"] as? [[String: Any]] {
            for s in upcoming {
                let id = (s["schedule_id"] as? Int) ?? 0
                let dt = (s["data_treino"] as? String) ?? ""
                let jaExiste = lista.contains {
                    (($0["schedule_id"] as? Int) == id) && (($0["data_treino"] as? String) == dt)
                }
                if !jaExiste {
                    lista.append(s)
                }
            }
        }
        return lista
    }

    /// Avalia a elegibilidade de um slot de treino contra os alarmes do usuário (lógica pura).
    public static func avaliarElegibilidade(
        slot: [String: Any],
        alarmesAtivos: [TrainingAlarm],
        forcarSeHouverTreino: Bool = false,
        agora: Date = Date()
    ) -> CheckinElegibilidade {
        let scheduleId = (slot["schedule_id"] as? Int) ?? 0
        let dataTreino = (slot["data_treino"] as? String) ?? ""
        let diaSemana = (slot["dia_semana"] as? Int) ?? 1
        let horarioInicio = (slot["horario_inicio"] as? String) ?? ""
        let tipoTreino = (slot["tipo_treino"] as? String) ?? "Gi"
        let canCheckin = (slot["can_checkin"] as? Bool) ?? false
        let statusSlot = (slot["status"] as? String) ?? ""
        let cancelado = (slot["cancelled"] as? Bool) ?? false
        let jaFeito = (slot["checkin"] != nil && !(slot["checkin"] is NSNull)) || statusSlot == "realizado"

        let desc = "\(tipoTreino) \(horarioInicio) (\(dataTreino))"

        if cancelado {
            return .cancelado(motivo: "O treino \(desc) foi cancelado pela academia.")
        }

        if jaFeito {
            return .jaRealizado(mensagem: "Check-in já foi realizado para \(desc)!")
        }

        let alarmeCorrespondente = alarmesAtivos.first { alarme in
            alarme.diasSemana.contains(diaSemana) && (alarme.horarioTreino == horarioInicio || forcarSeHouverTreino)
        }

        if alarmeCorrespondente == nil && !forcarSeHouverTreino {
            return .semAlarme(motivo: "Nenhum alarme ativo configurado para hoje às \(horarioInicio).")
        }

        let offsetMin = alarmeCorrespondente?.offsetMinutos ?? 10

        if !forcarSeHouverTreino {
            if let horarioAlvo = parseHorarioAlvo(dataTreino: dataTreino, horarioInicio: horarioInicio, offsetMinutos: offsetMin) {
                let diff = horarioAlvo.timeIntervalSince(agora)
                if diff > 60 { // Mais de 1 minuto restante
                    let minutosRestantes = Int(diff / 60)
                    return .aguardandoHorario(minutosRestantes: minutosRestantes)
                }
            }
        }

        if !canCheckin {
            return .janelaFechada(motivo: "A janela de check-in não está aberta na API no momento.")
        }

        return .prontoParaCheckin(scheduleId: scheduleId, dataTreino: dataTreino, desc: desc)
    }

    /// Executa a tentativa completa de check-in
    public func tentarCheckin(forcarSeHouverTreino: Bool = false) async -> (sucesso: Bool, mensagem: String) {
        guard api.hasSavedCredentials else {
            return (false, "Nenhuma credencial salva no aparelho.")
        }

        let painel: [String: Any]
        do {
            painel = try await api.get(path: "/api/checkins-treino")
        } catch {
            return (false, "Erro de conexão com a API: \(error.localizedDescription)")
        }

        let todosSlots = CheckinManager.extrairTodosSlots(painel: painel)
        guard !todosSlots.isEmpty else {
            return (false, "Nenhum treino disponível no momento na plataforma.")
        }

        let alarmesAtivos = alarmStore.getAlarms().filter { $0.enabled }
        if alarmesAtivos.isEmpty && !forcarSeHouverTreino {
            return (false, "Nenhum alarme de treino está ativado no aplicativo.")
        }

        var melhorResultado: CheckinElegibilidade? = nil
        for slot in todosSlots {
            let res = CheckinManager.avaliarElegibilidade(slot: slot, alarmesAtivos: alarmesAtivos, forcarSeHouverTreino: forcarSeHouverTreino)
            if case .prontoParaCheckin = res {
                melhorResultado = res
                break
            }
            if melhorResultado == nil {
                melhorResultado = res
            } else if case .jaRealizado = res {
                melhorResultado = res
            }
        }

        guard let resultado = melhorResultado else {
            return (false, "Nenhum treino compatível encontrado.")
        }

        switch resultado {
        case .cancelado(let motivo):
            return (false, motivo)
        case .jaRealizado(let msg):
            return (true, msg)
        case .semAlarme(let motivo):
            return (false, motivo)
        case .janelaFechada(let motivo):
            return (false, motivo)
        case .aguardandoHorario(let min):
            return (false, "Aguardando horário estimado de check-in (~\(min) min restantes).")
        case .prontoParaCheckin(let scheduleId, let dataTreino, let desc):
            let body: [String: Any] = [
                "schedule_id": scheduleId,
                "data_treino": dataTreino
            ]

            do {
                let (code, respBody) = try await api.postAuthenticated(path: "/api/checkins-treino", body: body)
                let confirmou = (200...299).contains(code) || CheckinManager.verificarConfirmacao(respBody: respBody, scheduleId: scheduleId)

                if confirmou {
                    logStore.addLog(scheduleId: scheduleId, treinoDesc: desc, sucesso: true, mensagem: "Check-in automático realizado com sucesso!")
                    NotificationHelper.shared.notify(titulo: "Check-in Confirmado! 🥋", mensagem: desc, sucesso: true)
                    return (true, "Check-in realizado com sucesso para \(desc)!")
                } else {
                    let erroMsg = "API recusou com HTTP \(code)"
                    logStore.addLog(scheduleId: scheduleId, treinoDesc: desc, sucesso: false, mensagem: erroMsg)
                    NotificationHelper.shared.notify(titulo: "Falha no Check-in ⚠️", mensagem: erroMsg, sucesso: false)
                    return (false, erroMsg)
                }
            } catch {
                let erroMsg = "Falha ao enviar check-in: \(error.localizedDescription)"
                logStore.addLog(scheduleId: scheduleId, treinoDesc: desc, sucesso: false, mensagem: erroMsg)
                NotificationHelper.shared.notify(titulo: "Falha no Check-in ⚠️", mensagem: erroMsg, sucesso: false)
                return (false, erroMsg)
            }
        }
    }

    public static func verificarConfirmacao(respBody: [String: Any], scheduleId: Int) -> Bool {
        if let history = respBody["history"] as? [[String: Any]] {
            for h in history {
                if (h["schedule_id"] as? Int) == scheduleId && (h["status"] as? String) == "confirmado" {
                    return true
                }
            }
        }
        if let upcoming = respBody["upcoming"] as? [[String: Any]] {
            for u in upcoming {
                if (u["schedule_id"] as? Int) == scheduleId {
                    if (u["checkin"] != nil && !(u["checkin"] is NSNull)) || (u["status"] as? String) == "realizado" {
                        return true
                    }
                }
            }
        }
        return false
    }

    public static func parseHorarioAlvo(dataTreino: String, horarioInicio: String, offsetMinutos: Int) -> Date? {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm"
        guard let date = formatter.date(from: "\(dataTreino) \(horarioInicio)") else { return nil }
        return Calendar.current.date(byAdding: .minute, value: offsetMinutos, to: date)
    }
}

