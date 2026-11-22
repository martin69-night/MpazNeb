package cl.duoc.mpazneb.viewmodel

import cl.duoc.mpazneb.data.model.Feriado
import cl.duoc.mpazneb.data.repository.FeriadoRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Pruebas del ViewModel de feriados. Se mockea el repositorio con MockK y se reemplaza el hilo principal
 * de Android (Dispatchers.Main) por uno de prueba, porque viewModelScope lo usa y en la JVM no existe.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeriadosViewModelTest : StringSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    "uiState debe contener los feriados esperados después de fetchFeriados()" {
        val fakeFeriados = listOf(Feriado("2026-12-08", "Inmaculada Concepción", true))
        val repository = mockk<FeriadoRepository>()
        coEvery { repository.getProximosFeriados() } returns fakeFeriados

        val viewModel = FeriadosViewModel(repository) // init llama a fetchFeriados()

        viewModel.uiState.value shouldBe FeriadosUiState.Listo(fakeFeriados)
    }

    "si la API falla uiState muestra un error y luego se puede reintentar" {
        val repository = mockk<FeriadoRepository>()
        coEvery { repository.getProximosFeriados() } throws java.io.IOException("sin internet")
        val viewModel = FeriadosViewModel(repository)
        viewModel.uiState.value.shouldBeInstanceOf<FeriadosUiState.Error>()

        coEvery { repository.getProximosFeriados() } returns emptyList()
        viewModel.fetchFeriados()

        viewModel.uiState.value shouldBe FeriadosUiState.Listo(emptyList())
    }
})
