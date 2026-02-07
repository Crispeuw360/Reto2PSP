package eus.tartanga.eus.PSP2EVA.service;

import eus.tartanga.eus.PSP2EVA.model.Apk;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private  List<Apk> apks = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
    	cargarApks();

        // Calculamos y almacenamos el hash de todos los APK al arrancar el servidor.
        for (Apk apk : apks) {
            calculateAndStoreApkHash(apk);
        }
    }
    

	private void cargarApks () {
		try {
			File file = ResourceUtils.getFile(getClass().getResource("/apk.json"));
			System.out.println("primer json" + file);
			apks = objectMapper.readValue(file, new TypeReference<List<Apk>>() {});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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

    /**
     * Calcula (y guarda en memoria) el hash SHA-256 del APK del juego.
     */
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
            return Optional.of(hash);
        } catch (IOException | NoSuchAlgorithmException e) {
            return Optional.empty();
        }
    }
}

