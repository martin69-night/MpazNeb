package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.model.Feriado
import cl.duoc.mpazneb.data.remote.FeriadosApiService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.time.LocalDate

/**
 * Pruebas del repositorio de la API externa de feriados.
 * Se MOCKEA el FeriadosApiService con MockK: la prueba no llama a internet, así siempre da el mismo resultado.
 */
class FeriadoRepositoryTest : StringSpec({

    "getProximosFeriados() debe retornar solo feriados nacionales, ordenados y como máximo la cantidad pedida" {
        // 1. Simulamos el resultado de la API
        val fakeFeriados = listOf(
            Feriado("2027-01-01", "Año Nuevo", true),
            Feriado("2026-12-25", "Navidad", true),
            Feriado("2026-12-10", "Feriado regional", false),
            Feriado("2026-12-08", "Inmaculada Concepción", true)
        )
        // 2. Creamos un mock de FeriadosApiService
        val mockApi = mockk<FeriadosApiService>()
        coEvery { mockApi.getProximosFeriados("CL") } returns fakeFeriados
        // 3. Creamos el repositorio inyectando el mock
        val repo = FeriadoRepository(mockApi)
        // 4. Ejecutamos el test
        runTest {
            val result = repo.getProximosFeriados(cantidad = 2)
            result.map { it.localName } shouldContainExactly listOf("Inmaculada Concepción", "Navidad")
        }
    }

    "fechaLegible() muestra la fecha en español" {
        fechaLegible("2026-12-08") shouldBe "martes 8 de diciembre"
    }

    "fechaLegible() deja tal cual una fecha con formato incorrecto" {
        fechaLegible("mañana") shouldBe "mañana"
    }

    "diasHasta() calcula los días que faltan para el feriado" {
        diasHasta("2026-12-08", hoy = LocalDate.of(2026, 11, 21)) shouldBe 17L
    }
})
