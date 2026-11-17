import Foundation

/// Modelo de Alarme de Treino (estilo despertador nativo do celular):
/// - Horário do treino (ex: "09:00", "20:00")
/// - Dias da semana em que repete (1=Segunda, ..., 7=Domingo)
/// - Minutos após o início para efetuar o check-in (ex: 10 min)
/// - Switch de ativado/desativado
/// - Variação randômica humana (jitter)
public struct TrainingAlarm: Identifiable, Codable, Equatable {
    public var id: UUID
    public var titulo: String
    public var horarioTreino: String
    public var diasSemana: Set<Int> // 1=Segunda, 2=Terça ... 7=Domingo
    public var offsetMinutos: Int
    public var variacaoRandomica: Bool
    public var enabled: Bool

    public init(
        id: UUID = UUID(),
        titulo: String = "Treino",
        horarioTreino: String = "09:00",
        diasSemana: Set<Int> = [1, 3, 5],
        offsetMinutos: Int = 10,
        variacaoRandomica: Bool = true,
        enabled: Bool = true
    ) {
        self.id = id
        self.titulo = titulo
        self.horarioTreino = horarioTreino
        self.diasSemana = diasSemana
        self.offsetMinutos = offsetMinutos
        self.variacaoRandomica = variacaoRandomica
        self.enabled = enabled
    }
}

