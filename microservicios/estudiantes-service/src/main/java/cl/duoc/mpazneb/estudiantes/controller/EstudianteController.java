package cl.duoc.mpazneb.estudiantes.controller;

import cl.duoc.mpazneb.estudiantes.dto.ObservacionRequest;
import cl.duoc.mpazneb.estudiantes.model.Estudiante;
import cl.duoc.mpazneb.estudiantes.model.Resultado;
import cl.duoc.mpazneb.estudiantes.service.EstudianteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST de estudiantes. Base: http://localhost:8081/api/estudiantes
 */
@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService service;

    public EstudianteController(EstudianteService service) {
        this.service = service;
    }

    /** GET /api/estudiantes → todos los estudiantes. */
    @GetMapping
    public List<Estudiante> listar() {
        return service.listar();
    }

    /** GET /api/estudiantes/{id} → un estudiante. */
    @GetMapping("/{id}")
    public Estudiante obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    /** POST /api/estudiantes → registra un estudiante (409 si el apodo ya existe en el curso). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Estudiante crear(@Valid @RequestBody Estudiante estudiante) {
        return service.crear(estudiante);
    }

    /** PATCH /api/estudiantes/{id}/observacion → guarda la observación pedagógica. */
    @PatchMapping("/{id}/observacion")
    public Estudiante guardarObservacion(@PathVariable Long id, @Valid @RequestBody ObservacionRequest request) {
        return service.guardarObservacion(id, request.observacion());
    }

    /** DELETE /api/estudiantes/{id} → elimina al estudiante y su historial. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    /** GET /api/estudiantes/{id}/resultados → historial de intentos del estudiante. */
    @GetMapping("/{id}/resultados")
    public List<Resultado> resultados(@PathVariable Long id) {
        return service.resultadosDe(id);
    }

    /** POST /api/estudiantes/{id}/resultados → registra un intento terminado. */
    @PostMapping("/{id}/resultados")
    @ResponseStatus(HttpStatus.CREATED)
    public Resultado registrarResultado(@PathVariable Long id, @Valid @RequestBody Resultado resultado) {
        return service.registrarResultado(id, resultado);
    }
}
