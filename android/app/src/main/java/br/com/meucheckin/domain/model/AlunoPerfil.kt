package br.com.meucheckin.domain.model

/** Dados resumidos do perfil do aluno da academia. */
data class AlunoPerfil(
    val nome: String,
    val faixa: String,
    val graus: Int,
    val proximaFaixa: String? = null
)

