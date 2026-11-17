import Foundation

public struct AlarmFormatters {

    public static func formatDiasCompacto(_ dias: Set<Int>) -> String {
        if dias.count == 7 { return "Todos os dias" }
        if dias == Set([1, 2, 3, 4, 5]) { return "Seg.-sex." }
        if dias == Set([6, 7]) { return "Fim de semana" }

        let nomes: [Int: String] = [
            1: "Seg.", 2: "Ter.", 3: "Qua.", 4: "Qui.",
            5: "Sex.", 6: "Sáb.", 7: "Dom."
        ]
        return dias.sorted().compactMap { nomes[$0] }.joined(separator: " ")
    }

    public static func calcularProximoTexto(_ dias: Set<Int>, date: Date = Date()) -> String {
        let calendar = Calendar.current
        let weekday = calendar.component(.weekday, from: date)
        // 1=Domingo, 2=Segunda ... 7=Sábado -> Converte para 1=Segunda ... 7=Domingo
        let hojeIso: Int
        switch weekday {
        case 2: hojeIso = 1 // Segunda
        case 3: hojeIso = 2 // Terça
        case 4: hojeIso = 3 // Quarta
        case 5: hojeIso = 4 // Quinta
        case 6: hojeIso = 5 // Sexta
        case 7: hojeIso = 6 // Sábado
        case 1: hojeIso = 7 // Domingo
        default: hojeIso = 1
        }

        return dias.contains(hojeIso) ? "Hoje" : "Próximo dia cadastrado"
    }
}

