package cl.duoc.mpazneb.actividades.config;

import cl.duoc.mpazneb.actividades.model.Actividad;
import cl.duoc.mpazneb.actividades.model.Dificultad;
import cl.duoc.mpazneb.actividades.model.EstadoActividad;
import cl.duoc.mpazneb.actividades.model.Modulo;
import cl.duoc.mpazneb.actividades.model.Pregunta;
import cl.duoc.mpazneb.actividades.repository.ActividadRepository;
import cl.duoc.mpazneb.actividades.repository.PreguntaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Carga los datos FICTICIOS la primera vez que se levanta el microservicio (si la tabla está vacía).
 * Son las mismas 12 actividades (y 35 preguntas) que trae la app, en el mismo orden,
 * así los ids del microservicio (1..12) coinciden con los de Room en el teléfono.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final ActividadRepository actividades;
    private final PreguntaRepository preguntas;

    public DatosIniciales(ActividadRepository actividades, PreguntaRepository preguntas) {
        this.actividades = actividades;
        this.preguntas = preguntas;
    }

    @Override
    public void run(String... args) {
        if (actividades.count() > 0) return;

        // 1.º básico · Lectura
        crear("El gato Tomás", Modulo.LECTURA, 1, Dificultad.INICIAL,
                "Extraer información explícita de un texto breve.",
                "Lee el cuento con calma y toca la respuesta correcta.",
                "Tomás es un gato gris. Vive en una casa con un patio grande. En las mañanas toma sol junto a la ventana. En las tardes juega con una pelota roja.", EstadoActividad.DISPONIBLE,
                p("¿De qué color es Tomás?", "Gris", "Negro", "Blanco", 0, "El cuento dice: «Tomás es un gato gris»."),
                p("¿Con qué juega Tomás en las tardes?", "Con una caja", "Con una pelota roja", "Con un ratón", 1, "La última oración dice que juega con una pelota roja."),
                p("¿Dónde toma sol Tomás?", "En la cama", "En el techo", "Junto a la ventana", 2, "En las mañanas toma sol junto a la ventana."));

        // 1.º básico · Lectura
        crear("Un día de lluvia", Modulo.LECTURA, 1, Dificultad.INTERMEDIA,
                "Inferir información a partir de pistas del texto.",
                "Lee el texto y piensa en las pistas antes de responder.",
                "Ana miró por la ventana. El cielo estaba gris y caían gotas por todas partes. Se puso sus botas amarillas y tomó su paraguas. En el camino saltó en cada charco.", EstadoActividad.DISPONIBLE,
                p("¿Qué tiempo hacía?", "Hacía sol", "Estaba lloviendo", "Estaba nevando", 1, "Caían gotas y había charcos: estaba lloviendo."),
                p("¿Por qué Ana tomó su paraguas?", "Para no mojarse", "Para jugar", "Para regalarlo", 0, "El paraguas sirve para no mojarse con la lluvia."),
                p("¿Qué es un «charco»?", "Un árbol pequeño", "Agua acumulada en el suelo", "Un tipo de bota", 1, "Un charco es agua de lluvia que queda en el suelo."));

        // 1.º básico · Lectura
        crear("Palabras que riman", Modulo.LECTURA, 1, Dificultad.AVANZADA,
                "Reconocer sonidos finales iguales en palabras (rima).",
                "Lee cada palabra en voz baja y busca la que suena igual al final.",
                null, EstadoActividad.DISPONIBLE,
                p("¿Qué palabra rima con «gato»?", "Pato", "Perro", "Sol", 0, "Ga-to y pa-to terminan igual: -ato."),
                p("¿Qué palabra rima con «flor»?", "Mesa", "Calor", "Luna", 1, "Fl-or y cal-or terminan igual: -or."),
                p("¿Qué palabra rima con «ratón»?", "Casa", "Árbol", "Botón", 2, "Ra-tón y bo-tón terminan igual: -ón."));

        // 1.º básico · Matemática
        crear("Sumas hasta 10", Modulo.MATEMATICA, 1, Dificultad.INICIAL,
                "Resolver adiciones con resultado hasta 10.",
                "Resuelve cada suma. Puedes usar tus dedos para contar.",
                null, EstadoActividad.DISPONIBLE,
                p("3 + 4 = ?", "6", "7", "8", 1, "Parte en 3 y cuenta 4 más: 4, 5, 6, 7."),
                p("5 + 5 = ?", "10", "9", "11", 0, "Cinco y cinco son diez, como los dedos de tus dos manos."),
                p("2 + 6 = ?", "7", "9", "8", 2, "Parte en 6 y cuenta 2 más: 7, 8."));

        // 1.º básico · Matemática
        crear("Restas hasta 10", Modulo.MATEMATICA, 1, Dificultad.INTERMEDIA,
                "Resolver sustracciones con números hasta 10.",
                "Resuelve cada resta. Puedes contar hacia atrás.",
                null, EstadoActividad.DISPONIBLE,
                p("9 − 3 = ?", "6", "5", "7", 0, "Desde 9 cuenta 3 hacia atrás: 8, 7, 6."),
                p("7 − 2 = ?", "4", "5", "6", 1, "Desde 7 cuenta 2 hacia atrás: 6, 5."),
                p("10 − 4 = ?", "5", "7", "6", 2, "Desde 10 cuenta 4 hacia atrás: 9, 8, 7, 6."));

        // 1.º básico · Matemática
        crear("Problemas de la feria", Modulo.MATEMATICA, 1, Dificultad.AVANZADA,
                "Resolver problemas simples de suma y resta.",
                "Lee el problema, piensa si se junta o se quita, y responde.",
                null, EstadoActividad.DISPONIBLE,
                p("Ana compra 4 manzanas y luego 3 más. ¿Cuántas tiene?", "7", "6", "1", 0, "Se juntan manzanas: 4 + 3 = 7."),
                p("Había 8 globos y se volaron 5. ¿Cuántos quedan?", "13", "3", "4", 1, "Se quitan globos: 8 − 5 = 3."),
                p("Pedro tiene 6 lápices y le regalan 4. ¿Cuántos tiene?", "2", "9", "10", 2, "Se juntan lápices: 6 + 4 = 10."));

        // 2.º básico · Lectura
        crear("La huerta del curso", Modulo.LECTURA, 2, Dificultad.INICIAL,
                "Reconocer la secuencia de acciones de un texto.",
                "Lee el texto completo. Puedes volver a verlo durante las preguntas.",
                "El 2.º B hizo una huerta en un rincón de la escuela. Primero limpiaron la tierra. Luego plantaron lechugas y zanahorias. Cada grupo regaba los lunes, miércoles y viernes. Cuando cosecharon las lechugas, prepararon una ensalada para todo el curso.", EstadoActividad.DISPONIBLE,
                p("¿Qué hicieron primero?", "Plantaron lechugas", "Limpiaron la tierra", "Prepararon una ensalada", 1, "El texto usa la palabra «Primero» antes de limpiar la tierra."),
                p("¿Qué días regaban?", "Lunes, miércoles y viernes", "Todos los días", "Solo los sábados", 0, "Cada grupo regaba los lunes, miércoles y viernes."),
                p("¿Qué significa «cosechar»?", "Comprar verduras", "Regar la tierra", "Recoger lo que se plantó", 2, "Cosechar es recoger lo que creció."));

        // 2.º básico · Matemática
        crear("Sumas hasta 100", Modulo.MATEMATICA, 2, Dificultad.INICIAL,
                "Resolver adiciones sin reserva hasta 100.",
                "Suma primero las unidades y después las decenas.",
                null, EstadoActividad.DISPONIBLE,
                p("23 + 14 = ?", "37", "36", "47", 0, "Unidades: 3 + 4 = 7. Decenas: 2 + 1 = 3. Resultado: 37."),
                p("40 + 30 = ?", "60", "70", "80", 1, "4 decenas más 3 decenas son 7 decenas: 70."),
                p("52 + 25 = ?", "75", "87", "77", 2, "Unidades: 2 + 5 = 7. Decenas: 5 + 2 = 7. Resultado: 77."));

        // 2.º básico · Matemática
        crear("Restas hasta 100", Modulo.MATEMATICA, 2, Dificultad.INTERMEDIA,
                "Resolver sustracciones sin reserva hasta 100.",
                "Resta primero las unidades y después las decenas.",
                null, EstadoActividad.DISPONIBLE,
                p("45 − 12 = ?", "33", "23", "57", 0, "Unidades: 5 − 2 = 3. Decenas: 4 − 1 = 3. Resultado: 33."),
                p("En el bus van 38 personas y se bajan 15. ¿Cuántas quedan?", "53", "23", "13", 1, "Se bajan personas, entonces restamos: 38 − 15 = 23."),
                p("90 − 40 = ?", "40", "60", "50", 2, "9 decenas menos 4 decenas son 5 decenas: 50."));

        // 3.º básico · Lectura
        crear("El faro", Modulo.LECTURA, 3, Dificultad.INICIAL,
                "Comprender información explícita e implícita de un texto informativo.",
                "Lee con atención. Algunas respuestas no están escritas tal cual: piensa en las pistas.",
                "Un faro es una torre alta que se construye cerca del mar. En la noche, su luz gira y avisa a los barcos dónde hay rocas o costa. Antes, una persona llamada farero vivía en el faro para encender la luz cada tarde. Hoy muchos faros funcionan de forma automática.", EstadoActividad.DISPONIBLE,
                p("¿Para qué sirve la luz de un faro?", "Para iluminar la playa", "Para avisar a los barcos", "Para decorar", 1, "El texto dice que la luz avisa a los barcos dónde hay rocas o costa."),
                p("¿Por qué hoy se necesitan menos fareros?", "Porque los faros son automáticos", "Porque no hay barcos", "Porque no hay mar", 0, "Muchos faros funcionan solos, sin que alguien encienda la luz."),
                p("¿Cuándo es más útil un faro?", "Al mediodía", "En la noche", "Cuando hace calor", 1, "En la noche los barcos no ven bien la costa."));

        // 3.º básico · Matemática
        crear("Tablas del 2 y del 5", Modulo.MATEMATICA, 3, Dificultad.INICIAL,
                "Aplicar las tablas del 2 y del 5.",
                "Piensa en grupos iguales para resolver.",
                null, EstadoActividad.BORRADOR,
                p("2 × 7 = ?", "14", "9", "12", 0, "7 grupos de 2 son 14."),
                p("Hay 4 bolsas con 5 manzanas cada una. ¿Cuántas manzanas hay?", "9", "20", "25", 1, "Son 4 grupos de 5: 4 × 5 = 20."));

        // 4.º básico · Matemática
        crear("Multiplicar y dividir", Modulo.MATEMATICA, 4, Dificultad.INICIAL,
                "Resolver multiplicaciones y divisiones simples.",
                "Recuerda: dividir es repartir en partes iguales.",
                null, EstadoActividad.DISPONIBLE,
                p("6 × 4 = ?", "24", "20", "28", 0, "6 grupos de 4: 4, 8, 12, 16, 20, 24."),
                p("Reparte 12 dulces entre 3 amigos. ¿Cuántos recibe cada uno?", "3", "4", "6", 1, "12 ÷ 3 = 4, porque 3 × 4 = 12."),
                p("9 × 3 = ?", "21", "18", "27", 2, "9 grupos de 3 son 27."));
    }

    /** Pregunta abreviada (se completa con el id de la actividad en crear). */
    private Pregunta p(String enunciado, String a, String b, String c, int correcta, String retro) {
        return new Pregunta(null, enunciado, a, b, c, correcta, retro);
    }

    private void crear(String nombre, Modulo modulo, int curso, Dificultad dificultad, String objetivo,
                       String instrucciones, String texto, EstadoActividad estado, Pregunta... lista) {
        Actividad actividad = actividades.save(
                new Actividad(nombre, modulo, curso, dificultad, objetivo, instrucciones, 3, estado, texto));
        for (Pregunta pregunta : lista) {
            pregunta.setActividadId(actividad.getId());
            preguntas.save(pregunta);
        }
    }
}
