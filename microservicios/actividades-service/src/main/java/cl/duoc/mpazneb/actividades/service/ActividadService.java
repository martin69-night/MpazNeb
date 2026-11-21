package cl.duoc.mpazneb.actividades.service;

import cl.duoc.mpazneb.actividades.exception.RecursoNoEncontradoException;
import cl.duoc.mpazneb.actividades.exception.ReglaNegocioException;
import cl.duoc.mpazneb.actividades.model.Actividad;
import cl.duoc.mpazneb.actividades.model.EstadoActividad;
import cl.duoc.mpazneb.actividades.model.Pregunta;
import cl.duoc.mpazneb.actividades.repository.ActividadRepository;
import cl.duoc.mpazneb.actividades.repository.PreguntaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio del microservicio. El controlador solo recibe la petición y llama a este servicio;
 * el servicio aplica las reglas del caso y usa los repositorios (JPA) para leer y guardar.
 */
@Service
public class ActividadService {

    private final ActividadRepository actividades;
    private final PreguntaRepository preguntas;

    public ActividadService(ActividadRepository actividades, PreguntaRepository preguntas) {
        this.actividades = actividades;
        this.preguntas = preguntas;
    }

    // ---------------- Actividades ----------------

    public List<Actividad> listar() {
        return actividades.findAllByOrderByCursoAscModuloAscNombreAsc();
    }

    public Actividad obtener(Long id) {
        return actividades.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la actividad " + id));
    }

    /** Regla: una actividad nueva siempre parte en BORRADOR y su nombre no se puede repetir. */
    public Actividad crear(Actividad actividad) {
        if (actividades.existsByNombreIgnoreCase(actividad.getNombre().trim())) {
            throw new ReglaNegocioException("Ya existe una actividad con ese nombre");
        }
        actividad.setId(null);
        actividad.setNombre(actividad.getNombre().trim());
        actividad.setEstado(EstadoActividad.BORRADOR);
        return actividades.save(actividad);
    }

    /** Actualiza los datos (el estado se cambia con su propio endpoint). */
    public Actividad actualizar(Long id, Actividad datos) {
        Actividad actual = obtener(id);
        if (actividades.existsByNombreIgnoreCaseAndIdNot(datos.getNombre().trim(), id)) {
            throw new ReglaNegocioException("Ya existe una actividad con ese nombre");
        }
        actual.setNombre(datos.getNombre().trim());
        actual.setModulo(datos.getModulo());
        actual.setCurso(datos.getCurso());
        actual.setDificultad(datos.getDificultad());
        actual.setObjetivo(datos.getObjetivo());
        actual.setInstrucciones(datos.getInstrucciones());
        actual.setIntentosPermitidos(datos.getIntentosPermitidos());
        actual.setTextoLectura(datos.getTextoLectura());
        actual.setFotoRuta(datos.getFotoRuta());
        return actividades.save(actual);
    }

    /** Regla: una actividad sin preguntas no puede quedar DISPONIBLE para los estudiantes. */
    public Actividad cambiarEstado(Long id, EstadoActividad nuevo) {
        Actividad actual = obtener(id);
        if (nuevo == EstadoActividad.DISPONIBLE && preguntas.countByActividadId(id) == 0) {
            throw new ReglaNegocioException("Agrega al menos una pregunta antes de dejarla disponible.");
        }
        actual.setEstado(nuevo);
        return actividades.save(actual);
    }

    /** Regla: solo se eliminan actividades en BORRADOR (las otras pueden tener intentos de estudiantes). */
    @Transactional
    public void eliminar(Long id) {
        Actividad actual = obtener(id);
        if (actual.getEstado() != EstadoActividad.BORRADOR) {
            throw new ReglaNegocioException("Solo se pueden eliminar actividades en borrador.");
        }
        preguntas.deleteByActividadId(id);
        actividades.delete(actual);
    }

    // ---------------- Preguntas ----------------

    public List<Pregunta> listarPreguntas() {
        return preguntas.findAll();
    }

    public List<Pregunta> preguntasDe(Long actividadId) {
        obtener(actividadId); // 404 si la actividad no existe
        return preguntas.findByActividadIdOrderById(actividadId);
    }

    public Pregunta crearPregunta(Long actividadId, Pregunta pregunta) {
        obtener(actividadId);
        pregunta.setId(null);
        pregunta.setActividadId(actividadId);
        return preguntas.save(pregunta);
    }

    public Pregunta actualizarPregunta(Long id, Pregunta datos) {
        Pregunta actual = preguntas.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la pregunta " + id));
        actual.setEnunciado(datos.getEnunciado());
        actual.setAlternativaA(datos.getAlternativaA());
        actual.setAlternativaB(datos.getAlternativaB());
        actual.setAlternativaC(datos.getAlternativaC());
        actual.setCorrecta(datos.getCorrecta());
        actual.setRetroalimentacion(datos.getRetroalimentacion());
        return preguntas.save(actual);
    }

    public void eliminarPregunta(Long id) {
        if (!preguntas.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe la pregunta " + id);
        }
        preguntas.deleteById(id);
    }
}
