package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.AppDatabase
import cl.duoc.mpazneb.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de ingreso (adultos y estudiantes).
 * El ViewModel no sabe de SQL: le pide los datos al repositorio y el repositorio usa los DAO.
 */
class UsuarioRepository(private val db: AppDatabase) {

    /** Validación contra la base: devuelve el usuario si alias y contraseña coinciden; si no, null. */
    suspend fun iniciarSesion(alias: String, clave: String): UsuarioEntity? {
        val usuario = db.usuarioDao.buscarPorAlias(alias.trim().lowercase())
        if (usuario == null || usuario.claveHash != hashSha256(clave)) return null
        Sesion.usuario = usuario
        return usuario
    }

    /** Validación contra la base: true si el PIN coincide con el guardado para ese estudiante. */
    suspend fun pinCorrecto(estudianteId: Long, pin: String): Boolean =
        db.estudianteDao.getById(estudianteId)?.pinHash == hashSha256(pin)

    /** El profesor/a del curso del estudiante (para mostrar su nombre y foto). */
    fun observarDocenteDe(estudianteId: Long): Flow<UsuarioEntity?> = db.usuarioDao.observeDocenteDe(estudianteId)

    fun observar(id: Long): Flow<UsuarioEntity?> = db.usuarioDao.observeById(id)

    /** Guarda la ruta de la nueva foto de perfil del usuario. */
    suspend fun cambiarFoto(usuario: UsuarioEntity, fotoRuta: String) {
        val actualizado = usuario.copy(fotoRuta = fotoRuta)
        db.usuarioDao.update(actualizado)
        if (Sesion.usuario?.id == usuario.id) Sesion.usuario = actualizado
    }

    fun cerrarSesion() {
        Sesion.usuario = null
    }
}
