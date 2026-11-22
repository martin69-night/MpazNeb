package cl.duoc.mpazneb.estudiantes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de estudiantes de "Camino de Estrellas".
 * Guarda los estudiantes (apodo, curso, PIN con hash) y el historial de resultados de sus intentos.
 */
@SpringBootApplication
public class EstudiantesServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EstudiantesServiceApplication.class, args);
    }
}
