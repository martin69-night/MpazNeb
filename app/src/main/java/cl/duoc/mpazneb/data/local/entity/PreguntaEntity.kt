package cl.duoc.mpazneb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla "preguntas" de cada actividad.
 * KISS: siempre 3 alternativas (botones grandes para niños y una tabla simple).
 */
@Entity(tableName = "preguntas")
data class PreguntaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actividadId: Long,             // A qué actividad pertenece
    val enunciado: String,
    val alternativaA: String,
    val alternativaB: String,
    val alternativaC: String,
    val correcta: Int,                 // 0 = A, 1 = B, 2 = C
    val retroalimentacion: String      // Mensaje breve y amable que ve el estudiante
) {
    /** Las 3 alternativas como lista, para recorrerlas en la pantalla. */
    fun alternativas() = listOf(alternativaA, alternativaB, alternativaC)
}
