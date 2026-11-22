package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.dao.EstudianteDao
import cl.duoc.mpazneb.data.local.dao.RespuestaDao
import cl.duoc.mpazneb.data.local.dao.ResultadoDao
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.data.local.entity.RespuestaEntity
import cl.duoc.mpazneb.data.remote.EstudiantesApiService
import cl.duoc.mpazneb.data.remote.ObservacionRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
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
 * Pruebas del repositorio de estudiantes (microservicio estudiantes-service + Room).
 * Se MOCKEAN con MockK los DAO y el EstudiantesApiService: no hay base de datos real ni red.
 */
class EstudianteRepositoryTest : StringSpec({

    val estudianteDao = mockk<EstudianteDao>(relaxed = true)
    val resultadoDao = mockk<ResultadoDao>(relaxed = true)
    val respuestaDao = mockk<RespuestaDao>(relaxed = true)
    val mockApi = mockk<EstudiantesApiService>()
    val repo = EstudianteRepository(estudianteDao, resultadoDao, respuestaDao, mockApi)

    val luna = EstudianteEntity(id = 7, alias = "Luna", curso = 1, pinHash = hashSha256("1234"))

    beforeTest { clearAllMocks(answers = false) }

    "crear un estudiante hace POST con el PIN como hash y guarda en Room lo que responde el microservicio" {
        val enviado = slot<EstudianteEntity>()
        coEvery { mockApi.crearEstudiante(capture(enviado)) } returns luna
        runTest {
            repo.crear(" Luna ", 1, "1234", null)
            enviado.captured.alias shouldBe "Luna"
            enviado.captured.pinHash shouldBe hashSha256("1234") // nunca el PIN tal cual
            coVerify { estudianteDao.upsert(luna) }
        }
    }

    "si el microservicio rechaza el estudiante no se guarda en Room" {
        coEvery { mockApi.crearEstudiante(any()) } throws HttpException(Response.error<Any>(409, "{}".toResponseBody()))
        runTest {
            shouldThrow<HttpException> { repo.crear("Luna", 1, "1234", null) }
            coVerify(exactly = 0) { estudianteDao.upsert(any()) }
        }
    }

    "el apodo repetido en el curso se valida contra Room" {
        coEvery { estudianteDao.contarConAlias("Luna", 1) } returns 1
        runTest { repo.aliasRepetido(" Luna ", 1) shouldBe true }
    }

    "guardar la observación hace PATCH y actualiza Room" {
        val conObservacion = luna.copy(observacion = "Lee con más fluidez")
        coEvery { mockApi.guardarObservacion(7, ObservacionRequest("Lee con más fluidez")) } returns conObservacion
        runTest {
            repo.guardarObservacion(7, "  Lee con más fluidez ")
            coVerify { estudianteDao.upsert(conObservacion) }
        }
    }

    "eliminar hace DELETE y borra de Room al estudiante con su historial" {
        coEvery { mockApi.eliminarEstudiante(7) } returns Response.success(Unit)
        runTest {
            repo.eliminar(7)
            coVerify { respuestaDao.deleteByEstudiante(7) }
            coVerify { resultadoDao.deleteByEstudiante(7) }
            coVerify { estudianteDao.deleteById(7) }
        }
    }

    "si el DELETE falla no se borra nada de Room" {
        coEvery { mockApi.eliminarEstudiante(7) } returns Response.error(404, "{}".toResponseBody())
        runTest {
            shouldThrow<HttpException> { repo.eliminar(7) }
            coVerify(exactly = 0) { estudianteDao.deleteById(any()) }
        }
    }

    "sincronizar guarda en Room los estudiantes del microservicio" {
        coEvery { mockApi.listarEstudiantes() } returns listOf(luna)
        runTest {
            repo.sincronizar()
            coVerify { estudianteDao.upsertAll(listOf(luna)) }
        }
    }

    "registrar un intento cuenta aciertos, guarda las respuestas y lo envía al microservicio" {
        coEvery { resultadoDao.insert(any()) } returns 50
        coEvery { mockApi.registrarResultado(7, any()) } answers { secondArg() }
        val respuestas = listOf(
            RespuestaEntity(resultadoId = 0, preguntaId = 1, elegida = 0, esCorrecta = true),
            RespuestaEntity(resultadoId = 0, preguntaId = 2, elegida = 1, esCorrecta = false)
        )
        runTest {
            val resultado = repo.registrarIntento(7, 3, respuestas)
            resultado.id shouldBe 50L
            resultado.aciertos shouldBe 1
            resultado.total shouldBe 2
            coVerify { respuestaDao.insertAll(respuestas.map { it.copy(resultadoId = 50) }) }
            coVerify(exactly = 1) { mockApi.registrarResultado(7, any()) }
        }
    }

    "sin conexión el intento igual queda guardado en el teléfono" {
        coEvery { resultadoDao.insert(any()) } returns 51
        coEvery { mockApi.registrarResultado(any(), any()) } throws IOException("sin red")
        runTest {
            val resultado = repo.registrarIntento(7, 3, emptyList())
            resultado.id shouldBe 51L // no lanza excepción: la práctica no se interrumpe
        }
    }

    "las lecturas del estudiante salen de Room" {
        every { estudianteDao.getAll() } returns flowOf(listOf(luna))
        every { estudianteDao.observeById(7) } returns flowOf(luna)
        coEvery { estudianteDao.getById(7) } returns luna
        every { resultadoDao.observeByEstudiante(7) } returns flowOf(emptyList())
        every { respuestaDao.observeByEstudiante(7) } returns flowOf(emptyList())
        coEvery { resultadoDao.contarIntentos(7, 3) } returns 2
        runTest {
            repo.observarTodos().first() shouldContainExactly listOf(luna)
            repo.observar(7).first() shouldBe luna
            repo.obtener(7) shouldBe luna
            repo.observarResultados(7).first().shouldBeEmpty()
            repo.observarRespuestas(7).first().shouldBeEmpty()
            repo.intentosUsados(7, 3) shouldBe 2
        }
    }
})
