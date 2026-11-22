package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.model.Feriado
import cl.duoc.mpazneb.data.remote.FeriadosApiService
import cl.duoc.mpazneb.data.remote.FeriadosRetrofitInstance
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Este repositorio se encarga de acceder a los feriados usando Retrofit (API externa).
 * Sirve para que la docente planifique: no conviene dejar una actividad nueva justo antes de un feriado.
 * Es "open" para poder crear una versión falsa en la prueba de UI (igual que la Guía 15).
 */
open class FeriadoRepository(private val api: FeriadosApiService = FeriadosRetrofitInstance.api) {

    // Función que obtiene los próximos feriados NACIONALES de Chile, del más cercano al más lejano
    open suspend fun getProximosFeriados(cantidad: Int = 3): List<Feriado> =
        api.getProximosFeriados("CL")
            .filter { it.global }   // se descartan los feriados regionales
            .sortedBy { it.date }
            .take(cantidad)
}

private val formatoFecha = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es-CL"))

/** "2026-12-08" → "martes 8 de diciembre". Si la fecha viene mal, se muestra tal cual. */
fun fechaLegible(fecha: String): String = try {
    LocalDate.parse(fecha).format(formatoFecha)
} catch (_: Exception) {
    fecha
}

/** Días que faltan desde [hoy] hasta la fecha del feriado. */
fun diasHasta(fecha: String, hoy: LocalDate = LocalDate.now()): Long =
    ChronoUnit.DAYS.between(hoy, LocalDate.parse(fecha))
