import Foundation

/// Perfil do aluno com graduação e faixa
public struct AlunoPerfil: Codable, Equatable {
    public let nome: String
    public let faixa: String
    public let graus: Int
    public let proximaFaixa: String?

    public init(nome: String, faixa: String, graus: Int, proximaFaixa: String? = nil) {
        self.nome = nome
        self.faixa = faixa
        self.graus = graus
        self.proximaFaixa = proximaFaixa
    }
}

