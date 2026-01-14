package eus.tartanga.eus.PSP2EVA.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eus.tartanga.eus.PSP2EVA.model.Alumno;
import jakarta.annotation.PostConstruct;

@Service
public class AlumnoService {
    
    private List<Alumno> alumnos = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    public void init() {
        cargarAlumnosDesdeJSON();
    }
    
    private void cargarAlumnosDesdeJSON() {
        try {
            File file = ResourceUtils.getFile(getClass().getResource("/alumnos.json"));
            alumnos = objectMapper.readValue(file, new TypeReference<List<Alumno>>() {});
            System.out.println("Alumnos cargados: " + alumnos.size());
        } catch (IOException e) {
            System.err.println("Error al cargar el archivo JSON: " + e.getMessage());
            alumnos = new ArrayList<>();
        }
    }
    
    public List<Alumno> obtenerTodosLosAlumnos() {
        return new ArrayList<>(alumnos);
    }
    
    public Alumno obtenerAlumnoPorId(Integer id) {
        for (Alumno alumno : alumnos) {
            if (alumno.getId().equals(id)) {
                return alumno;
            }
        }
        return null;
    }

    public List<Alumno> obtenerAlumnosPorCurso(String curso) {
        List<Alumno> resultado = new ArrayList<>();
        for (Alumno alumno : alumnos) {
            if (alumno.getCurso().equalsIgnoreCase(curso)) {
                resultado.add(alumno);
            }
        }
        return resultado;
    }

    public long contarAlumnos() {
        return alumnos.size();
    }

    public List<Alumno> buscarAlumnosPorNombre(String nombre) {
        String nombreBusqueda = nombre.toLowerCase();
        List<Alumno> resultado = new ArrayList<>();
        for (Alumno alumno : alumnos) {
            if (alumno.getNombre().toLowerCase().contains(nombreBusqueda)) {
                resultado.add(alumno);
            }
        }
        return resultado;
    }
}
