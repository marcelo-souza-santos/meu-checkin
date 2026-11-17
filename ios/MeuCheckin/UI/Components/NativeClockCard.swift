import SwiftUI

public struct NativeClockCard: View {
    public let alarm: TrainingAlarm
    public let isExpanded: Bool
    public let onCardClick: () -> Void
    public let onOpenTreinoSelect: () -> Void
    public let onToggle: (Bool) -> Void
    public let onSave: (TrainingAlarm) -> Void
    public let onDelete: () -> Void

    @State private var horarioState: String
    @State private var tituloState: String
    @State private var offsetState: Int
    @State private var randomJitterState: Bool
    @State private var diasState: [Int: Bool]

    private let diasOrdem: [(Int, String)] = [
        (7, "D"), (1, "S"), (2, "T"), (3, "Q"), (4, "Q"), (5, "S"), (6, "S")
    ]

    public init(
        alarm: TrainingAlarm,
        isExpanded: Bool,
        onCardClick: @escaping () -> Void,
        onOpenTreinoSelect: @escaping () -> Void,
        onToggle: @escaping (Bool) -> Void,
        onSave: @escaping (TrainingAlarm) -> Void,
        onDelete: @escaping () -> Void
    ) {
        self.alarm = alarm
        self.isExpanded = isExpanded
        self.onCardClick = onCardClick
        self.onOpenTreinoSelect = onOpenTreinoSelect
        self.onToggle = onToggle
        self.onSave = onSave
        self.onDelete = onDelete

        _horarioState = State(initialValue: alarm.horarioTreino)
        _tituloState = State(initialValue: alarm.titulo)
        _offsetState = State(initialValue: alarm.offsetMinutos)
        _randomJitterState = State(initialValue: alarm.variacaoRandomica)

        var map: [Int: Bool] = [:]
        (1...7).forEach { map[$0] = alarm.diasSemana.contains($0) }
        _diasState = State(initialValue: map)
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header: Descrição dos dias / Switch
            HStack {
                Text("\(AlarmFormatters.formatDiasCompacto(alarm.diasSemana)) • \(alarm.titulo)")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.clockTextDim)
                Spacer()
                Toggle("", isOn: Binding(
                    get: { alarm.enabled },
                    set: { onToggle($0) }
                ))
                .labelsHidden()
                .tint(.clockPillBlue)
            }

            // Horário grande
            HStack(alignment: .bottom) {
                Text(horarioState)
                    .font(.system(size: 48, weight: .bold))
                    .foregroundColor(alarm.enabled ? .clockTextLight : .clockTextDim.opacity(0.6))

                Spacer()

                if isExpanded {
                    Button(action: onOpenTreinoSelect) {
                        Text("Trocar Treino")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(.clockTextLight)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 6)
                            .background(Color.clockChipBg)
                            .cornerRadius(16)
                    }
                }
            }
            .padding(.top, 4)

            // Área Expandida
            if isExpanded {
                VStack(spacing: 16) {
                    Divider().background(Color.clockTextDim.opacity(0.2)).padding(.top, 8)

                    // Seletor de dias circular: D S T Q Q S S
                    HStack(spacing: 8) {
                        ForEach(diasOrdem, id: \.0) { diaNum, sigla in
                            let isSel = diasState[diaNum] ?? false
                            Button(action: {
                                diasState[diaNum] = !isSel
                            }) {
                                Text(sigla)
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(isSel ? .clockPillOnBlue : .clockTextLight)
                                    .frame(width: 38, height: 38)
                                    .background(isSel ? Color.clockPillBlue : Color.clockChipBg)
                                    .clipShape(Circle())
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                    .frame(maxWidth: .infinity)

                    // Próximo check-in informativo
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Próximo check-in")
                                .font(.system(size: 11))
                                .foregroundColor(.clockTextDim)
                            Text(AlarmFormatters.calcularProximoTexto(alarm.diasSemana))
                                .font(.system(size: 13, weight: .medium))
                                .foregroundColor(.clockTextLight)
                        }
                        Spacer()
                        Text("+\(offsetState) min após início")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(.clockPillBlue)
                    }

                    // Seletor de minutos de disparo
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Disparar após início")
                                .font(.system(size: 14))
                                .foregroundColor(.clockTextLight)
                            Text("Tempo base estimado")
                                .font(.system(size: 11))
                                .foregroundColor(.clockTextDim)
                        }
                        Spacer()
                        HStack(spacing: 6) {
                            ForEach([5, 10, 15, 20], id: \.self) { min in
                                let sel = offsetState == min
                                Button(action: { offsetState = min }) {
                                    Text("~\(min) min")
                                        .font(.system(size: 12, weight: .bold))
                                        .foregroundColor(sel ? .clockPillOnBlue : .clockTextDim)
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 4)
                                        .background(sel ? Color.clockPillBlue : Color.clear)
                                        .cornerRadius(8)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                    }
                    .padding(12)
                    .background(Color.clockChipBg)
                    .cornerRadius(12)

                    // Switch: Simular horário humano
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Simular horário humano")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.clockTextLight)
                            Text("Varia alguns minutos e segundos aleatoriamente a cada dia.")
                                .font(.system(size: 11))
                                .foregroundColor(.clockTextDim)
                        }
                        Spacer()
                        Toggle("", isOn: $randomJitterState)
                            .labelsHidden()
                            .tint(.clockPillBlue)
                    }
                    .padding(12)
                    .background(Color.clockChipBg)
                    .cornerRadius(12)

                    // Botões: Excluir / Salvar
                    HStack {
                        Button(action: onDelete) {
                            Text("Excluir")
                                .font(.system(size: 15, weight: .medium))
                                .foregroundColor(.clockDeleteRed)
                        }

                        Spacer()

                        Button(action: {
                            let selectedDays = Set(diasState.filter { $0.value }.map { $0.key })
                            let updated = TrainingAlarm(
                                id: alarm.id,
                                titulo: tituloState.isEmpty ? "Treino" : tituloState,
                                horarioTreino: horarioState,
                                diasSemana: selectedDays.isEmpty ? [1, 3, 5] : selectedDays,
                                offsetMinutos: offsetState,
                                variacaoRandomica: randomJitterState,
                                enabled: true
                            )
                            onSave(updated)
                        }) {
                            Text("Salvar")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(.clockPillOnBlue)
                                .padding(.horizontal, 24)
                                .padding(.vertical, 10)
                                .background(Color.clockPillBlue)
                                .cornerRadius(20)
                        }
                    }
                    .padding(.top, 4)
                }
            }
        }
        .padding(20)
        .background(alarm.enabled ? Color.clockCardBlue : Color.clockCardOff)
        .cornerRadius(24)
        .contentShape(Rectangle())
        .onTapGesture {
            if !isExpanded {
                onCardClick()
            }
        }
    }
}

