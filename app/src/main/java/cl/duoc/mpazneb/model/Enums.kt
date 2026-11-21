package cl.duoc.mpazneb.model

// Room guarda los enum como texto (su nombre) sin configuración extra.

enum class Perfil(val etiqueta: String) {
    DOCENTE("Docente / UTP"),
    ASISTENTE("Asistente de aula")
}

enum class Modulo(val etiqueta: String, val emoji: String) {
    LECTURA("Lectura", "📖"),
    MATEMATICA("Matemática", "🔢")
}

/** El orden de las constantes es el orden del camino: primero Inicial, al final Avanzada. */
enum class Dificultad(val etiqueta: String) {
    INICIAL("Inicial"),
    INTERMEDIA("Intermedia"),
    AVANZADA("Avanzada")
}

enum class EstadoActividad(val etiqueta: String) {
    BORRADOR("Borrador"),
    DISPONIBLE("Disponible"),
    DESACTIVADA("Desactivada")
}

/** El MVP considera de 1.º a 4.º básico. */
val CURSOS = listOf(1, 2, 3, 4)

fun etiquetaCurso(curso: Int) = "$curso.º básico"
