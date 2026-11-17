import SwiftUI

public struct ClockAlarmsView: View {
    @ObservedObject public var viewModel: MainViewModel

    @State private var expandedAlarmId: UUID? = nil
    @State private var draftNewAlarm: TrainingAlarm? = nil
    @State private var selectTreinoDialogOpen: Bool = false
    @State private var pickingForExistingAlarmId: UUID? = nil
    @State private var pickingForDraft: Bool = false
    @State private var showMenuSheet: Bool = false

    public var body: some View {
        ZStack(alignment: .bottomTrailing) {
            Color.clockDarkBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                // TopBar
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 3) {
                        Text("Check-in Automático")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.clockTextLight)

                        if let perfil = viewModel.perfilAluno {
                            Text("\(perfil.nome) • Faixa \(perfil.faixa) (\(perfil.graus)º grau)\(perfil.proximaFaixa != nil ? " → \(perfil.proximaFaixa!)" : "")")
                                .font(.system(size: 12, weight: .medium))
                                .foregroundColor(.clockPillBlue)
                        }
                    }

                    Spacer()

                    Menu {
                        Button(action: {
                            Task { await viewModel.testarCheckinAgora() }
                        }) {
                            Label(viewModel.isExecutingAction ? "Verificando..." : "Testar check-in agora", systemImage: "play.circle")
                        }

                        Button(role: .destructive, action: {
                            viewModel.logout()
                        }) {
                            Label("Sair da conta (Logout)", systemImage: "rectangle.portrait.and.arrow.right")
                        }
                    } label: {
                        Image(systemName: "ellipsis")
                            .rotationEffect(.degrees(90))
                            .font(.system(size: 18))
                            .foregroundColor(.clockTextLight)
                            .padding(8)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 16)
                .padding(.bottom, 12)

                // Conteúdo / Lista de Alarmes
                ScrollView {
                    VStack(spacing: 16) {
                        // Feedback banner
                        if let feedback = viewModel.actionFeedback {
                            HStack {
                                Text(feedback)
                                    .font(.system(size: 13))
                                    .foregroundColor(.clockTextLight)
                                Spacer()
                            }
                            .padding(14)
                            .background(Color.clockCardBlue)
                            .cornerRadius(14)
                        }

                        // Rascunho de novo alarme (+)
                        if let draft = draftNewAlarm {
                            VStack(alignment: .leading, spacing: 6) {
                                Text("Novo Agendamento (Rascunho):")
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(.clockPillBlue)

                                NativeClockCard(
                                    alarm: draft,
                                    isExpanded: true,
                                    onCardClick: {},
                                    onOpenTreinoSelect: {
                                        pickingForDraft = true
                                        pickingForExistingAlarmId = nil
                                        selectTreinoDialogOpen = true
                                    },
                                    onToggle: { draftNewAlarm?.enabled = $0 },
                                    onSave: { finalized in
                                        viewModel.saveAlarm(finalized)
                                        draftNewAlarm = nil
                                        expandedAlarmId = nil
                                    },
                                    onDelete: {
                                        draftNewAlarm = nil
                                    }
                                )
                            }
                        }

                        // Estado vazio
                        if viewModel.alarms.isEmpty && draftNewAlarm == nil {
                            VStack(spacing: 8) {
                                Text("Nenhum alarme de check-in configurado.\nToque em + para adicionar os treinos que você frequenta.")
                                    .font(.system(size: 14))
                                    .foregroundColor(.clockTextDim)
                                    .multilineTextAlignment(.center)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.top, 60)
                        }

                        // Lista de alarmes
                        ForEach(viewModel.alarms) { alarm in
                            let isExpanded = expandedAlarmId == alarm.id
                            NativeClockCard(
                                alarm: alarm,
                                isExpanded: isExpanded,
                                onCardClick: {
                                    withAnimation {
                                        expandedAlarmId = isExpanded ? nil : alarm.id
                                    }
                                },
                                onOpenTreinoSelect: {
                                    pickingForDraft = false
                                    pickingForExistingAlarmId = alarm.id
                                    selectTreinoDialogOpen = true
                                },
                                onToggle: { viewModel.toggleAlarm(id: alarm.id, enabled: $0) },
                                onSave: { updated in
                                    viewModel.saveAlarm(updated)
                                    expandedAlarmId = nil
                                },
                                onDelete: {
                                    viewModel.deleteAlarm(id: alarm.id)
                                    if expandedAlarmId == alarm.id { expandedAlarmId = nil }
                                }
                            )
                        }

                        Spacer().frame(height: 80)
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 8)
                }
            }

            // Floating Action Button (+)
            Button(action: {
                pickingForDraft = false
                pickingForExistingAlarmId = nil
                selectTreinoDialogOpen = true
            }) {
                Image(systemName: "plus")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.clockPillOnBlue)
                    .frame(width: 56, height: 56)
                    .background(Color.clockPillBlue)
                    .clipShape(Circle())
                    .shadow(radius: 4)
            }
            .padding(.trailing, 20)
            .padding(.bottom, 24)
        }
        .sheet(isPresented: $selectTreinoDialogOpen) {
            SelectTreinoDialog(
                treinos: viewModel.availableTreinos,
                onSelect: { opcao in
                    selectTreinoDialogOpen = false
                    if pickingForDraft {
                        draftNewAlarm?.titulo = opcao.tipoTreino
                        draftNewAlarm?.horarioTreino = opcao.horarioInicio
                    } else if let existId = pickingForExistingAlarmId,
                              let existing = viewModel.alarms.first(where: { $0.id == existId }) {
                        var updated = existing
                        updated.titulo = opcao.tipoTreino
                        updated.horarioTreino = opcao.horarioInicio
                        viewModel.saveAlarm(updated)
                    } else {
                        // Novo alarme via botão (+)
                        draftNewAlarm = TrainingAlarm(
                            titulo: opcao.tipoTreino,
                            horarioTreino: opcao.horarioInicio,
                            diasSemana: [opcao.diaSemana],
                            offsetMinutos: 10,
                            variacaoRandomica: true,
                            enabled: true
                        )
                    }
                },
                onDismiss: { selectTreinoDialogOpen = false }
            )
        }
    }
}

