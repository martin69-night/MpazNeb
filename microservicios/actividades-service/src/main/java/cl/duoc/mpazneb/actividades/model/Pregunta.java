package cl.duoc.mpazneb.actividades.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad JPA = tabla "preguntas". Cada pregunta pertenece a una actividad (actividadId)
 * y tiene siempre 3 alternativas; "correcta" es 0 = A, 1 = B, 2 = C.
 */
@Entity
@Table(name = "preguntas")
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Relación con la tabla actividades (la llena el servicio con el id de la URL). */
    private Long actividadId;

    @NotBlank(message = "El enunciado es obligatorio")
    private String enunciado;

    @NotBlank(message = "La alternativa A es obligatoria")
    private String alternativaA;

    @NotBlank(message = "La alternativa B es obligatoria")
    private String alternativaB;

    @NotBlank(message = "La alternativa C es obligatoria")
    private String alternativaC;

    @NotNull(message = "Indica la alternativa correcta")
    @Min(value = 0, message = "La correcta debe ser 0, 1 o 2")
    @Max(value = 2, message = "La correcta debe ser 0, 1 o 2")
    private Integer correcta;

    @NotBlank(message = "La retroalimentación es obligatoria")
    private String retroalimentacion;

    public Pregunta() {
    }

    public Pregunta(Long actividadId, String enunciado, String alternativaA, String alternativaB, String alternativaC,
                    Integer correcta, String retroalimentacion) {
        this.actividadId = actividadId;
        this.enunciado = enunciado;
        this.alternativaA = alternativaA;
        this.alternativaB = alternativaB;
        this.alternativaC = alternativaC;
        this.correcta = correcta;
        this.retroalimentacion = retroalimentacion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getActividadId() { return actividadId; }
    public void setActividadId(Long actividadId) { this.actividadId = actividadId; }
    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }
    public String getAlternativaA() { return alternativaA; }
    public void setAlternativaA(String alternativaA) { this.alternativaA = alternativaA; }
    public String getAlternativaB() { return alternativaB; }
    public void setAlternativaB(String alternativaB) { this.alternativaB = alternativaB; }
    public String getAlternativaC() { return alternativaC; }
    public void setAlternativaC(String alternativaC) { this.alternativaC = alternativaC; }
    public Integer getCorrecta() { return correcta; }
    public void setCorrecta(Integer correcta) { this.correcta = correcta; }
    public String getRetroalimentacion() { return retroalimentacion; }
    public void setRetroalimentacion(String retroalimentacion) { this.retroalimentacion = retroalimentacion; }
}
