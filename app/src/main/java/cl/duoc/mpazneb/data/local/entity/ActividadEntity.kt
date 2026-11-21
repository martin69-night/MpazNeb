package cl.duoc.mpazneb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.mpazneb.model.Dificultad
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo

/**
 * Entidad de Room que representa la tabla "actividades" de Lectura y Matemática.
 */
@Entity(tableName = "actividades")
data class ActividadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val modulo: Modulo,                                  // LECTURA o MATEMATICA
    val curso: Int,                                      // 1 a 4 (básico)
    val dificultad: Dificultad,                          // Define el orden en el camino del estudiante
    val objetivo: String,                                // Objetivo pedagógico
    val instrucciones: String,                           // Instrucciones breves para el estudiante
    val intentosPermitidos: Int,
    val estado: EstadoActividad = EstadoActividad.BORRADOR, // Solo las DISPONIBLES las ve el estudiante
    val textoLectura: String? = null,                    // Solo Lectura: el texto que se lee
    val fotoRuta: String? = null                         // Foto (cámara) del material. Opcional
)
