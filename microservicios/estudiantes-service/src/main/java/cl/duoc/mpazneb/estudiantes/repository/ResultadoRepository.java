package cl.duoc.mpazneb.estudiantes.repository;

import cl.duoc.mpazneb.estudiantes.model.Resultado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Acceso a la tabla "resultados". */
public interface ResultadoRepository extends JpaRepository<Resultado, Long> {

    List<Resultado> findByEstudianteIdOrderByFechaDesc(Long estudianteId);

    @Transactional
    void deleteByEstudianteId(Long estudianteId);
}
