package com.idiomas.app.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "Students")
public class Student {

    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String identificationNumber;
    private String email;
    private String program;
    private List<CourseEnrollment> courseEnrollments = new ArrayList<>(); // Historial de cursos

    // Constructor vacío
    public Student() {}

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public List<CourseEnrollment> getCourseEnrollments() {
        return courseEnrollments;
    }

    public void setCourseEnrollments(List<CourseEnrollment> courseEnrollments) {
        this.courseEnrollments = courseEnrollments;
    }

    // Método para agregar una inscripción al historial de cursos
    public void addCourseEnrollment(String courseId, String courseName, String englishLevel, String finalGrade) {
        CourseEnrollment enrollment = new CourseEnrollment(courseId, courseName, englishLevel, finalGrade);
        this.courseEnrollments.add(enrollment);
    }
}
