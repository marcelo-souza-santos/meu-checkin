import SwiftUI

extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xff) / 255,
            green: Double((hex >> 8) & 0xff) / 255,
            blue: Double(hex & 0xff) / 255,
            opacity: alpha
        )
    }

    // Paleta Dark temática fiel ao app nativo de relógio e ao Meu Check-in Android
    static let clockDarkBackground = Color(hex: 0x121316)
    static let clockCardBlue = Color(hex: 0x0F3647)
    static let clockCardOff = Color(hex: 0x1E2024)
    static let clockTextLight = Color(hex: 0xE2E3E7)
    static let clockTextDim = Color(hex: 0x8E9199)
    static let clockPillBlue = Color(hex: 0x7FCFFF)
    static let clockPillOnBlue = Color(hex: 0x00344D)
    static let clockChipBg = Color(hex: 0x282A2F)
    static let clockDeleteRed = Color(hex: 0xFFFF8585)
}

