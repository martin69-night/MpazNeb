package cl.duoc.mpazneb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla "escuela": una sola fila (id = 1) con la configuración del GPS.
 */
@Entity(tableName = "escuela")
data class EscuelaEntity(
    @PrimaryKey
    val id: Int = 1,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val radioMetros: Int = 200,
    val soloEnEscuela: Boolean = false   // true = solo se practica dentro del radio de la escuela
)
