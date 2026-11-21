package cl.duoc.mpazneb.actividades.repository;

import cl.duoc.mpazneb.actividades.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Acceso a la tabla "preguntas". */
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

    List<Pregunta> findByActividadIdOrderById(Long actividadId);

    long countByActividadId(Long actividadId);

    @Transactional
    void deleteByActividadId(Long actividadId);
}
