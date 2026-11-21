package cl.duoc.mpazneb.data.local

import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import cl.duoc.mpazneb.data.local.entity.EscuelaEntity
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.data.local.entity.PreguntaEntity
import cl.duoc.mpazneb.data.local.entity.UsuarioEntity
import cl.duoc.mpazneb.data.repository.hashSha256
import cl.duoc.mpazneb.model.Dificultad
import cl.duoc.mpazneb.model.Dificultad.AVANZADA
import cl.duoc.mpazneb.model.Dificultad.INICIAL
import cl.duoc.mpazneb.model.Dificultad.INTERMEDIA
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Modulo.LECTURA
import cl.duoc.mpazneb.model.Modulo.MATEMATICA
import cl.duoc.mpazneb.model.Perfil





const val CLAVE_DEMO = "Mpaz2026"
const val PIN_DEMO = "1234"

/**
 * Llena la base de datos con datos FICTICIOS la primera vez que se abre la app.
 * Los textos y ejercicios son de elaboración propia.
 */
suspend fun cargarDatosIniciales(db: AppDatabase) {
    if (db.usuarioDao.contar() > 0) return

    // ---------- Adultos ----------
    // Un docente a cargo de cada curso (nombres ficticios) y una asistente sin curso.
    val clave = hashSha256(CLAVE_DEMO)
    db.usuarioDao.insert(UsuarioEntity(alias = "docente.demo", claveHash = clave, perfil = Perfil.DOCENTE, nombre = "Profesora Carmen", curso = 1))
    db.usuarioDao.insert(UsuarioEntity(alias = "docente2.demo", claveHash = clave, perfil = Perfil.DOCENTE, nombre = "Profesor Andrés", curso = 2))
    db.usuarioDao.insert(UsuarioEntity(alias = "docente3.demo", claveHash = clave, perfil = Perfil.DOCENTE, nombre = "Profesora Isabel", curso = 3))
    db.usuarioDao.insert(UsuarioEntity(alias = "docente4.demo", claveHash = clave, perfil = Perfil.DOCENTE, nombre = "Profesor Felipe", curso = 4))
    db.usuarioDao.insert(UsuarioEntity(alias = "asistente.demo", claveHash = clave, perfil = Perfil.ASISTENTE, nombre = "Asistente Rosa"))

    // ---------- Estudiantes (apodos ficticios, PIN 1234) ----------
    listOf("Luna" to 1, "Benja" to 1, "Cami" to 2, "Tomi" to 2, "Pía" to 3, "Nico" to 4).forEach { (alias, curso) ->
        db.estudianteDao.insert(EstudianteEntity(alias = alias, curso = curso, pinHash = hashSha256(PIN_DEMO)))
    }

    // ---------- Escuela (ubicación referencial; la asistente la fija con el GPS) ----------
    db.escuelaDao.save(EscuelaEntity(latitud = -33.5922, longitud = -70.6996))

    // ---------- 1.º básico · Lectura ----------
    actividad(db, "El gato Tomás", LECTURA, 1, INICIAL,
        "Extraer información explícita de un texto breve.",
        "Lee el cuento con calma y toca la respuesta correcta.",
        texto = "Tomás es un gato gris. Vive en una casa con un patio grande. " +
            "En las mañanas toma sol junto a la ventana. En las tardes juega con una pelota roja.",
        preguntas = listOf(
            P("¿De qué color es Tomás?", "Gris", "Negro", "Blanco", 0, "El cuento dice: «Tomás es un gato gris»."),
            P("¿Con qué juega Tomás en las tardes?", "Con una caja", "Con una pelota roja", "Con un ratón", 1, "La última oración dice que juega con una pelota roja."),
            P("¿Dónde toma sol Tomás?", "En la cama", "En el techo", "Junto a la ventana", 2, "En las mañanas toma sol junto a la ventana.")
        ))
    actividad(db, "Un día de lluvia", LECTURA, 1, INTERMEDIA,
        "Inferir información a partir de pistas del texto.",
        "Lee el texto y piensa en las pistas antes de responder.",
        texto = "Ana miró por la ventana. El cielo estaba gris y caían gotas por todas partes. " +
            "Se puso sus botas amarillas y tomó su paraguas. En el camino saltó en cada charco.",
        preguntas = listOf(
            P("¿Qué tiempo hacía?", "Hacía sol", "Estaba lloviendo", "Estaba nevando", 1, "Caían gotas y había charcos: estaba lloviendo."),
            P("¿Por qué Ana tomó su paraguas?", "Para no mojarse", "Para jugar", "Para regalarlo", 0, "El paraguas sirve para no mojarse con la lluvia."),
            P("¿Qué es un «charco»?", "Un árbol pequeño", "Agua acumulada en el suelo", "Un tipo de bota", 1, "Un charco es agua de lluvia que queda en el suelo.")
        ))
    actividad(db, "Palabras que riman", LECTURA, 1, AVANZADA,
        "Reconocer sonidos finales iguales en palabras (rima).",
        "Lee cada palabra en voz baja y busca la que suena igual al final.",
        preguntas = listOf(
            P("¿Qué palabra rima con «gato»?", "Pato", "Perro", "Sol", 0, "Ga-to y pa-to terminan igual: -ato."),
            P("¿Qué palabra rima con «flor»?", "Mesa", "Calor", "Luna", 1, "Fl-or y cal-or terminan igual: -or."),
            P("¿Qué palabra rima con «ratón»?", "Casa", "Árbol", "Botón", 2, "Ra-tón y bo-tón terminan igual: -ón.")
        ))

    // ---------- 1.º básico · Matemática ----------
    actividad(db, "Sumas hasta 10", MATEMATICA, 1, INICIAL,
        "Resolver adiciones con resultado hasta 10.",
        "Resuelve cada suma. Puedes usar tus dedos para contar.",
        preguntas = listOf(
            P("3 + 4 = ?", "6", "7", "8", 1, "Parte en 3 y cuenta 4 más: 4, 5, 6, 7."),
            P("5 + 5 = ?", "10", "9", "11", 0, "Cinco y cinco son diez, como los dedos de tus dos manos."),
            P("2 + 6 = ?", "7", "9", "8", 2, "Parte en 6 y cuenta 2 más: 7, 8.")
        ))
    actividad(db, "Restas hasta 10", MATEMATICA, 1, INTERMEDIA,
        "Resolver sustracciones con números hasta 10.",
        "Resuelve cada resta. Puedes contar hacia atrás.",
        preguntas = listOf(
            P("9 − 3 = ?", "6", "5", "7", 0, "Desde 9 cuenta 3 hacia atrás: 8, 7, 6."),
            P("7 − 2 = ?", "4", "5", "6", 1, "Desde 7 cuenta 2 hacia atrás: 6, 5."),
            P("10 − 4 = ?", "5", "7", "6", 2, "Desde 10 cuenta 4 hacia atrás: 9, 8, 7, 6.")
        ))
    actividad(db, "Problemas de la feria", MATEMATICA, 1, AVANZADA,
        "Resolver problemas simples de suma y resta.",
        "Lee el problema, piensa si se junta o se quita, y responde.",
        preguntas = listOf(
            P("Ana compra 4 manzanas y luego 3 más. ¿Cuántas tiene?", "7", "6", "1", 0, "Se juntan manzanas: 4 + 3 = 7."),
            P("Había 8 globos y se volaron 5. ¿Cuántos quedan?", "13", "3", "4", 1, "Se quitan globos: 8 − 5 = 3."),
            P("Pedro tiene 6 lápices y le regalan 4. ¿Cuántos tiene?", "2", "9", "10", 2, "Se juntan lápices: 6 + 4 = 10.")
        ))

    // ---------- 2.º básico ----------
    actividad(db, "La huerta del curso", LECTURA, 2, INICIAL,
        "Reconocer la secuencia de acciones de un texto.",
        "Lee el texto completo. Puedes volver a verlo durante las preguntas.",
        texto = "El 2.º B hizo una huerta en un rincón de la escuela. Primero limpiaron la tierra. " +
            "Luego plantaron lechugas y zanahorias. Cada grupo regaba los lunes, miércoles y viernes. " +
            "Cuando cosecharon las lechugas, prepararon una ensalada para todo el curso.",
        preguntas = listOf(
            P("¿Qué hicieron primero?", "Plantaron lechugas", "Limpiaron la tierra", "Prepararon una ensalada", 1, "El texto usa la palabra «Primero» antes de limpiar la tierra."),
            P("¿Qué días regaban?", "Lunes, miércoles y viernes", "Todos los días", "Solo los sábados", 0, "Cada grupo regaba los lunes, miércoles y viernes."),
            P("¿Qué significa «cosechar»?", "Comprar verduras", "Regar la tierra", "Recoger lo que se plantó", 2, "Cosechar es recoger lo que creció.")
        ))
    actividad(db, "Sumas hasta 100", MATEMATICA, 2, INICIAL,
        "Resolver adiciones sin reserva hasta 100.",
        "Suma primero las unidades y después las decenas.",
        preguntas = listOf(
            P("23 + 14 = ?", "37", "36", "47", 0, "Unidades: 3 + 4 = 7. Decenas: 2 + 1 = 3. Resultado: 37."),
            P("40 + 30 = ?", "60", "70", "80", 1, "4 decenas más 3 decenas son 7 decenas: 70."),
            P("52 + 25 = ?", "75", "87", "77", 2, "Unidades: 2 + 5 = 7. Decenas: 5 + 2 = 7. Resultado: 77.")
        ))
    actividad(db, "Restas hasta 100", MATEMATICA, 2, INTERMEDIA,
        "Resolver sustracciones sin reserva hasta 100.",
        "Resta primero las unidades y después las decenas.",
        preguntas = listOf(
            P("45 − 12 = ?", "33", "23", "57", 0, "Unidades: 5 − 2 = 3. Decenas: 4 − 1 = 3. Resultado: 33."),
            P("En el bus van 38 personas y se bajan 15. ¿Cuántas quedan?", "53", "23", "13", 1, "Se bajan personas, entonces restamos: 38 − 15 = 23."),
            P("90 − 40 = ?", "40", "60", "50", 2, "9 decenas menos 4 decenas son 5 decenas: 50.")
        ))

    // ---------- 3.º y 4.º básico ----------
    actividad(db, "El faro", LECTURA, 3, INICIAL,
        "Comprender información explícita e implícita de un texto informativo.",
        "Lee con atención. Algunas respuestas no están escritas tal cual: piensa en las pistas.",
        texto = "Un faro es una torre alta que se construye cerca del mar. En la noche, su luz gira y " +
            "avisa a los barcos dónde hay rocas o costa. Antes, una persona llamada farero vivía en el faro " +
            "para encender la luz cada tarde. Hoy muchos faros funcionan de forma automática.",
        preguntas = listOf(
            P("¿Para qué sirve la luz de un faro?", "Para iluminar la playa", "Para avisar a los barcos", "Para decorar", 1, "El texto dice que la luz avisa a los barcos dónde hay rocas o costa."),
            P("¿Por qué hoy se necesitan menos fareros?", "Porque los faros son automáticos", "Porque no hay barcos", "Porque no hay mar", 0, "Muchos faros funcionan solos, sin que alguien encienda la luz."),
            P("¿Cuándo es más útil un faro?", "Al mediodía", "En la noche", "Cuando hace calor", 1, "En la noche los barcos no ven bien la costa.")
        ))
    actividad(db, "Tablas del 2 y del 5", MATEMATICA, 3, INICIAL,
        "Aplicar las tablas del 2 y del 5.",
        "Piensa en grupos iguales para resolver.",
        estado = EstadoActividad.BORRADOR, // en borrador, para mostrar los estados
        preguntas = listOf(
            P("2 × 7 = ?", "14", "9", "12", 0, "7 grupos de 2 son 14."),
            P("Hay 4 bolsas con 5 manzanas cada una. ¿Cuántas manzanas hay?", "9", "20", "25", 1, "Son 4 grupos de 5: 4 × 5 = 20.")
        ))
    actividad(db, "Multiplicar y dividir", MATEMATICA, 4, INICIAL,
        "Resolver multiplicaciones y divisiones simples.",
        "Recuerda: dividir es repartir en partes iguales.",
        preguntas = listOf(
            P("6 × 4 = ?", "24", "20", "28", 0, "6 grupos de 4: 4, 8, 12, 16, 20, 24."),
            P("Reparte 12 dulces entre 3 amigos. ¿Cuántos recibe cada uno?", "3", "4", "6", 1, "12 ÷ 3 = 4, porque 3 × 4 = 12."),
            P("9 × 3 = ?", "21", "18", "27", 2, "9 grupos de 3 son 27.")
        ))
}

/** Pregunta abreviada solo para escribir los datos iniciales más corto. */
private data class P(val enunciado: String, val a: String, val b: String, val c: String, val correcta: Int, val retro: String)

private suspend fun actividad(
    db: AppDatabase,
    nombre: String,
    modulo: Modulo,
    curso: Int,
    dificultad: Dificultad,
    objetivo: String,
    instrucciones: String,
    texto: String? = null,
    estado: EstadoActividad = EstadoActividad.DISPONIBLE,
    preguntas: List<P>
) {
    val id = db.actividadDao.insert(
        ActividadEntity(
            nombre = nombre, modulo = modulo, curso = curso, dificultad = dificultad, objetivo = objetivo,
            instrucciones = instrucciones, intentosPermitidos = 3, estado = estado, textoLectura = texto
        )
    )
    preguntas.forEach {
        db.preguntaDao.insert(
            PreguntaEntity(
                actividadId = id, enunciado = it.enunciado, alternativaA = it.a, alternativaB = it.b,
                alternativaC = it.c, correcta = it.correcta, retroalimentacion = it.retro
            )
        )
    }
}
