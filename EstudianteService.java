package cl.duoc.mpazneb.estudiantes.service;

import cl.duoc.mpazneb.estudiantes.exception.RecursoNoEncontradoException;
import cl.duoc.mpazneb.estudiantes.exception.ReglaNegocioException;
import cl.duoc.mpazneb.estudiantes.model.Estudiante;
import cl.duoc.mpazneb.estudiantes.model.Resultado;
import cl.duoc.mpazneb.estudiantes.repository.EstudianteRepository;
import cl.duoc.mpazneb.estudiantes.repository.ResultadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio del microservicio de estudiantes: aplica las reglas del caso
 * y usa los repositorios JPA para leer y guardar.
 */
@Service
public class EstudianteService {

    private final EstudianteRepository estudiantes;
    private final ResultadoRepository resultados;

    public EstudianteService(EstudianteRepository estudiantes, ResultadoRepository resultados) {
        this.estudiantes = estudiantes;
        this.resultados = resultados;
    }

    // ---------------- Estudiantes ----------------

    public List<Estudiante> listar() {
        return estudiantes.findAllByOrderByCursoAscAliasAsc();
    }

    public Estudiante obtener(Long id) {
        return estudiantes.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el estudiante " + id));
    }

    /** Regla: no puede haber dos estudiantes con el mismo apodo en el mismo curso. */
    public Estudiante crear(Estudiante estudiante) {
        String alias = estudiante.getAlias().trim();
        if (estudiantes.existsByAliasIgnoreCaseAndCurso(alias, estudiante.getCurso())) {
            throw new ReglaNegocioException("Ya hay un/a " + alias + " en ese curso. Usa otro apodo.");
        }
        estudiante.setId(null);
        estudiante.setAlias(alias);
        return estudiantes.save(estudiante);
    }

    /** Guarda la observación pedagógica del estudiante. */
    public Estudiante guardarObservacion(Long id, String observacion) {
        Estudiante actual = obtener(id);
        actual.setObservacion(observacion.trim());
        return estudiantes.save(actual);
    }

    /** Elimina al estudiante y también su historial de resultados. */
    @Transactional
    public void eliminar(Long id) {
        Estudiante actual = obtener(id);
        resultados.deleteByEstudianteId(id);
        estudiantes.delete(actual);
    }

    // ---------------- Resultados (intentos) ----------------

    public List<Resultado> listarResultados() {
        return resultados.findAll();
    }

    public List<Resultado> resultadosDe(Long estudianteId) {
        obtener(estudianteId); // 404 si el estudiante no existe
        return resultados.findByEstudianteIdOrderByFechaDesc(estudianteId);
    }

    /** Regla: los aciertos no pueden ser más que el total de preguntas. */
    public Resultado registrarResultado(Long estudianteId, Resultado resultado) {
        obtener(estudianteId);
        if (resultado.getAciertos() > resultado.getTotal()) {
            throw new ReglaNegocioException("Los aciertos no pueden ser más que el total de preguntas.");
        }
        resultado.setId(null);
        resultado.setEstudianteId(estudianteId);
        if (resultado.getFecha() == null) resultado.setFecha(System.currentTimeMillis());
        return resultados.save(resultado);
    }
}
