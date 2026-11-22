package cl.duoc.mpazneb.estudiantes.exception;

/** Se lanza cuando no existe la estudiante pedida (respuesta HTTP 404). */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
