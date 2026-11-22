package cl.duoc.mpazneb.data.remote

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Pruebas de los mensajes que ve la docente cuando el microservicio falla. */
class ErroresTest : StringSpec({

    "un error 409 muestra el mensaje que envía el microservicio" {
        val respuesta = Response.error<Any>(409, """{"mensaje":"Ya existe una actividad con ese nombre"}""".toResponseBody())
        mensajeDeError(HttpException(respuesta)) shouldBe "Ya existe una actividad con ese nombre"
    }

    "un error sin mensaje muestra el código HTTP" {
        val respuesta = Response.error<Any>(500, "no es json".toResponseBody())
        mensajeDeError(HttpException(respuesta)) shouldBe "El servidor respondió con error 500"
    }

    "sin conexión avisa que el microservicio está apagado" {
        mensajeDeError(IOException()) shouldBe "No hay conexión con el microservicio. ¿Está encendido?"
    }

    "otro error muestra un mensaje genérico" {
        mensajeDeError(IllegalStateException()) shouldBe "Ocurrió un error inesperado"
    }

    "exigirExito() lanza HttpException si la respuesta no es 2xx" {
        shouldThrow<HttpException> { Response.error<Unit>(404, "".toResponseBody()).exigirExito() }
    }
})
