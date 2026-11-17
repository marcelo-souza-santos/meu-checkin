package br.com.meucheckin.domain.model

/** Modelo para opções de treinos disponíveis na academia vindos da API. */
data class TreinoOpcao(
    val scheduleId: Int,
    val tipoTreino: String,
    val horarioInicio: String,
    val horarioFim: String,
    val diaSemana: Int
)

