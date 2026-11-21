package cl.duoc.mpazneb.actividades.repository;

import cl.duoc.mpazneb.actividades.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a la tabla "actividades". JpaRepository ya trae save, findById, findAll, deleteById, count... */
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    List<Actividad> findAllByOrderByCursoAscModuloAscNombreAsc();

    /** Para la regla "no puede haber dos actividades con el mismo nombre". */
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    boolean existsByNombreIgnoreCase(String nombre);
}
