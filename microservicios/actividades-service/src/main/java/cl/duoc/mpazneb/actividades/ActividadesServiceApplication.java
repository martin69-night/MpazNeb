package cl.duoc.mpazneb.actividades;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de actividades de "Camino de Estrellas".
 * Expone una API REST que la app móvil consume con Retrofit (CRUD de actividades y preguntas).
 */
@SpringBootApplication
public class ActividadesServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ActividadesServiceApplication.class, args);
    }
}
