package cl.duoc.mpazneb.data.remote

import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Error que devuelve el microservicio: {"mensaje": "...", "campos": {...}} */
data class ErrorApi(val mensaje: String? = null)

/** Si la respuesta no fue 2xx, lanza HttpException (igual que hace Retrofit con las otras funciones). */
fun Response<Unit>.exigirExito() {
    if (!isSuccessful) throw HttpException(this)
}

/**
 * Traduce un error de red a un mensaje para la docente:
 * - HttpException: el microservicio respondió con error (400, 404, 409) → se muestra su "mensaje".
 * - IOException: no se pudo conectar (microservicio apagado o sin adb reverse).
 */
fun mensajeDeError(e: Throwable): String = when (e) {
    is HttpException -> leerMensaje(e) ?: "El servidor respondió con error ${e.code()}"
    is IOException -> "No hay conexión con el microservicio. ¿Está encendido?"
    else -> "Ocurrió un error inesperado"
}

private fun leerMensaje(e: HttpException): String? = try {
    Gson().fromJson(e.response()?.errorBody()?.string(), ErrorApi::class.java)?.mensaje
} catch (_: Exception) {
    null
}
