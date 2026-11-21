package cl.duoc.mpazneb.validacion

/*
 * VALIDACIONES LOCALES: funciones puras (sin Android ni Compose), separadas de la interfaz.
 * Cada una recibe lo que escribió el usuario y devuelve un mapa  campo -> mensaje de error.
 * Mapa vacío = formulario válido. Las validaciones CONTRA LA BASE DE DATOS (nombre repetido,
 * PIN correcto…) las hace el ViewModel con el repositorio, después de estas.
 * Se prueban con JUnit en app/src/test.
 */

/** Regla común: texto obligatorio con largo mínimo y máximo. */
private fun MutableMap<String, String>.texto(campo: String, valor: String, min: Int, max: Int) {
    when {
        valor.isBlank() -> put(campo, "Este campo es obligatorio")
        valor.trim().length !in min..max -> put(campo, "Debe tener entre $min y $max caracteres")
    }
}

/** El caso prohíbe datos personales reales: se rechaza todo lo que parezca un RUT. */
fun pareceRut(texto: String): Boolean = Regex("""\d{1,2}\.?\d{3}\.?\d{3}-?[\dkK]""").containsMatchIn(texto)

fun validarLogin(alias: String, clave: String): Map<String, String> = buildMap {
    texto("alias", alias, 4, 30)
    if (clave.isEmpty()) put("clave", "Este campo es obligatorio")
    else if (clave.length < 6) put("clave", "Mínimo 6 caracteres")
}

/** El PIN del estudiante: exactamente 4 números. */
fun pinValido(pin: String): Boolean = pin.length == 4 && pin.all { it.isDigit() }

fun validarEstudiante(alias: String, curso: Int?, pin: String, confirmacion: String): Map<String, String> = buildMap {
    texto("alias", alias, 2, 15)
    if ("alias" !in this && pareceRut(alias)) put("alias", "No uses RUT: escribe un apodo ficticio")
    if ("alias" !in this && alias.any { it.isDigit() }) put("alias", "El apodo no lleva números")
    if (curso == null) put("curso", "Selecciona un curso")
    if (!pinValido(pin)) put("pin", "El PIN tiene 4 números")
    if (pin != confirmacion) put("confirmacion", "Los PIN no coinciden")
}

fun validarActividad(
    nombre: String,
    modulo: Any?,
    curso: Int?,
    dificultad: Any?,
    objetivo: String,
    instrucciones: String,
    intentos: String,
    esLectura: Boolean,
    textoLectura: String
): Map<String, String> = buildMap {
    texto("nombre", nombre, 4, 40)
    if (modulo == null) put("modulo", "Selecciona un módulo")
    if (curso == null) put("curso", "Selecciona un curso")
    if (dificultad == null) put("dificultad", "Selecciona una dificultad")
    texto("objetivo", objetivo, 10, 200)
    texto("instrucciones", instrucciones, 10, 150)
    val numero = intentos.toIntOrNull()
    if (numero == null || numero !in 1..5) put("intentos", "Escribe un número entre 1 y 5")
    if (esLectura) texto("textoLectura", textoLectura, 30, 1000)
}

fun validarPregunta(enunciado: String, alternativas: List<String>, correcta: Int?, retroalimentacion: String): Map<String, String> =
    buildMap {
        texto("enunciado", enunciado, 3, 150)
        alternativas.forEachIndexed { i, alternativa -> texto("alternativa$i", alternativa, 1, 60) }
        val distintas = alternativas.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
        if (distintas.size < alternativas.count { it.isNotBlank() }) put("alternativas", "Hay alternativas repetidas")
        if (correcta == null) put("correcta", "Marca cuál es la respuesta correcta")
        texto("retroalimentacion", retroalimentacion, 5, 150)
    }

fun validarObservacion(texto: String): Map<String, String> = buildMap {
    texto("observacion", texto, 10, 300)
    if ("observacion" !in this && pareceRut(texto)) put("observacion", "No incluyas RUT ni datos personales")
}
