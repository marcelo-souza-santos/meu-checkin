import SwiftUI

public struct LoadingView: View {
    public var body: some View {
        ZStack {
            Color.clockDarkBackground.ignoresSafeArea()

            VStack(spacing: 16) {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .clockPillBlue))
                    .scaleEffect(1.6)

                Text("Carregando...")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.clockTextLight)

                Text("Conectando à plataforma de treinos")
                    .font(.system(size: 13))
                    .foregroundColor(.clockTextDim)
            }
        }
    }
}

