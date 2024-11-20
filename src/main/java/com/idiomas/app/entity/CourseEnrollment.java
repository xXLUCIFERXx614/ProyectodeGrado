package com.idiomas.app.entity;



public class CourseEnrollment {
    private String courseId;
    private String courseName;
    private String englishLevel;
    private String finalGrade; // Nota final en el curso
    private String status; // "Aprobado", "No aprobado" o "NP"
    private String endDate;

    // Constructor vacío
    public CourseEnrollment() {}

    // Constructor con parámetros
    public CourseEnrollment(String courseId, String courseName, String englishLevel, String finalGrade) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.englishLevel = englishLevel;
        setFinalGrade(finalGrade); // Se establece la nota y el estado automáticamente
    }

    // Getters y setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getEnglishLevel() {
        return englishLevel;
    }

    public void setEnglishLevel(String englishLevel) {
        this.englishLevel = englishLevel;
    }

    public String getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(String finalGrade) {
        this.finalGrade = finalGrade;
        
        // Determinar el estado en función de la nota
        if ("NP".equalsIgnoreCase(finalGrade)) {
            this.status = "No Aprobado";
        } else {
            try {
                double grade = Double.parseDouble(finalGrade);
                this.status = grade >= 3.0 ? "Aprobado" : "No Aprobado";
            } catch (NumberFormatException e) {
                this.status = "No Aprobado"; // Valor por defecto si la nota no es válida
            }
        }
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    // Agrega este método para poder establecer el estado manualmente
    public void setStatus(String status) {
        this.status = status;
    }
}
