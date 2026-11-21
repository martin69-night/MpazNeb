package cl.duoc.mpazneb.actividades.dto;

import cl.duoc.mpazneb.actividades.model.EstadoActividad;
import jakarta.validation.constraints.NotNull;

/** Cuerpo de PATCH /api/actividades/{id}/estado  →  {"estado": "DISPONIBLE"} */
public record CambioEstadoRequest(@NotNull(message = "El estado es obligatorio") EstadoActividad estado) {
}
