package cl.duoc.mpazneb.estudiantes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de PATCH /api/estudiantes/{id}/observacion  →  {"observacion": "..."} */
public record ObservacionRequest(
        @NotBlank(message = "La observación es obligatoria")
        @Size(min = 10, max = 300, message = "La observación debe tener entre 10 y 300 caracteres")
        String observacion) {
}
