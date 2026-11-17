import Foundation
import UserNotifications

public final class NotificationHelper {
    public static let shared = NotificationHelper()

    private init() {}

    public func requestAuthorization() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
    }

    public func notify(titulo: String, mensagem: String, sucesso: Bool) {
        let content = UNMutableNotificationContent()
        content.title = titulo
        content.body = mensagem
        content.sound = .default

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: trigger)

        UNUserNotificationCenter.current().add(request, withCompletionHandler: nil)
    }
}

