package cl.duoc.mpazneb.data.remote

import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import cl.duoc.mpazneb.data.local.entity.PreguntaEntity
import cl.duoc.mpazneb.model.EstadoActividad
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Cuerpo de PATCH /api/actividades/{id}/estado. */
data class EstadoRequest(val estado: EstadoActividad)

/**
 * Endpoints del MICROSERVICIO PROPIO (Spring Boot, carpeta microservicios/actividades-service).
 * Retrofit genera la implementación: cada función es una petición HTTP y Gson convierte el JSON.
 * El JSON usa los mismos nombres de campo que ActividadEntity y PreguntaEntity.
 */
interface ApiService {

    // ---------- Actividades ----------

    @GET("api/actividades")
    suspend fun listarActividades(): List<ActividadEntity>

    @POST("api/actividades")
    suspend fun crearActividad(@Body actividad: ActividadEntity): ActividadEntity

    @PUT("api/actividades/{id}")
    suspend fun actualizarActividad(@Path("id") id: Long, @Body actividad: ActividadEntity): ActividadEntity

    @PATCH("api/actividades/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long, @Body request: EstadoRequest): ActividadEntity

    /** 204 No Content: no trae cuerpo, por eso se usa Response<Unit> y se revisa isSuccessful. */
    @DELETE("api/actividades/{id}")
    suspend fun eliminarActividad(@Path("id") id: Long): Response<Unit>

    // ---------- Preguntas ----------

    @GET("api/preguntas")
    suspend fun listarPreguntas(): List<PreguntaEntity>

    @POST("api/actividades/{id}/preguntas")
    suspend fun crearPregunta(@Path("id") actividadId: Long, @Body pregunta: PreguntaEntity): PreguntaEntity

    @PUT("api/preguntas/{id}")
    suspend fun actualizarPregunta(@Path("id") id: Long, @Body pregunta: PreguntaEntity): PreguntaEntity

    @DELETE("api/preguntas/{id}")
    suspend fun eliminarPregunta(@Path("id") id: Long): Response<Unit>
}
