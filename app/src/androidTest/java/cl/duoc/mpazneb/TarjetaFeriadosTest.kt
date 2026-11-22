package cl.duoc.mpazneb

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import cl.duoc.mpazneb.data.model.Feriado
import cl.duoc.mpazneb.data.repository.FeriadoRepository
import cl.duoc.mpazneb.ui.components.TarjetaFeriados
import cl.duoc.mpazneb.viewmodel.FeriadosViewModel
import org.junit.Rule
import org.junit.Test

/**
 * Prueba de UI con JUnit y Compose (Guía 15): se dibuja la tarjeta de feriados con datos falsos
 * y se verifica que los feriados aparecen en pantalla. Se ejecuta en el celular o emulador.
 */
class TarjetaFeriadosTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun los_feriados_deben_aparecer_en_pantalla() {
        // Simulamos los datos que entregaría la API externa
        val fakeFeriados = listOf(
            Feriado("2026-12-08", "Inmaculada Concepción", true),
            Feriado("2026-12-25", "Navidad", true)
        )
        // Subclase falsa del repositorio: no llama a internet
        val fakeRepository = object : FeriadoRepository() {
            override suspend fun getProximosFeriados(cantidad: Int) = fakeFeriados
        }
        // Renderizamos la tarjeta con un ViewModel que usa el repositorio falso
        composeRule.setContent {
            TarjetaFeriados(viewModel = FeriadosViewModel(fakeRepository))
        }
        // Validamos que los feriados se muestran correctamente en la UI
        composeRule.onNodeWithText("Martes 8 de diciembre: Inmaculada Concepción").assertIsDisplayed()
        composeRule.onNodeWithText("Viernes 25 de diciembre: Navidad").assertIsDisplayed()
    }
}
