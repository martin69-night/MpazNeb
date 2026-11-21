package cl.duoc.mpazneb.actividades.controller;

import cl.duoc.mpazneb.actividades.model.Pregunta;
import cl.duoc.mpazneb.actividades.service.ActividadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST de preguntas. Base: http://localhost:8080/api/preguntas
 * (Crear una pregunta va en ActividadController: POST /api/actividades/{id}/preguntas)
 */
@RestController
@RequestMapping("/api/preguntas")
public class PreguntaController {

    private final ActividadService service;

    public PreguntaController(ActividadService service) {
        this.service = service;
    }

    /** GET /api/preguntas → todas las preguntas (la app las usa para sincronizar). */
    @GetMapping
    public List<Pregunta> listar() {
        return service.listarPreguntas();
    }

    /** PUT /api/preguntas/{id} → actualiza una pregunta. */
    @PutMapping("/{id}")
    public Pregunta actualizar(@PathVariable Long id, @Valid @RequestBody Pregunta pregunta) {
        return service.actualizarPregunta(id, pregunta);
    }

    /** DELETE /api/preguntas/{id} → elimina una pregunta. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminarPregunta(id);
    }
}
