package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.dao.ActividadDao
import cl.duoc.mpazneb.data.local.dao.PreguntaDao
import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import cl.duoc.mpazneb.data.local.entity.PreguntaEntity
import cl.duoc.mpazneb.data.remote.ApiService
import cl.duoc.mpazneb.data.remote.EstadoRequest
import cl.duoc.mpazneb.model.Dificultad
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Pruebas del repositorio de actividades (persistencia remota en el microservicio + Room).
 * Se MOCKEAN con MockK los DAO de Room y el ApiService del microservicio: así se prueba solo la
 * lógica del repositorio, sin base de datos real, sin red y sin emulador.
 */
class ActividadRepositoryTest : StringSpec({

    // Mocks: "relaxed" = las funciones que no se configuran no hacen nada (sirve para los DAO)
    val actividadDao = mockk<ActividadDao>(relaxed = true)
    val preguntaDao = mockk<PreguntaDao>(relaxed = true)
    val mockApi = mockk<ApiService>()
    val repo = ActividadRepository(actividadDao, preguntaDao, mockApi)

    val actividad = ActividadEntity(
        id = 5, nombre = "Sumas", modulo = Modulo.MATEMATICA, curso = 1, dificultad = Dificultad.INICIAL,
        objetivo = "Sumar", instrucciones = "Resuelve", intentosPermitidos = 3, estado = EstadoActividad.BORRADOR
    )
    val pregunta = PreguntaEntity(
        id = 9, actividadId = 5, enunciado = "2 + 2", alternativaA = "4", alternativaB = "5",
        alternativaC = "6", correcta = 0, retroalimentacion = "Dos más dos son cuatro"
    )

    // Antes de cada prueba se limpian los registros de llamadas de los mocks
    beforeTest { clearAllMocks(answers = false) }

    "guardar una actividad nueva hace POST como borrador y guarda en Room lo que responde el microservicio" {
        val enviada = slot<ActividadEntity>()
        coEvery { mockApi.crearActividad(capture(enviada)) } returns actividad.copy(id = 13)
        runTest {
            val resultado = repo.guardar(actividad.copy(id = 0, estado = EstadoActividad.DISPONIBLE))
            enviada.captured.estado shouldBe EstadoActividad.BORRADOR // regla: siempre parte en borrador
            resultado.id shouldBe 13L                                 // el id lo asigna el microservicio
            coVerify { actividadDao.upsert(actividad.copy(id = 13)) }
        }
    }

    "guardar una actividad existente hace PUT" {
        coEvery { mockApi.actualizarActividad(5, actividad) } returns actividad
        runTest {
            repo.guardar(actividad)
            coVerify(exactly = 1) { mockApi.actualizarActividad(5, actividad) }
            coVerify(exactly = 0) { mockApi.crearActividad(any()) }
            coVerify { actividadDao.upsert(actividad) }
        }
    }

    "si el microservicio no responde no se guarda nada en Room" {
        coEvery { mockApi.crearActividad(any()) } throws IOException("sin red")
        runTest {
            shouldThrow<IOException> { repo.guardar(actividad.copy(id = 0)) }
            coVerify(exactly = 0) { actividadDao.upsert(any()) }
        }
    }

    "no se puede dejar disponible una actividad sin preguntas" {
        coEvery { preguntaDao.getByActividad(5) } returns emptyList()
        runTest {
            repo.cambiarEstado(actividad, EstadoActividad.DISPONIBLE) shouldBe
                "Agrega al menos una pregunta antes de dejarla disponible."
            coVerify(exactly = 0) { mockApi.cambiarEstado(any(), any()) }
        }
    }

    "con preguntas el estado se cambia en el microservicio (PATCH)" {
        val disponible = actividad.copy(estado = EstadoActividad.DISPONIBLE)
        coEvery { preguntaDao.getByActividad(5) } returns listOf(pregunta)
        coEvery { mockApi.cambiarEstado(5, EstadoRequest(EstadoActividad.DISPONIBLE)) } returns disponible
        runTest {
            repo.cambiarEstado(actividad, EstadoActividad.DISPONIBLE) shouldBe "La actividad quedó: disponible"
            coVerify { actividadDao.upsert(disponible) }
        }
    }

    "desactivar una actividad no revisa las preguntas" {
        val desactivada = actividad.copy(estado = EstadoActividad.DESACTIVADA)
        coEvery { mockApi.cambiarEstado(5, EstadoRequest(EstadoActividad.DESACTIVADA)) } returns desactivada
        runTest {
            repo.cambiarEstado(actividad, EstadoActividad.DESACTIVADA)
            coVerify(exactly = 0) { preguntaDao.getByActividad(any()) }
            coVerify { actividadDao.upsert(desactivada) }
        }
    }

    "eliminar un borrador hace DELETE y lo borra de Room junto a sus preguntas" {
        coEvery { mockApi.eliminarActividad(5) } returns Response.success(Unit)
        runTest {
            repo.eliminar(actividad) shouldBe null
            coVerify { preguntaDao.deleteByActividad(5) }
            coVerify { actividadDao.deleteById(5) }
        }
    }

    "no se puede eliminar una actividad disponible" {
        runTest {
            repo.eliminar(actividad.copy(estado = EstadoActividad.DISPONIBLE)) shouldBe
                "Solo se pueden eliminar actividades en borrador."
            coVerify(exactly = 0) { mockApi.eliminarActividad(any()) }
        }
    }

    "si el DELETE responde error no se borra de Room" {
        coEvery { mockApi.eliminarActividad(5) } returns Response.error(409, "{}".toResponseBody())
        runTest {
            shouldThrow<HttpException> { repo.eliminar(actividad) }
            coVerify(exactly = 0) { actividadDao.deleteById(any()) }
        }
    }

    "una pregunta nueva se crea con POST y una existente se actualiza con PUT" {
        coEvery { mockApi.crearPregunta(5, any()) } returns pregunta
        coEvery { mockApi.actualizarPregunta(9, pregunta) } returns pregunta
        runTest {
            repo.guardarPregunta(pregunta.copy(id = 0))
            repo.guardarPregunta(pregunta)
            coVerify(exactly = 1) { mockApi.crearPregunta(5, pregunta.copy(id = 0)) }
            coVerify(exactly = 1) { mockApi.actualizarPregunta(9, pregunta) }
            coVerify(exactly = 2) { preguntaDao.upsert(pregunta) }
        }
    }

    "eliminar una pregunta hace DELETE y la borra de Room" {
        coEvery { mockApi.eliminarPregunta(9) } returns Response.success(Unit)
        runTest {
            repo.eliminarPregunta(pregunta)
            coVerify { preguntaDao.delete(pregunta) }
        }
    }

    "sincronizar deja Room igual que el microservicio" {
        coEvery { mockApi.listarActividades() } returns listOf(actividad)
        coEvery { mockApi.listarPreguntas() } returns listOf(pregunta)
        runTest {
            repo.sincronizar()
            coVerify { actividadDao.upsertAll(listOf(actividad)) }
            coVerify { actividadDao.deleteNotIn(listOf(5L)) }
            coVerify { preguntaDao.upsertAll(listOf(pregunta)) }
            coVerify { preguntaDao.deleteNotIn(listOf(9L)) }
        }
    }

    "el nombre repetido se valida contra Room" {
        coEvery { actividadDao.contarConNombre("Sumas", 0) } returns 1
        coEvery { actividadDao.contarConNombre("Nueva", 0) } returns 0
        runTest {
            repo.nombreRepetido("  Sumas ", 0) shouldBe true
            repo.nombreRepetido("Nueva", 0) shouldBe false
        }
    }

    "las lecturas salen de Room" {
        every { actividadDao.getAll() } returns flowOf(listOf(actividad))
        every { actividadDao.observeById(5) } returns flowOf(actividad)
        coEvery { actividadDao.getById(5) } returns actividad
        every { preguntaDao.observeByActividad(5) } returns flowOf(listOf(pregunta))
        every { preguntaDao.getAll() } returns flowOf(listOf(pregunta))
        coEvery { preguntaDao.getById(9) } returns pregunta
        runTest {
            repo.observarTodas().first() shouldContainExactly listOf(actividad)
            repo.observar(5).first() shouldBe actividad
            repo.obtener(5) shouldBe actividad
            repo.observarPreguntas(5).first() shouldContainExactly listOf(pregunta)
            repo.observarTodasLasPreguntas().first() shouldContainExactly listOf(pregunta)
            repo.obtenerPregunta(9) shouldBe pregunta
        }
    }
})
