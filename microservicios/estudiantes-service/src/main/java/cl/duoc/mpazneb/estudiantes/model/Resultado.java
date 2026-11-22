package cl.duoc.mpazneb.estudiantes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad JPA = tabla "resultados": un registro por cada intento terminado de un estudiante.
 * Mismos nombres de campo que ResultadoEntity de la app.
 */
@Entity
@Table(name = "resultados")
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Relación con la tabla estudiantes (la llena el servicio con el id de la URL). */
    private Long estudianteId;

    /** Id de la actividad (vive en actividades-service). */
    @NotNull(message = "La actividad es obligatoria")
    private Long actividadId;

    @NotNull(message = "Los aciertos son obligatorios")
    @Min(value = 0, message = "Los aciertos no pueden ser negativos")
    private Integer aciertos;

    @NotNull(message = "El total es obligatorio")
    @Min(value = 1, message = "El total debe ser al menos 1")
    private Integer total;

    /** Fecha del intento en milisegundos (como en la app). */
    private Long fecha;

    public Resultado() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }
    public Long getActividadId() { return actividadId; }
    public void setActividadId(Long actividadId) { this.actividadId = actividadId; }
    public Integer getAciertos() { return aciertos; }
    public void setAciertos(Integer aciertos) { this.aciertos = aciertos; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public Long getFecha() { return fecha; }
    public void setFecha(Long fecha) { this.fecha = fecha; }
}
