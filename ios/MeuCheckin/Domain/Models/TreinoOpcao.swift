import Foundation

/// Opção de treino disponível da academia
public struct TreinoOpcao: Identifiable, Codable, Equatable {
    public var id: Int { scheduleId }
    public let scheduleId: Int
    public let tipoTreino: String
    public let horarioInicio: String
    public let horarioFim: String
    public let diaSemana: Int // 1=Segunda ... 7=Domingo

    public init(scheduleId: Int, tipoTreino: String, horarioInicio: String, horarioFim: String, diaSemana: Int) {
        self.scheduleId = scheduleId
        self.tipoTreino = tipoTreino
        self.horarioInicio = horarioInicio
        self.horarioFim = horarioFim
        self.diaSemana = diaSemana
    }
}

/// Registro histórico de execução de check-in
public struct CheckinLog: Identifiable, Codable {
    public var id: String { "\(timestamp)_\(scheduleId)" }
    public let timestamp: Int64
    public let dataHoraStr: String
    public let scheduleId: Int
    public let treinoDesc: String
    public let sucesso: Bool
    public let mensagem: String

    public init(timestamp: Int64, dataHoraStr: String, scheduleId: Int, treinoDesc: String, sucesso: Bool, mensagem: String) {
        self.timestamp = timestamp
        self.dataHoraStr = dataHoraStr
        self.scheduleId = scheduleId
        self.treinoDesc = treinoDesc
        self.sucesso = sucesso
        self.mensagem = mensagem
    }
}

