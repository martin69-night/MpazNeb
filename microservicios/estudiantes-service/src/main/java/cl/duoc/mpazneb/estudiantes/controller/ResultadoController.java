package cl.duoc.mpazneb.estudiantes.controller;

import cl.duoc.mpazneb.estudiantes.model.Resultado;
import cl.duoc.mpazneb.estudiantes.service.EstudianteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** GET /api/resultados → todos los intentos registrados (para reportes de la docente). */
@RestController
@RequestMapping("/api/resultados")
public class ResultadoController {

    private final EstudianteService service;

    public ResultadoController(EstudianteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Resultado> listar() {
        return service.listarResultados();
    }
}
