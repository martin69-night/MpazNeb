package cl.duoc.mpazneb.model

import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import cl.duoc.mpazneb.data.local.entity.ResultadoEntity






/** Insignia que el estudiante puede ganar. */
data class Logro(val emoji: String, val nombre: String, val descripcion: String, val ganado: Boolean)

/**
 * Calcula las insignias a partir de los resultados guardados.
 * Es una función pura (sin Android), así se puede probar con JUnit.
 */
fun calcularLogros(resultados: List<ResultadoEntity>, actividades: List<ActividadEntity>): List<Logro> {
    val moduloDe = actividades.associate { it.id to it.modulo }
    val completadas = resultados.map { it.actividadId }.toSet()
    val lectura = completadas.count { moduloDe[it] == Modulo.LECTURA }
    val matematica = completadas.count { moduloDe[it] == Modulo.MATEMATICA }

    return listOf(
        Logro("🚀", "Primer paso", "Termina tu primera actividad", completadas.isNotEmpty()),
        Logro("📚", "Lector/a", "Termina 2 actividades de Lectura", lectura >= 2),
        Logro("🧮", "Matemágico/a", "Termina 2 actividades de Matemática", matematica >= 2),
        Logro("🌟", "¡Perfecto!", "Gana 3 estrellas en una actividad", resultados.any { it.estrellas() == 3 }),
        Logro("💪", "Constancia", "Practica 5 veces", resultados.size >= 5)
    )
}
