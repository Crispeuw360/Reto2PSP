package eus.tartanga.eus.PSP2EVA.controller;


import eus.tartanga.eus.PSP2EVA.model.Game;
import eus.tartanga.eus.PSP2EVA.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Listado de aplicaciones (GET)
     * Devuelve directamente la lista de juegos como JSON "por defecto".
     */
    @GetMapping
    public ResponseEntity<List<Game>> listGames() {
        List<Game> games = gameService.findAll();
        return ResponseEntity.ok(games);
    }

    /**
     * Obtención de imagen con curl (GET)
     * Descarga binaria de la imagen del juego.
     */
    @GetMapping("/image/{id}")
    public ResponseEntity<?> getGameImage(@PathVariable int id) {
        Optional<Game> optionalGame = gameService.findById(id);
        if (optionalGame.isEmpty()) {
            Map<String, Object> error = buildErrorBody(
                    HttpStatus.NOT_FOUND,
                    "El juego solicitado no fue encontrado"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Game game = optionalGame.get();
        Resource image = gameService.getImageResource(game);
        if (!image.exists()) {
            Map<String, Object> error = buildErrorBody(
                    HttpStatus.NOT_FOUND,
                    "La imagen asociada al juego no fue encontrada"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        String contentType = guessContentType(image);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(image);
    }

    /**
     * Descarga del APK con curl (GET)
     * Descarga binaria del fichero APK.
     */
    @GetMapping("/apk/{id}")
    public ResponseEntity<?> downloadApk(@PathVariable int id) {
        Optional<Game> optionalGame = gameService.findById(id);
        if (optionalGame.isEmpty()) {
            Map<String, Object> error = buildErrorBody(
                    HttpStatus.NOT_FOUND,
                    "El juego solicitado no fue encontrado"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Game game = optionalGame.get();
        Resource apk = gameService.getApkResource(game);
        if (!apk.exists()) {
            Map<String, Object> error = buildErrorBody(
                    HttpStatus.NOT_FOUND,
                    "El APK asociado al juego no fue encontrado"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        String contentType = "application/vnd.android.package-archive";
        try {
            String guessed = guessContentType(apk);
            if (guessed != null) {
                contentType = guessed;
            }
        } catch (Exception ignored) {
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(apk.getFilename() != null ? apk.getFilename() : "download.apk")
                        .build()
        );

        return new ResponseEntity<>(apk, headers, HttpStatus.OK);
    }

    /**
     * Enviar Hash (GET)
     * Obtención del hash del APK.
     */
    @GetMapping("/{id}/hash")
    public ResponseEntity<Map<String, Object>> getApkHash(@PathVariable int id) {
        Optional<Game> optionalGame = gameService.findById(id);
        if (optionalGame.isEmpty()) {
            Map<String, Object> error = buildErrorBody(
                    HttpStatus.NOT_FOUND,
                    "El juego solicitado no fue encontrado"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Game game = optionalGame.get();
        Optional<String> hash = gameService.calculateAndStoreApkHash(game);

        if (hash.isEmpty()) {
            Map<String, Object> error = buildErrorBody(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo calcular el hash del APK"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("status", "ok");
        body.put("code", HttpStatus.OK.value());
        body.put("message", "Hash del APK calculado correctamente");
        body.put("hash", hash.get());

        return ResponseEntity.ok(body);
    }

    private String guessContentType(Resource resource) {
        String filename = resource.getFilename();
        if (filename == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        String mimeType = URLConnection.guessContentTypeFromName(filename);
        if (mimeType == null) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return mimeType;
    }

    private Map<String, Object> buildErrorBody(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "error");
        body.put("code", status.value());
        body.put("message", message);
        return body;
    }
}
