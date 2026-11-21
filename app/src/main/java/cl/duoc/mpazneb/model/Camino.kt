package cl.duoc.mpazneb.model

import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import cl.duoc.mpazneb.data.local.entity.ResultadoEntity




/** Un punto del camino del estudiante: la actividad y cómo le ha ido en ella. */
data class Paso(
    val actividad: ActividadEntity,
    /** Mejor resultado en estrellas (0 si nunca la ha hecho). */
    val estrellas: Int,
    val intentosUsados: Int,
    /** Se desbloquea al terminar la actividad anterior (progresión gradual). */
    val desbloqueada: Boolean
) {
    val terminada: Boolean get() = intentosUsados > 0
    val sinIntentos: Boolean get() = intentosUsados >= actividad.intentosPermitidos
}

/**
 * Arma el camino de un módulo para el curso del estudiante.
 * Reglas (estructuras de control):
 *  - solo actividades DISPONIBLES del curso y del módulo elegido,
 *  - ordenadas de la más fácil a la más difícil,
 *  - la primera siempre está abierta; las demás se abren al terminar la anterior.
 * Función pura: se prueba con JUnit.
 */
fun armarCamino(actividades: List<ActividadEntity>, resultados: List<ResultadoEntity>, curso: Int, modulo: Modulo): List<Paso> {
    val delCamino = actividades
        .filter { it.curso == curso && it.modulo == modulo && it.estado == EstadoActividad.DISPONIBLE }
        .sortedWith(compareBy<ActividadEntity>({ it.dificultad }, { it.id }))

    var anteriorTerminada = true
    return delCamino.map { actividad ->
        val propios = resultados.filter { it.actividadId == actividad.id }
        val paso = Paso(
            actividad = actividad,
            estrellas = propios.maxOfOrNull { it.estrellas() } ?: 0,
            intentosUsados = propios.size,
            desbloqueada = anteriorTerminada
        )
        anteriorTerminada = paso.terminada
        paso
    }
}

/** Estrellas totales: la mejor marca de cada actividad, sumadas. */
fun totalEstrellas(resultados: List<ResultadoEntity>): Int =
    resultados.groupBy { it.actividadId }.values.sumOf { intentos -> intentos.maxOf { it.estrellas() } }

/** Resumen de una asignatura para la tarjeta del menú del estudiante. */
data class AvanceAsignatura(
    val modulo: Modulo,
    val terminadas: Int,
    val total: Int,
    val estrellas: Int,
    /** Nombre de la próxima actividad por hacer, o null si terminó todas. */
    val siguiente: String?
) {
    /** Avance entre 0 y 1 para la barra de progreso. */
    val progreso: Float get() = if (total == 0) 0f else terminadas.toFloat() / total
}

/** Calcula el avance de una asignatura a partir de su camino. Función pura: se prueba con JUnit. */
fun avanceAsignatura(modulo: Modulo, camino: List<Paso>) = AvanceAsignatura(
    modulo = modulo,
    terminadas = camino.count { it.terminada },
    total = camino.size,
    estrellas = camino.sumOf { it.estrellas },
    siguiente = camino.firstOrNull { it.desbloqueada && !it.terminada }?.actividad?.nombre
)
