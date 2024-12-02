package com.idiomas.app.controller;

import com.idiomas.app.entity.Course;
import com.idiomas.app.entity.Professor;
import com.idiomas.app.entity.Student;
import com.idiomas.app.repository.CourseRepository;
import com.idiomas.app.repository.ProfessorRepository;
import com.idiomas.app.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.function.Function;


@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    
    @Autowired
    private ProfessorRepository professorRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }
    @GetMapping("/Link")
    public ModelAndView showCoursesPage() {
        ModelAndView modelAndView = new ModelAndView("ListadeCursos");

        // Obtener todos los cursos
        List<Course> courses = courseRepository.findAll();
        if (courses == null || courses.isEmpty()) {
            modelAndView.addObject("courses", new ArrayList<>()); // Lista vacía si no hay cursos
            modelAndView.addObject("professorsMap", new HashMap<>()); // Mapa vacío si no hay profesores
            return modelAndView;
        }

        // Crear el mapa de profesores permitiendo nulos
        Map<String, String> professorsMap = courses.stream()
                .filter(course -> course.getId() != null) // Validar que el ID del curso no sea nulo
                .collect(Collectors.toMap(
                    Course::getId, // Clave: ID del curso
                    course -> {
                        if (course.getProfessorId() != null) {
                            // Si hay un ID de profesor, buscar el profesor
                            return professorRepository.findById(course.getProfessorId())
                                    .map(professor -> professor.getFirstName() + " " + professor.getLastName())
                                    .orElse("No asignado");
                        }
                        // Si no hay profesor, devolver "No asignado"
                        return "No asignado";
                    }
                ));

        // Agregar datos al modelo
        modelAndView.addObject("courses", courses);
        modelAndView.addObject("professorsMap", professorsMap);

        return modelAndView;
    }

 


    // Método para obtener todos los cursos y redirigir a la vista ListaCursos.html
    @GetMapping("/listar")
    public ModelAndView getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        List<Professor> professors = professorRepository.findAll();

        // Crear un mapa de course.id -> Nombre completo del profesor
        Map<String, String> professorNamesMap = new HashMap<>();
        for (Course course : courses) {
            String professorName = "No asignado";
            if (course.getProfessorId() != null) {
                Professor professor = professors.stream()
                        .filter(p -> p.getId().equals(course.getProfessorId()))
                        .findFirst()
                        .orElse(null);
                if (professor != null) {
                    professorName = professor.getFirstName() + " " + professor.getLastName();
                }
            }
            professorNamesMap.put(course.getId(), professorName);
        }

        ModelAndView modelAndView = new ModelAndView("ListadeCursos");
        modelAndView.addObject("courses", courses); // Lista de cursos
        modelAndView.addObject("professorNamesMap", professorNamesMap); // Mapa de nombres de profesores (siempre inicializado)

        return modelAndView;
    }


    @GetMapping("/")
    public ModelAndView showCreateCourseForm() {
        ModelAndView modelAndView = new ModelAndView("CrearCurso");
        modelAndView.addObject("course", new Course()); // Agrega un curso vacío si es nuevo

        List<String> englishLevels = List.of("Nivel 1", "Nivel 2", "Nivel 3", "Nivel 4");
        modelAndView.addObject("englishLevels", englishLevels);

        List<Professor> professors = professorRepository.findAll();
        modelAndView.addObject("professors", professors);

        return modelAndView;
    }

    @PostMapping("/new")
    public RedirectView createCourse(@ModelAttribute Course course, @RequestParam(name = "professorId", required = false) String professorId) {
        System.out.println("Valor recibido de professorId: '" + professorId + "'");

        if (professorId == null || professorId.trim().isEmpty()) {
            course.setProfessorId(null);
        } else {
            course.setProfessorId(professorId);

            // Agregar lógica para vincular el curso al profesor si es necesario
            Professor professor = professorRepository.findById(professorId).orElse(null);
            if (professor != null) {
                List<String> courseIds = professor.getCourseIds();
                if (courseIds == null) {
                    courseIds = new ArrayList<>();
                }
                if (!courseIds.contains(course.getId())) {
                    courseIds.add(course.getId());
                }
                professor.setCourseIds(courseIds);
                professorRepository.save(professor);
            }
        }

        // Guarda el curso
        courseRepository.save(course);

        return new RedirectView("/course/listar");
    }



    
 // Método GET para mostrar el formulario de edición (reutilizando la vista CrearCurso.html)
    @GetMapping("/edit/{id}")
    public ModelAndView showEditCourseForm(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView("CrearCurso");
        Course course = courseRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));
        modelAndView.addObject("course", course); // Carga el curso existente

        // Cargar lista de profesores para el selector en la vista
        List<Professor> professors = professorRepository.findAll();
        modelAndView.addObject("professors", professors);

        // Lista de niveles de inglés
        List<String> englishLevels = List.of("Nivel 1", "Nivel 2", "Nivel 3", "Nivel 4");
        modelAndView.addObject("englishLevels", englishLevels);

        return modelAndView;
    }



 // Método POST para actualizar el curso editado (mismo formulario para crear y editar)
    @PostMapping("/edit/{id}")
    public RedirectView updateCourse(@PathVariable String id, @ModelAttribute Course course, @RequestParam("professorId") String professorId) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));
        
        // Actualiza los campos básicos del curso
        existingCourse.setCourseName(course.getCourseName());
        existingCourse.setStartDate(course.getStartDate());
        existingCourse.setEndDate(course.getEndDate());
        existingCourse.setStatus(course.getStatus());
        existingCourse.setEnglishLevel(course.getEnglishLevel()); // Ajuste a un solo nivel específico

        // Verifica si el profesor ha cambiado
        if (!professorId.equals(existingCourse.getProfessorId())) {
            // Remueve el curso de la lista de cursos del profesor anterior, si existe
            if (existingCourse.getProfessorId() != null) {
                Professor previousProfessor = professorRepository.findById(existingCourse.getProfessorId()).orElse(null);
                if (previousProfessor != null) {
                    previousProfessor.getCourseIds().remove(id);
                    professorRepository.save(previousProfessor);
                }
            }

            // Actualiza el ID del profesor en el curso
            existingCourse.setProfessorId(professorId);

            // Agrega el curso a la lista de cursos del nuevo profesor
            Professor newProfessor = professorRepository.findById(professorId).orElse(null);
            if (newProfessor != null) {
                List<String> courseIds = newProfessor.getCourseIds();
                if (courseIds == null) {
                    courseIds = new ArrayList<>();
                }
                courseIds.add(id);
                newProfessor.setCourseIds(courseIds);
                professorRepository.save(newProfessor);
            }
        }

        // Guarda los cambios en el curso
        courseRepository.save(existingCourse);
        return new RedirectView("/course/listar"); // Redirige a la lista de cursos
    }

    
 // Método GET para eliminar un curso
    @GetMapping("/delete/{id}")
    public RedirectView deleteCourse(@PathVariable String id) {
        // Buscar el curso que se va a eliminar
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));

        // Buscar todos los estudiantes que tienen este curso en sus inscripciones
        List<Student> studentsWithCourse = studentRepository.findByCourseEnrollments_CourseId(id);

        // Recorrer cada estudiante y eliminar la referencia del curso en sus inscripciones
        for (Student student : studentsWithCourse) {
            // Filtrar las inscripciones para quitar el curso que se está eliminando
            student.getCourseEnrollments().removeIf(enrollment -> enrollment.getCourseId().equals(id));
            // Guardar el estudiante actualizado en el repositorio
            studentRepository.save(student);
        }

        // Verificar si el curso tiene un profesor asignado
        if (course.getProfessorId() != null && !course.getProfessorId().isEmpty()) {
            // Buscar el profesor asignado al curso
            Professor professor = professorRepository.findById(course.getProfessorId()).orElse(null);
            if (professor != null) {
                // Remover el curso de la lista de cursos del profesor
                professor.getCourseIds().remove(id);
                // Guardar los cambios en el profesor
                professorRepository.save(professor);
            }
        }

        // Eliminar el curso
        courseRepository.delete(course);

        // Redirigir a la lista de cursos
        return new RedirectView("/course/listar");
    }


    
    
    @GetMapping("/{courseId}")
    public ModelAndView showCourseDetails(@PathVariable String courseId) {
        ModelAndView modelAndView = new ModelAndView("DetallesCursos");

        // Buscar el curso
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + courseId));

        // Verificar si el curso tiene un profesor asignado
        Professor professor = null;
        if (course.getProfessorId() != null && !course.getProfessorId().trim().isEmpty()) {
            professor = professorRepository.findById(course.getProfessorId()).orElse(null);
        }

        // Obtener estudiantes asociados al curso
        List<Student> students = studentRepository.findByCourseEnrollments_CourseId(courseId);

        // Agregar datos al modelo
        modelAndView.addObject("course", course);
        modelAndView.addObject("professor", professor); // Puede ser null si no hay profesor asignado
        modelAndView.addObject("students", students);

        return modelAndView;
    }

    
}

