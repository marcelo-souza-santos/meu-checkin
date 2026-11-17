import Foundation

public final class CheckinLogStore {
    public static let shared = CheckinLogStore()
    private let key = "checkin_logs"
    private let defaults = UserDefaults.standard

    public init() {}

    public func getLogs() -> [CheckinLog] {
        guard let data = defaults.data(forKey: key),
              let list = try? JSONDecoder().decode([CheckinLog].self, from: data) else {
            return []
        }
        return list
    }

    public func addLog(scheduleId: Int, treinoDesc: String, sucesso: Bool, mensagem: String) {
        let now = Date()
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy HH:mm"
        let dataHoraStr = formatter.string(from: now)
        let timestamp = Int64(now.timeIntervalSince1970 * 1000)

        let newLog = CheckinLog(
            timestamp: timestamp,
            dataHoraStr: dataHoraStr,
            scheduleId: scheduleId,
            treinoDesc: treinoDesc,
            sucesso: sucesso,
            mensagem: mensagem
        )

        var logs = getLogs()
        logs.insert(newLog, at: 0)
        let trimmed = Array(logs.prefix(50)) // Mantém os últimos 50 eventos

        if let data = try? JSONEncoder().encode(trimmed) {
            defaults.set(data, forKey: key)
        }
    }
}

