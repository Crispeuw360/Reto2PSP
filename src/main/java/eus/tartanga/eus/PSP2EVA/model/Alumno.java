package eus.tartanga.eus.PSP2EVA.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Alumno {
    private Integer id;
    private String nombre;
    private String curso;

    // Constructor vacío para Jackson
    public Alumno() {}

    // Constructor con parámetros
    public Alumno(Integer id, String nombre, String curso) {
        this.id = id;
        this.nombre = nombre;
        this.curso = curso;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    @Override
    public String toString() {
        return "Alumno{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", curso='" + curso + '\'' +
                '}';
    }
    
}
