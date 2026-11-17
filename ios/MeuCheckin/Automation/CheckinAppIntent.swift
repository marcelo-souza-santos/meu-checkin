import AppIntents
import Foundation

/// Ação nativa do iOS para integração com o aplicativo Atalhos (Shortcuts da Apple).
/// Permite criar Automações Pessoais no iPhone ("Todos os dias às 09:10 -> Fazer Check-in").
@available(iOS 16.0, *)
public struct RealizarCheckinIntent: AppIntent {
    public static var title: LocalizedStringResource = "Fazer Check-in no Treino"
    public static var description = IntentDescription("Verifica os treinos da academia e realiza o check-in automático.")

    public static var openAppWhenRun: Bool = false

    public init() {}

    public func perform() async throws -> some IntentResult & ProvidesDialog {
        let (sucesso, mensagem) = await CheckinManager.shared.tentarCheckin(forcarSeHouverTreino: false)
        return .result(dialog: IntentDialog(stringLiteral: mensagem))
    }
}

/// Fornece o atalho pré-configurado no sistema
@available(iOS 16.0, *)
public struct MeuCheckinShortcuts: AppShortcutsProvider {
    public static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: RealizarCheckinIntent(),
            phrases: [
                "Fazer check-in no \(.applicationName)",
                "Confirmar treino no \(.applicationName)",
                "Check-in Jiu-Jitsu"
            ],
            shortTitle: "Check-in Jiu-Jitsu",
            systemImageName: "figure.martial.arts"
        )
    }
}

