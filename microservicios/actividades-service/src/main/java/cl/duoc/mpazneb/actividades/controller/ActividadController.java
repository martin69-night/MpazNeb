package cl.duoc.mpazneb.actividades.controller;

import cl.duoc.mpazneb.actividades.dto.CambioEstadoRequest;
import cl.duoc.mpazneb.actividades.model.Actividad;
import cl.duoc.mpazneb.actividades.model.Pregunta;
import cl.duoc.mpazneb.actividades.service.ActividadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST de actividades. Base: http://localhost:8080/api/actividades
 */
@RestController
@RequestMapping("/api/actividades")
public class ActividadController {

    private final ActividadService service;

    public ActividadController(ActividadService service) {
        this.service = service;
    }

    /** GET /api/actividades → todas las actividades. */
    @GetMapping
    public List<Actividad> listar() {
        return service.listar();
    }

    /** GET /api/actividades/{id} → una actividad. */
    @GetMapping("/{id}")
    public Actividad obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    /** POST /api/actividades → crea una actividad (queda en BORRADOR). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Actividad crear(@Valid @RequestBody Actividad actividad) {
        return service.crear(actividad);
    }

    /** PUT /api/actividades/{id} → actualiza los datos de la actividad. */
    @PutMapping("/{id}")
    public Actividad actualizar(@PathVariable Long id, @Valid @RequestBody Actividad actividad) {
        return service.actualizar(id, actividad);
    }

    /** PATCH /api/actividades/{id}/estado → BORRADOR, DISPONIBLE o DESACTIVADA. */
    @PatchMapping("/{id}/estado")
    public Actividad cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest request) {
        return service.cambiarEstado(id, request.estado());
    }

    /** DELETE /api/actividades/{id} → elimina una actividad en borrador y sus preguntas. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    /** GET /api/actividades/{id}/preguntas → preguntas de la actividad. */
    @GetMapping("/{id}/preguntas")
    public List<Pregunta> preguntas(@PathVariable Long id) {
        return service.preguntasDe(id);
    }

    /** POST /api/actividades/{id}/preguntas → agrega una pregunta a la actividad. */
    @PostMapping("/{id}/preguntas")
    @ResponseStatus(HttpStatus.CREATED)
    public Pregunta crearPregunta(@PathVariable Long id, @Valid @RequestBody Pregunta pregunta) {
        return service.crearPregunta(id, pregunta);
    }
}
