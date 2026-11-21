package cl.duoc.mpazneb.actividades.exception;

/** Se lanza cuando no existe la actividad o pregunta pedida (respuesta HTTP 404). */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
