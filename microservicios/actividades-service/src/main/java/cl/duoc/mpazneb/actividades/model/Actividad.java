package cl.duoc.mpazneb.actividades.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Entidad JPA = tabla "actividades".
 * Los nombres de los campos son iguales a ActividadEntity de la app, así Retrofit (Gson) convierte el JSON directo.
 */
@Entity
@Table(name = "actividades")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 60, message = "El nombre debe tener entre 3 y 60 caracteres")
    private String nombre;

    @NotNull(message = "El módulo es obligatorio")
    @Enumerated(EnumType.STRING)
    private Modulo modulo;

    @NotNull(message = "El curso es obligatorio")
    @Min(value = 1, message = "El curso va de 1 a 4")
    @Max(value = 4, message = "El curso va de 1 a 4")
    private Integer curso;

    @NotNull(message = "La dificultad es obligatoria")
    @Enumerated(EnumType.STRING)
    private Dificultad dificultad;

    @NotBlank(message = "El objetivo es obligatorio")
    @Column(length = 500)
    private String objetivo;

    @NotBlank(message = "Las instrucciones son obligatorias")
    @Column(length = 500)
    private String instrucciones;

    @NotNull(message = "Los intentos son obligatorios")
    @Min(value = 1, message = "Debe permitir al menos 1 intento")
    @Max(value = 10, message = "Máximo 10 intentos")
    private Integer intentosPermitidos;

    @Enumerated(EnumType.STRING)
    private EstadoActividad estado = EstadoActividad.BORRADOR;

    /** Solo Lectura: el texto que lee el estudiante. */
    @Column(length = 2000)
    private String textoLectura;

    /** Ruta de la foto del material (queda en el teléfono de la docente). */
    private String fotoRuta;

    public Actividad() {
    }

    public Actividad(String nombre, Modulo modulo, Integer curso, Dificultad dificultad, String objetivo,
                     String instrucciones, Integer intentosPermitidos, EstadoActividad estado, String textoLectura) {
        this.nombre = nombre;
        this.modulo = modulo;
        this.curso = curso;
        this.dificultad = dificultad;
        this.objetivo = objetivo;
        this.instrucciones = instrucciones;
        this.intentosPermitidos = intentosPermitidos;
        this.estado = estado;
        this.textoLectura = textoLectura;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Modulo getModulo() { return modulo; }
    public void setModulo(Modulo modulo) { this.modulo = modulo; }
    public Integer getCurso() { return curso; }
    public void setCurso(Integer curso) { this.curso = curso; }
    public Dificultad getDificultad() { return dificultad; }
    public void setDificultad(Dificultad dificultad) { this.dificultad = dificultad; }
    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
    public String getInstrucciones() { return instrucciones; }
    public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }
    public Integer getIntentosPermitidos() { return intentosPermitidos; }
    public void setIntentosPermitidos(Integer intentosPermitidos) { this.intentosPermitidos = intentosPermitidos; }
    public EstadoActividad getEstado() { return estado; }
    public void setEstado(EstadoActividad estado) { this.estado = estado; }
    public String getTextoLectura() { return textoLectura; }
    public void setTextoLectura(String textoLectura) { this.textoLectura = textoLectura; }
    public String getFotoRuta() { return fotoRuta; }
    public void setFotoRuta(String fotoRuta) { this.fotoRuta = fotoRuta; }
}
