package eus.tartanga.eus.PSP2EVA.service;

import eus.tartanga.eus.PSP2EVA.model.Apk;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ApkService {

    private List<Apk> apks = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // --- CAMBIO IMPORTANTE: Ruta al archivo fuente (SOURCE) ---
    // "user.dir" suele ser la raíz del proyecto en Eclipse.
    // Si esto falla, pon la ruta absoluta: "C:/Users/Igor/eclipse-workspace/Reto2PSP/src/main/resources/apk.json"
    private final String RUTA_ARCHIVO = System.getProperty("user.dir") + "/src/main/resources/apk.json";
    
    private File jsonFile;

    @PostConstruct
    public void init() {
        cargarApks();

        // Calcular hashes iniciales
        for (Apk apk : apks) {
            calculateAndStoreApkHash(apk);
        }
    }
    
    private void cargarApks() {
        try {
            // Apuntamos directamente al archivo físico en la carpeta src
            jsonFile = new File(RUTA_ARCHIVO);
            
            System.out.println("Intentando cargar JSON desde código fuente: " + jsonFile.getAbsolutePath());

            if (jsonFile.exists()) {
                apks = objectMapper.readValue(jsonFile, new TypeReference<List<Apk>>() {});
            } else {
                System.err.println("¡CUIDADO! No se encuentra el archivo en src. Se iniciará lista vacía.");
                // Opcional: Intentar cargar del classpath como respaldo si falla el src
                apks = new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
            apks = new ArrayList<>();
        }
    }

    private void guardarCambios() {
        if (jsonFile == null) return;
        
        try {
            // Configurar para que el JSON se vea bonito (con saltos de línea)
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            
            // Escribir en el archivo físico
            objectMapper.writeValue(jsonFile, apks);
            System.out.println("JSON actualizado correctamente en: " + jsonFile.getAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("Error guardando cambios: " + e.getMessage());
        }
    }

    // --- RESTO DE MÉTODOS (con la llamada a guardarCambios añadida) ---

    public Apk addApk(Apk newApk) {
        int newId = apks.stream().mapToInt(Apk::getId).max().orElse(0) + 1;
        newApk.setId(newId);
        
        if (newApk.getDownloadHash() == null) newApk.setDownloadHash("");
        
        apks.add(newApk);
        guardarCambios(); // Guardar
        return newApk;
    }

    public Optional<Apk> updateDescription(int id, String newDescription) {
        Optional<Apk> optionalApk = findById(id);
        if (optionalApk.isPresent()) {
            Apk apk = optionalApk.get();
            apk.setDescripcion(newDescription);
            guardarCambios(); // Guardar
            return Optional.of(apk);
        }
        return Optional.empty();
    }
    
    public boolean deleteApk(int id) {
        boolean removed = apks.removeIf(apk -> apk.getId() == id);
        if (removed) {
            guardarCambios(); // Guardar
        }
        return removed;
    }

    // --- Métodos de lectura y utilidad ---

    public List<Apk> findAll() {
        return new ArrayList<>(apks);
    }

    public Optional<Apk> findById(int id) {
        return apks.stream().filter(g -> g.getId() == id).findFirst();
    }

    public Resource getImageResource(Apk game) {
        return new ClassPathResource(game.getImageUrl());
    }

    public Resource getApkResource(Apk game) {
        return new ClassPathResource(game.getApkUrl());
    }

    public Optional<String> calculateAndStoreApkHash(Apk game) {
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
            
            // Opcional: guardar hash calculado en el JSON
            // guardarCambios(); 
            
            return Optional.of(hash);
        } catch (IOException | NoSuchAlgorithmException e) {
            return Optional.empty();
        }
    }
}