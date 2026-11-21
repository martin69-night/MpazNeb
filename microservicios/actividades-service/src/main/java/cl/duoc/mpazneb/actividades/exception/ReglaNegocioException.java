package cl.duoc.mpazneb.actividades.exception;

/** Se lanza cuando una operación rompe una regla del caso (respuesta HTTP 409 Conflict). */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
