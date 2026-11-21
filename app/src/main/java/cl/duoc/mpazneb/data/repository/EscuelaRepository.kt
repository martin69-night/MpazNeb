package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.AppDatabase
import cl.duoc.mpazneb.data.local.entity.EscuelaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio de la configuración de la escuela (ubicación GPS y radio permitido).
 */
class EscuelaRepository(private val db: AppDatabase) {

    /** Si todavía no hay fila, se usa la configuración por defecto. */
    fun observar(): Flow<EscuelaEntity> = db.escuelaDao.observe().map { it ?: EscuelaEntity() }

    private suspend fun actual(): EscuelaEntity = db.escuelaDao.get() ?: EscuelaEntity()

    suspend fun guardarUbicacion(latitud: Double, longitud: Double) =
        db.escuelaDao.save(actual().copy(latitud = latitud, longitud = longitud))

    suspend fun cambiarRadio(metros: Int) = db.escuelaDao.save(actual().copy(radioMetros = metros))

    suspend fun cambiarSoloEnEscuela(activo: Boolean) = db.escuelaDao.save(actual().copy(soloEnEscuela = activo))
}
