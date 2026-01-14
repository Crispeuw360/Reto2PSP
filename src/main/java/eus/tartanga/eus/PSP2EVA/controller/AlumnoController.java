package eus.tartanga.eus.PSP2EVA.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import eus.tartanga.eus.PSP2EVA.model.Alumno;
import eus.tartanga.eus.PSP2EVA.service.AlumnoService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/alumnos")
public class AlumnoController {
    
    private final AlumnoService alumnoService;
    
    @Autowired
    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }
    
    @GetMapping
    public ResponseEntity<List<Alumno>> getAllAlumnos() {
        System.out.println("GET /alumnos llamado");
        List<Alumno> alumnos = alumnoService.obtenerTodosLosAlumnos();
        System.out.println("Alumnos encontrados: " + alumnos.size());
        return ResponseEntity.ok(alumnos);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Alumno> getAlumnoById(@PathVariable Integer id) {
        Alumno alumno = alumnoService.obtenerAlumnoPorId(id); // Ahora devuelve Alumno o null
        
        if (alumno != null) {
            return ResponseEntity.ok(alumno);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/curso/{curso}")
    public ResponseEntity<List<Alumno>> getAlumnosByCurso(@PathVariable String curso) {
        List<Alumno> alumnos = alumnoService.obtenerAlumnosPorCurso(curso);
        if (alumnos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(alumnos);
    }
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countAlumnos() {
        long total = alumnoService.contarAlumnos();
        Map<String, Long> response = new HashMap<>();
        response.put("total", total);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/buscar")
    public ResponseEntity<List<Alumno>> buscarAlumnos(@RequestParam String nombre) {
        List<Alumno> alumnos = alumnoService.buscarAlumnosPorNombre(nombre);
        if (alumnos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(alumnos);
    }
}
