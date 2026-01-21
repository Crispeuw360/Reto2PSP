package eus.tartanga.eus.PSP2EVA.service;

import eus.tartanga.eus.PSP2EVA.model.Game;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GameService {

    private final List<Game> games = new ArrayList<>();

    @PostConstruct
    public void init() {
        // Aquí podrías cargar los juegos desde un JSON o BBDD.
        // Para el ejemplo, dejamos algunos juegos de prueba y calculamos su hash al iniciar.
        games.add(new Game(
                1,
                "Ejemplo Game 1",
                "APKS/game1.apk",
                "images/game1.png",
                null
        ));
        games.add(new Game(
                2,
                "Ejemplo Game 2",
                "APKS/game2.apk",
                "images/game2.png",
                null
        ));

        // Calculamos y almacenamos el hash de todos los APK al arrancar el servidor.
        for (Game game : games) {
            calculateAndStoreApkHash(game);
        }
    }

    public List<Game> findAll() {
        return new ArrayList<>(games);
    }

    public Optional<Game> findById(int id) {
        return games.stream().filter(g -> g.getId() == id).findFirst();
    }

    public Resource getImageResource(Game game) {
        return new ClassPathResource(game.getImageUrl());
    }

    public Resource getApkResource(Game game) {
        return new ClassPathResource(game.getApkUrl());
    }

    /**
     * Calcula (y guarda en memoria) el hash SHA-256 del APK del juego.
     */
    public Optional<String> calculateAndStoreApkHash(Game game) {
        Resource apkResource = getApkResource(game);
        if (!apkResource.exists()) {
            return Optional.empty();
        }
        try (InputStream is = apkResource.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            String hash = sb.toString();
            game.setDownloadHash(hash);
            return Optional.of(hash);
        } catch (IOException | NoSuchAlgorithmException e) {
            return Optional.empty();
        }
    }
}

