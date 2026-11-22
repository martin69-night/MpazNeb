package cl.duoc.mpazneb.estudiantes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entidad JPA = tabla "estudiantes". Mismos nombres de campo que EstudianteEntity de la app.
 * Datos ficticios: apodo (nunca RUT ni nombre real) y el PIN guardado como hash SHA-256.
 */
@Entity
@Table(name = "estudiantes")
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El apodo es obligatorio")
    @Size(min = 2, max = 20, message = "El apodo debe tener entre 2 y 20 caracteres")
    private String alias;

    @NotNull(message = "El curso es obligatorio")
    @Min(value = 1, message = "El curso va de 1 a 4")
    @Max(value = 4, message = "El curso va de 1 a 4")
    private Integer curso;

    @NotBlank(message = "El PIN es obligatorio")
    private String pinHash;

    /** Ruta de la foto del dibujo del personaje (queda en el teléfono). */
    private String fotoRuta;

    /** Observación pedagógica de la asistente. */
    @Size(max = 500, message = "La observación puede tener hasta 500 caracteres")
    private String observacion;

    public Estudiante() {
    }

    public Estudiante(String alias, Integer curso, String pinHash) {
        this.alias = alias;
        this.curso = curso;
        this.pinHash = pinHash;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public Integer getCurso() { return curso; }
    public void setCurso(Integer curso) { this.curso = curso; }
    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }
    public String getFotoRuta() { return fotoRuta; }
    public void setFotoRuta(String fotoRuta) { this.fotoRuta = fotoRuta; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
