import Foundation

public final class TrainingAlarmStore {
    public static let shared = TrainingAlarmStore()
    private let key = "alarm_list"
    private let defaults = UserDefaults.standard

    public init() {}

    public func getAlarms() -> [TrainingAlarm] {
        guard let data = defaults.data(forKey: key),
              let list = try? JSONDecoder().decode([TrainingAlarm].self, from: data) else {
            return [] // Inicia vazia, conforme aprovado
        }
        return list.sorted { $0.horarioTreino < $1.horarioTreino }
    }

    public func saveAlarms(_ alarms: [TrainingAlarm]) {
        if let data = try? JSONEncoder().encode(alarms) {
            defaults.set(data, forKey: key)
        }
    }

    public func saveAlarm(_ alarm: TrainingAlarm) {
        var current = getAlarms()
        if let idx = current.firstIndex(where: { $0.id == alarm.id }) {
            current[idx] = alarm
        } else {
            current.append(alarm)
        }
        saveAlarms(current)
    }

    public func deleteAlarm(id: UUID) {
        let current = getAlarms().filter { $0.id != id }
        saveAlarms(current)
    }

    public func toggleAlarm(id: UUID, enabled: Bool) {
        var current = getAlarms()
        if let idx = current.firstIndex(where: { $0.id == id }) {
            current[idx].enabled = enabled
            saveAlarms(current)
        }
    }
}

