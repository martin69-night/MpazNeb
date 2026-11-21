package cl.duoc.mpazneb.data.remote

import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.data.local.entity.ResultadoEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Cuerpo de PATCH /api/estudiantes/{id}/observacion. */
data class ObservacionRequest(val observacion: String)

/**
 * Endpoints del segundo MICROSERVICIO PROPIO: estudiantes-service (puerto 8081).
 * El JSON usa los mismos nombres de campo que EstudianteEntity y ResultadoEntity.
 */
interface EstudiantesApiService {

    @GET("api/estudiantes")
    suspend fun listarEstudiantes(): List<EstudianteEntity>

    @POST("api/estudiantes")
    suspend fun crearEstudiante(@Body estudiante: EstudianteEntity): EstudianteEntity

    @PATCH("api/estudiantes/{id}/observacion")
    suspend fun guardarObservacion(@Path("id") id: Long, @Body request: ObservacionRequest): EstudianteEntity

    @DELETE("api/estudiantes/{id}")
    suspend fun eliminarEstudiante(@Path("id") id: Long): Response<Unit>

    @POST("api/estudiantes/{id}/resultados")
    suspend fun registrarResultado(@Path("id") estudianteId: Long, @Body resultado: ResultadoEntity): ResultadoEntity
}
