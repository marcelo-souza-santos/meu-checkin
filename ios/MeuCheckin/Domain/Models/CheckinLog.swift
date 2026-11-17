import Foundation

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
