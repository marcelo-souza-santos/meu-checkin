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



