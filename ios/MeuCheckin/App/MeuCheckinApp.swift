import SwiftUI

@main
struct MeuCheckinApp: App {
    @StateObject private var viewModel = MainViewModel()

    init() {
        NotificationHelper.shared.requestAuthorization()
    }

    var body: some Scene {
        WindowGroup {
            Group {
                switch viewModel.state {
                case .loading:
                    LoadingView()
                case .login:
                    LoginView(viewModel: viewModel)
                case .alarms:
                    ClockAlarmsView(viewModel: viewModel)
                }
            }
            .preferredColorScheme(.dark)
        }
    }
}

