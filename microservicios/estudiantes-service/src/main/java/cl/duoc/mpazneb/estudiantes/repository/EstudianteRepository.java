package cl.duoc.mpazneb.estudiantes.repository;

import cl.duoc.mpazneb.estudiantes.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a la tabla "estudiantes". */
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    List<Estudiante> findAllByOrderByCursoAscAliasAsc();

    /** Regla: no puede haber dos estudiantes con el mismo apodo en un curso. */
    boolean existsByAliasIgnoreCaseAndCurso(String alias, Integer curso);
}
