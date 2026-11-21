package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.entity.UsuarioEntity
import java.security.MessageDigest

/**
 * Usuario adulto con la sesión abierta. Vive en memoria (KISS):
 * al cerrar la app hay que volver a ingresar.
 */
object Sesion {
    var usuario: UsuarioEntity? = null
}

/** Hash SHA-256 de un texto: así contraseñas y PIN nunca se guardan tal cual. */
fun hashSha256(texto: String): String =
    MessageDigest.getInstance("SHA-256").digest(texto.toByteArray()).joinToString("") { "%02x".format(it) }
