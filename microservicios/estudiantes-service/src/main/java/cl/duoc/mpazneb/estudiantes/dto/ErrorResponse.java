package cl.duoc.mpazneb.estudiantes.dto;

import java.util.Map;

/** Respuesta de error que lee la app: {"mensaje": "...", "campos": {"nombre": "..."}} */
public record ErrorResponse(String mensaje, Map<String, String> campos) {
}
