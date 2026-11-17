package br.com.meucheckin

import br.com.meucheckin.data.ApiClient
import br.com.meucheckin.data.local.CheckinLogStore
import br.com.meucheckin.data.local.TrainingAlarmStore
import br.com.meucheckin.data.repository.CheckinRepositoryImpl
import br.com.meucheckin.domain.model.TrainingAlarm
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CheckinRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var sessionStore: FakeStore
    private lateinit var api: ApiClient
    private lateinit var alarmStore: TrainingAlarmStore
    private lateinit var logStore: CheckinLogStore
    private lateinit var repository: CheckinRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        sessionStore = FakeStore()
        sessionStore.accessToken = "valid_token"
        sessionStore.email = "marcelo@test.com"
        sessionStore.password = "123456"

        api = ApiClient(sessionStore, server.url("/").toString().trimEnd('/'))
        alarmStore = TrainingAlarmStore(FakeSharedPreferences())
        logStore = CheckinLogStore(FakeSharedPreferences())
        repository = CheckinRepositoryImpl(api, alarmStore, logStore)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun getAlarms_and_saveAlarm_worksCorrectly() {
        val initial = repository.getAlarms()
        assertEquals(0, initial.size) // Defaults to empty list

        val custom = TrainingAlarm(
            id = "custom-1",
            titulo = "No-Gi",
            horarioTreino = "17:00",
            diasSemana = setOf(2, 4)
        )
        repository.saveAlarm(custom)

        val updated = repository.getAlarms()
        assertEquals(1, updated.size)
        assertTrue(updated.any { it.id == "custom-1" })

        repository.toggleAlarm("custom-1", false)
        val toggled = repository.getAlarms().first { it.id == "custom-1" }
        assertFalse(toggled.enabled)

        repository.deleteAlarm("custom-1")
        val afterDelete = repository.getAlarms()
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun getAlunoPerfil_parsesGraduacoesResponse() {
        val body = """
        {
            "aluno": {
                "id": "abc-123",
                "nome_completo": "Marcelo Souza Santos",
                "cor_faixa": "branca",
                "qntd_graus": 2,
                "graduacao_status": {
                    "proxima_faixa": "azul"
                }
            }
        }
        """.trimIndent()

        server.enqueue(MockResponse().setBody(body))

        val perfil = repository.getAlunoPerfil()
        assertNotNull(perfil)
        assertEquals("Marcelo Souza Santos", perfil?.nome)
        assertEquals("branca", perfil?.faixa)
        assertEquals(2, perfil?.graus)
        assertEquals("azul", perfil?.proximaFaixa)
    }

    @Test
    fun getTreinosDisponiveis_parsesMainAndUpcoming() {
        val body = """
        {
            "main": {
                "slot": {
                    "schedule_id": 2,
                    "tipo_treino": "Gi",
                    "horario_inicio": "09:00",
                    "horario_fim": "10:30",
                    "dia_semana": 1
                }
            },
            "upcoming": [
                {
                    "schedule_id": 3,
                    "tipo_treino": "No-Gi",
                    "horario_inicio": "20:00",
                    "horario_fim": "21:30",
                    "dia_semana": 1
                }
            ]
        }
        """.trimIndent()

        server.enqueue(MockResponse().setBody(body))

        val treinos = repository.getTreinosDisponiveis()
        assertEquals(2, treinos.size)
        assertEquals("09:00", treinos[0].horarioInicio)
        assertEquals("20:00", treinos[1].horarioInicio)
    }

    @Test
    fun getTreinosDisponiveis_apiError_returnsFallbackList() {
        server.enqueue(MockResponse().setResponseCode(500))

        val treinos = repository.getTreinosDisponiveis()
        assertTrue(treinos.isNotEmpty())
        assertTrue(treinos.any { it.horarioInicio == "09:00" })
    }
}

