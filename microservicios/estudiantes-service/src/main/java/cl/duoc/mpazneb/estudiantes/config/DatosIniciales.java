package cl.duoc.mpazneb.estudiantes.config;

import cl.duoc.mpazneb.estudiantes.model.Estudiante;
import cl.duoc.mpazneb.estudiantes.repository.EstudianteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Carga los 6 estudiantes FICTICIOS de la app (mismo orden → mismos ids 1..6 que Room), todos con PIN 1234.
 * Solo se ejecuta si la tabla está vacía.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final EstudianteRepository estudiantes;

    public DatosIniciales(EstudianteRepository estudiantes) {
        this.estudiantes = estudiantes;
    }

    @Override
    public void run(String... args) {
        if (estudiantes.count() > 0) return;
        String pin = sha256("1234");
        estudiantes.save(new Estudiante("Luna", 1, pin));
        estudiantes.save(new Estudiante("Benja", 1, pin));
        estudiantes.save(new Estudiante("Cami", 2, pin));
        estudiantes.save(new Estudiante("Tomi", 2, pin));
        estudiantes.save(new Estudiante("Pía", 3, pin));
        estudiantes.save(new Estudiante("Nico", 4, pin));
    }

    /** Mismo hash que usa la app (hashSha256 en Kotlin): el PIN nunca se guarda tal cual. */
    static String sha256(String texto) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
