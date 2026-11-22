package cl.duoc.mpazneb.data.remote

import cl.duoc.mpazneb.data.model.Feriado
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Esta interfaz define los endpoints HTTP de la API EXTERNA Nager.Date (feriados públicos, sin API key).
 * https://date.nager.at/api/v3/NextPublicHolidays/CL → feriados de Chile de los próximos 365 días.
 *
 * Diferencia con el microservicio: esta API no es nuestra, solo se LEE (GET),
 * no se guarda nada en Room ni en el microservicio, y su formato no lo podemos cambiar.
 */
interface FeriadosApiService {

    // Define una solicitud GET al endpoint /api/v3/NextPublicHolidays/{pais}
    @GET("api/v3/NextPublicHolidays/{pais}")
    suspend fun getProximosFeriados(@Path("pais") pais: String): List<Feriado>
}
