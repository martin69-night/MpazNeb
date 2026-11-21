package cl.duoc.mpazneb

import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import cl.duoc.mpazneb.data.local.entity.ResultadoEntity
import cl.duoc.mpazneb.model.Dificultad
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.armarCamino
import cl.duoc.mpazneb.model.avanceAsignatura
import cl.duoc.mpazneb.model.calcularLogros
import cl.duoc.mpazneb.model.totalEstrellas
import cl.duoc.mpazneb.validacion.pinValido
import cl.duoc.mpazneb.validacion.validarActividad
import cl.duoc.mpazneb.validacion.validarEstudiante
import cl.duoc.mpazneb.validacion.validarLogin
import cl.duoc.mpazneb.validacion.validarObservacion
import cl.duoc.mpazneb.validacion.validarPregunta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas unitarias de la lógica pura (validaciones, camino y logros). Corren en la JVM, sin emulador. */
class LogicaTest {

    private fun actividad(id: Long, dificultad: Dificultad, estado: EstadoActividad = EstadoActividad.DISPONIBLE) = ActividadEntity(
        id = id, nombre = "A$id", modulo = Modulo.LECTURA, curso = 1, dificultad = dificultad,
        objetivo = "Objetivo de prueba", instrucciones = "Instrucciones", intentosPermitidos = 3, estado = estado
    )

    private fun resultado(actividadId: Long, aciertos: Int, total: Int = 3) =
        ResultadoEntity(estudianteId = 1, actividadId = actividadId, aciertos = aciertos, total = total, fecha = 0)

    // ---------- Validaciones ----------

    @Test
    fun login_vacio_marca_ambos_campos() {
        val errores = validarLogin("", "")
        assertTrue("alias" in errores && "clave" in errores)
    }

    @Test
    fun pin_debe_tener_cuatro_numeros() {
        assertTrue(pinValido("1234"))
        assertFalse(pinValido("12a4"))
        assertFalse(pinValido("123"))
    }

    @Test
    fun estudiante_con_pin_distinto_y_sin_curso() {
        val errores = validarEstudiante("Luna", null, "1234", "4321")
        assertEquals(setOf("curso", "confirmacion"), errores.keys)
    }

    @Test
    fun estudiante_rechaza_rut_como_apodo() {
        assertTrue("alias" in validarEstudiante("12.345.678-5", 1, "1234", "1234"))
    }

    @Test
    fun actividad_de_lectura_exige_texto() {
        val errores = validarActividad("Cuento", "LECTURA", 1, "INICIAL", "Identificar personajes", "Lee y responde", "3", true, "")
        assertEquals(setOf("textoLectura"), errores.keys)
    }

    @Test
    fun pregunta_con_alternativas_repetidas() {
        val errores = validarPregunta("2 + 2 = ?", listOf("4", "5", "4"), 0, "Dos más dos son cuatro")
        assertEquals(setOf("alternativas"), errores.keys)
    }

    @Test
    fun observacion_corta_es_invalida() {
        assertTrue("observacion" in validarObservacion("Bien"))
    }

    // ---------- Camino, estrellas y logros ----------

    @Test
    fun estrellas_segun_aciertos() {
        assertEquals(3, resultado(1, 3).estrellas())
        assertEquals(2, resultado(1, 2).estrellas())
        assertEquals(1, resultado(1, 0).estrellas())
    }

    @Test
    fun camino_se_desbloquea_en_orden_y_omite_borradores() {
        val actividades = listOf(
            actividad(1, Dificultad.INTERMEDIA),
            actividad(2, Dificultad.INICIAL),
            actividad(3, Dificultad.AVANZADA, EstadoActividad.BORRADOR)
        )
        val camino = armarCamino(actividades, listOf(resultado(2, 3)), curso = 1, modulo = Modulo.LECTURA)
        assertEquals(listOf(2L, 1L), camino.map { it.actividad.id }) // la inicial primero, sin el borrador
        assertTrue(camino[1].desbloqueada) // se abrió porque la anterior está terminada
        assertEquals(3, camino[0].estrellas)
    }

    @Test
    fun segunda_actividad_bloqueada_si_no_se_termina_la_primera() {
        val camino = armarCamino(listOf(actividad(1, Dificultad.INICIAL), actividad(2, Dificultad.INTERMEDIA)), emptyList(), 1, Modulo.LECTURA)
        assertTrue(camino[0].desbloqueada)
        assertFalse(camino[1].desbloqueada)
    }

    @Test
    fun avance_de_asignatura_cuenta_terminadas_y_siguiente() {
        val actividades = listOf(actividad(1, Dificultad.INICIAL), actividad(2, Dificultad.INTERMEDIA), actividad(3, Dificultad.AVANZADA))
        val camino = armarCamino(actividades, listOf(resultado(1, 2)), 1, Modulo.LECTURA)
        val avance = avanceAsignatura(Modulo.LECTURA, camino)
        assertEquals(1, avance.terminadas)
        assertEquals(3, avance.total)
        assertEquals(2, avance.estrellas)
        assertEquals("A2", avance.siguiente) // la siguiente desbloqueada sin terminar
    }

    @Test
    fun total_de_estrellas_usa_la_mejor_marca() {
        assertEquals(5, totalEstrellas(listOf(resultado(1, 1), resultado(1, 3), resultado(2, 2))))
    }

    @Test
    fun logro_primer_paso() {
        val logros = calcularLogros(listOf(resultado(1, 3)), listOf(actividad(1, Dificultad.INICIAL)))
        assertTrue(logros.first { it.nombre == "Primer paso" }.ganado)
        assertTrue(logros.first { it.nombre == "¡Perfecto!" }.ganado)
        assertFalse(logros.first { it.nombre == "Constancia" }.ganado)
    }
}
