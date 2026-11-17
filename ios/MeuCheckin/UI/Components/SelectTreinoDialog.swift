import SwiftUI

public struct SelectTreinoDialog: View {
    public let treinos: [TreinoOpcao]
    public let onSelect: (TreinoOpcao) -> Void
    public let onDismiss: () -> Void

    private let diasNomes: [Int: String] = [
        1: "Segunda", 2: "Terça", 3: "Quarta", 4: "Quinta",
        5: "Sexta", 6: "Sábado", 7: "Domingo"
    ]

    public var body: some View {
        ZStack {
            Color.clockDarkBackground.ignoresSafeArea()

            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    Text("Escolha o Treino da Academia")
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundColor(.clockTextLight)
                    Spacer()
                    Button("Cancelar", action: onDismiss)
                        .foregroundColor(.clockTextDim)
                }
                .padding(.top, 20)
                .padding(.horizontal, 20)

                ScrollView {
                    VStack(spacing: 10) {
                        ForEach(treinos) { t in
                            Button(action: { onSelect(t) }) {
                                HStack {
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text("\(t.tipoTreino) • \(t.horarioInicio)")
                                            .font(.system(size: 16, weight: .bold))
                                            .foregroundColor(.clockTextLight)
                                        Text("Término às \(t.horarioFim) • \(diasNomes[t.diaSemana] ?? "")")
                                            .font(.system(size: 12))
                                            .foregroundColor(.clockTextDim)
                                    }
                                    Spacer()
                                    Text("Selecionar")
                                        .font(.system(size: 13, weight: .bold))
                                        .foregroundColor(.clockPillBlue)
                                }
                                .padding(14)
                                .background(Color.clockChipBg)
                                .cornerRadius(12)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                    .padding(.horizontal, 20)
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}

