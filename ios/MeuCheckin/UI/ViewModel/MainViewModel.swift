import Foundation
import SwiftUI

@MainActor
public final class MainViewModel: ObservableObject {
    @Published public var state: UiState = .loading
    @Published public var error: String? = nil
    @Published public var alarms: [TrainingAlarm] = []
    @Published public var perfilAluno: AlunoPerfil? = nil
    @Published public var availableTreinos: [TreinoOpcao] = []
    @Published public var isExecutingAction: Bool = false
    @Published public var actionFeedback: String? = nil

    private let repository: CheckinRepository
    private let checkinManager: CheckinManager

    public init(
        repository: CheckinRepository = CheckinRepositoryImpl.shared,
        checkinManager: CheckinManager = CheckinManager.shared
    ) {
        self.repository = repository
        self.checkinManager = checkinManager
        self.alarms = repository.getAlarms()

        if repository.hasSavedCredentials {
            self.state = .loading
            Task { await verificarSessao() }
        } else {
            self.state = .login
        }
    }

    public func verificarSessao() async {
        state = .loading
        do {
            async let perfilTask = repository.getAlunoPerfil()
            async let treinosTask = repository.getTreinosDisponiveis()

            let (perfil, treinos) = await (perfilTask, treinosTask)
            self.perfilAluno = perfil
            self.availableTreinos = treinos
            self.alarms = repository.getAlarms()
            self.state = .alarms
        } catch {
            self.error = error.localizedDescription
            self.state = repository.hasSavedCredentials ? .alarms : .login
        }
    }

    public func login(email: String, pass: String) async {
        error = nil
        state = .loading
        do {
            try await repository.login(email: email, pass: pass)
            await verificarSessao()
        } catch {
            self.error = error.localizedDescription
            self.state = .login
        }
    }

    public func toggleAlarm(id: UUID, enabled: Bool) {
        repository.toggleAlarm(id: id, enabled: enabled)
        alarms = repository.getAlarms()
    }

    public func saveAlarm(_ alarm: TrainingAlarm) {
        repository.saveAlarm(alarm)
        alarms = repository.getAlarms()
    }

    public func deleteAlarm(id: UUID) {
        repository.deleteAlarm(id: id)
        alarms = repository.getAlarms()
    }

    public func testarCheckinAgora() async {
        isExecutingAction = true
        actionFeedback = nil

        let (ok, msg) = await checkinManager.tentarCheckin(forcarSeHouverTreino: true)
        isExecutingAction = false
        actionFeedback = ok ? "✅ \(msg)" : "⚠️ \(msg)"
    }

    public func logout() {
        repository.logout()
        state = .login
    }
}

