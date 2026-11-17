import SwiftUI

public struct LoginView: View {
    @ObservedObject public var viewModel: MainViewModel
    @State private var email: String = ""
    @State private var pass: String = ""

    public var body: some View {
        ZStack {
            Color.clockDarkBackground.ignoresSafeArea()

            VStack(spacing: 24) {
                Spacer()

                VStack(spacing: 6) {
                    Text("Alarmes")
                        .font(.system(size: 32, weight: .bold))
                        .foregroundColor(.clockTextLight)

                    Text("Check-in automático Jiu-Jitsu")
                        .font(.system(size: 15))
                        .foregroundColor(.clockTextDim)
                }

                VStack(spacing: 14) {
                    TextField("E-mail", text: $email)
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)
                        .padding(14)
                        .background(Color.clockCardOff)
                        .cornerRadius(12)
                        .foregroundColor(.clockTextLight)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.clockTextDim.opacity(0.3), lineWidth: 1)
                        )

                    SecureField("Senha", text: $pass)
                        .padding(14)
                        .background(Color.clockCardOff)
                        .cornerRadius(12)
                        .foregroundColor(.clockTextLight)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.clockTextDim.opacity(0.3), lineWidth: 1)
                        )

                    if let error = viewModel.error {
                        Text(error)
                            .font(.system(size: 13))
                            .foregroundColor(.clockDeleteRed)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }
                .padding(.horizontal, 28)

                Button(action: {
                    Task { await viewModel.login(email: email, pass: pass) }
                }) {
                    Text("Entrar")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.clockPillOnBlue)
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .background(email.isEmpty || pass.isEmpty ? Color.clockPillBlue.opacity(0.5) : Color.clockPillBlue)
                        .cornerRadius(26)
                }
                .disabled(email.isEmpty || pass.isEmpty)
                .padding(.horizontal, 28)

                Text("Credenciais salvas de forma segura no Secure Enclave / Keychain do iPhone.")
                    .font(.system(size: 12))
                    .foregroundColor(.clockTextDim)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)

                Spacer()
            }
        }
    }
}

