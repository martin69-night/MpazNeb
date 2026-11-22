package cl.duoc.mpazneb.estudiantes.controller;

import cl.duoc.mpazneb.estudiantes.dto.ErrorResponse;
import cl.duoc.mpazneb.estudiantes.exception.RecursoNoEncontradoException;
import cl.duoc.mpazneb.estudiantes.exception.ReglaNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convierte las excepciones en respuestas JSON claras que la app puede mostrar.
 */
@RestControllerAdvice
public class ManejadorErrores {

    /** 400: algún campo no cumple las validaciones (@NotBlank, @Min, ...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return new ErrorResponse("Hay datos inválidos", campos);
    }

    /** 404: no existe lo que se pidió. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noEncontrado(RecursoNoEncontradoException e) {
        return new ErrorResponse(e.getMessage(), Map.of());
    }

    /** 409: se rompió una regla del caso (apodo repetido en el curso). */
    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse reglaNegocio(ReglaNegocioException e) {
        return new ErrorResponse(e.getMessage(), Map.of());
    }
}
