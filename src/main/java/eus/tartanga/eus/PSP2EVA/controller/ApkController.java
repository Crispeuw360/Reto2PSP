package eus.tartanga.eus.PSP2EVA.controller;


import eus.tartanga.eus.PSP2EVA.model.Apk;
import eus.tartanga.eus.PSP2EVA.service.ApkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/apks")
public class ApkController {

    private final ApkService apkService;

    @Autowired
    public ApkController(ApkService apkService) {
        this.apkService = apkService;
    }

    /**
     * Listado de aplicaciones (GET)
     * Devuelve directamente la lista de juegos como JSON "por defecto".
     */
    @GetMapping
    public ResponseEntity<List<Apk>> listGames() {
        List<Apk> games = apkService.findAll();
        return ResponseEntity.ok(games);
    }

    /**
     * Obtención de imagen con curl (GET)
     * Descarga binaria de la imagen del juego.
     */
    @GetMapping("/image/{id}")
    public ResponseEntity<?> getGameImage(@PathVariable int id) {
        Optional<Apk> optionalGame = apkService.findById(id);
        if (optionalGame.isEmpty()) {
            Map<String, Object> error = buildErrorBody(
                    HttpStatus.NOT_FOUND,
                    "El juego solicitado no fue encontrado"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Apk game = optionalGame.get();
        Resource image = apkService.getImageResource(game);
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
    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadApk(@PathVariable int id) {
        Optional<Apk> optionalGame = apkService.findById(id);
        if (optionalGame.isEmpty()) {
            Map<String, Object> error = buildErrorBody(
                    HttpStatus.NOT_FOUND,
                    "El juego solicitado no fue encontrado"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Apk game = optionalGame.get();
        Resource apk = apkService.getApkResource(game);
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
        Optional<Apk> optionalGame = apkService.findById(id);
        if (optionalGame.isEmpty()) {
            Map<String, Object> error = buildErrorBody(
                    HttpStatus.NOT_FOUND,
                    "El juego solicitado no fue encontrado"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Apk game = optionalGame.get();
        Optional<String> hash = apkService.calculateAndStoreApkHash(game);

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
    
    /**
     * Añadir APK (POST)
     * Recibe un JSON con los datos del juego y lo añade a la lista.
     */
    @PostMapping
    public ResponseEntity<?> addApk(@RequestBody Apk apk) {
        // Validación simple: el nombre es obligatorio
        if (apk.getNombre() == null || apk.getNombre().isBlank()) {
            return ResponseEntity.badRequest().body(
                buildErrorBody(HttpStatus.BAD_REQUEST, "El nombre del juego es obligatorio")
            );
        }

        Apk createdApk = apkService.addApk(apk);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdApk);
    }

    /**
     * Modificar descripción (PUT)
     * Recibe un JSON con la nueva descripción y actualiza el juego indicado por ID.
     * Espera un JSON así: { "descripcion": "Nueva descripción aquí..." }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDescription(@PathVariable int id, @RequestBody Map<String, String> body) {
        String newDescription = body.get("descripcion");

        if (newDescription == null) {
            return ResponseEntity.badRequest().body(
                buildErrorBody(HttpStatus.BAD_REQUEST, "Se requiere el campo 'descripcion' en el cuerpo JSON")
            );
        }

        Optional<Apk> updatedApk = apkService.updateDescription(id, newDescription);

        if (updatedApk.isEmpty()) {
            Map<String, Object> error = buildErrorBody(
                    HttpStatus.NOT_FOUND,
                    "El juego solicitado no fue encontrado para actualizar"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        return ResponseEntity.ok(updatedApk.get());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApk(@PathVariable int id) {
        boolean isDeleted = apkService.deleteApk(id);

        if (isDeleted) {
            // Retornamos 204 No Content cuando el borrado es exitoso
            return ResponseEntity.noContent().build();
        } else {
            // Retornamos 404 Not Found si el ID no existía
            Map<String, Object> error = buildErrorBody(
                    HttpStatus.NOT_FOUND,
                    "El juego con ID " + id + " no fue encontrado para eliminar"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
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
