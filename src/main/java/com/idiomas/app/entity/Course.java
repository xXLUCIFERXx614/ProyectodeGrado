package com.idiomas.app.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Document(collection = "Courses")
public class Course {
    @Id
    private String id;
    private String courseName;
    private String startDate;
    private String endDate;
    private String status;
    private String englishLevel; // Nivel de inglés específico de este curso
    private String professorId; // Id del profesor asignado (sin DBRef)

   

    // Constructor vacío
    public Course() {
        this.status = "Activo"; // Estado por defecto cuando se crea un curso
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
        updateStatus(); // Actualiza el estado al establecer la fecha de fin
    }

    public String getStatus() {
        updateStatus(); // Verifica y actualiza el estado antes de devolverlo
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEnglishLevel() {
        return englishLevel;
    }

    public void setEnglishLevel(String englishLevel) {
        List<String> validLevels = List.of("Nivel 1", "Nivel 2", "Nivel 3", "Nivel 4");
        if (!validLevels.contains(englishLevel)) {
            throw new IllegalArgumentException("Invalid level: " + englishLevel);
        }
        this.englishLevel = englishLevel;
    }

    public String getProfessorId() {
        return professorId;
    }

    public void setProfessorId(String professorId) {
        this.professorId = professorId;
    }

    // Método para verificar y actualizar el estado basado en la fecha de fin
    private void updateStatus() {
        if (endDate != null) {
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
            LocalDate today = LocalDate.now();

            if (ChronoUnit.DAYS.between(today, end) < 0) {
                this.status = "Inactivo";
            } else {
                this.status = "Activo";
            }
        }
    }
}
