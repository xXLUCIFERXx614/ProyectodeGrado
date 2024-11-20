package com.idiomas.app.service;

import org.springframework.stereotype.Service;

import com.idiomas.app.entity.Course;
import com.idiomas.app.entity.CourseEnrollment;
import com.idiomas.app.entity.Student;
import com.idiomas.app.repository.CourseRepository;
import com.idiomas.app.repository.StudentRepository;


@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    public String registerStudent(String identificationNumber, String courseId, String firstName, String lastName, String email) {
        // Buscar curso seleccionado
        Course selectedCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        // Buscar o crear estudiante
        Student student = studentRepository.findByIdentificationNumber(identificationNumber)
                .orElseGet(() -> new Student());

        // Si el estudiante no tiene inscripciones previas, validar que el curso sea Nivel 1
        if (student.getCourseEnrollments().isEmpty()) {
            if (!"Nivel 1".equalsIgnoreCase(selectedCourse.getEnglishLevel())) {
                return "error:Los estudiantes nuevos solo pueden registrarse en el Nivel 1.";
            }
        } else {
            // Validar si el estudiante ya tiene cursos activos
            if (hasActiveCourses(student)) {
                return "error:No puede registrarse en un nuevo curso mientras tenga cursos activos.";
            }

            // Validar si cumple el prerequisito
            if (!hasPrerequisiteCompleted(student, selectedCourse.getEnglishLevel())) {
                return "error:Debe aprobar el nivel anterior antes de registrarse en este curso.";
            }
        }

        // Actualizar datos del estudiante
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setIdentificationNumber(identificationNumber);

        // Agregar el curso a las inscripciones
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourseId(courseId);
        enrollment.setCourseName(selectedCourse.getCourseName());
        enrollment.setEnglishLevel(selectedCourse.getEnglishLevel());
        enrollment.setStatus("Activo");
        student.getCourseEnrollments().add(enrollment);

        // Guardar estudiante
        studentRepository.save(student);

        return "success:Registro exitoso. El estudiante ha sido inscrito correctamente.";
    }


    public Student findStudentByIdentificationNumber(String identificationNumber) {
        return studentRepository.findByIdentificationNumber(identificationNumber).orElse(null);
    }

    private boolean hasActiveCourses(Student student) {
        return student.getCourseEnrollments().stream()
                .anyMatch(enrollment -> "Activo".equalsIgnoreCase(enrollment.getStatus()));
    }

    private boolean hasPrerequisiteCompleted(Student student, String requestedLevel) {
        int requestedLevelNumber = extractLevelNumber(requestedLevel);

        return student.getCourseEnrollments().stream()
                .anyMatch(enrollment -> {
                    int enrolledLevelNumber = extractLevelNumber(enrollment.getEnglishLevel());
                    return enrolledLevelNumber == requestedLevelNumber - 1 &&
                            "Aprobado".equalsIgnoreCase(enrollment.getStatus());
                });
    }

    private int extractLevelNumber(String level) {
        if (level != null && level.startsWith("Nivel ")) {
            try {
                return Integer.parseInt(level.replace("Nivel ", "").trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Formato de nivel no válido: " + level, e);
            }
        }
        throw new IllegalArgumentException("Formato de nivel no válido: " + level);
    }
}