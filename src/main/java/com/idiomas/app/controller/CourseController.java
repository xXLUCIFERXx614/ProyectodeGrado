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

import java.util.List;
import java.util.Map;
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
        modelAndView.addObject("courses", courses);

        // Crear el mapa de profesores con los IDs de cursos
        Map<String, Professor> professorsMap = courses.stream()
                .filter(course -> course.getProfessorId() != null)
                .collect(Collectors.toMap(
                        Course::getId,
                        course -> professorRepository.findById(course.getProfessorId()).orElse(null)
                ));

        modelAndView.addObject("professorsMap", professorsMap);

        return modelAndView;
    }
    
    
 


    // Método para obtener todos los cursos y redirigir a la vista ListaCursos.html
    @GetMapping("/listar")
    public ModelAndView getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        List<Professor> professors = professorRepository.findAll();

        // Crear el mapa de course.id -> Professor
        Map<String, Professor> professorsMap = professors.stream()
            .filter(professor -> professor.getCourseIds() != null)
            .flatMap(professor -> professor.getCourseIds().stream()
                .map(courseId -> Map.entry(courseId, professor)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        ModelAndView modelAndView = new ModelAndView("ListadeCursos");
        modelAndView.addObject("courses", courses);
        modelAndView.addObject("professorsMap", professorsMap); // Agregar el mapa al modelo

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
    public RedirectView createCourse(@ModelAttribute Course course, @RequestParam("professorId") String professorId) {
        Professor professor = professorRepository.findById(professorId).orElse(null);
        
        if (professor != null) {
            // Asigna el ID del profesor al curso y guarda el curso
            course.setProfessorId(professorId);
            courseRepository.save(course);

            // Agrega el ID del curso al profesor solo si no existe ya en la lista
            List<String> courseIds = professor.getCourseIds();
            if (courseIds == null) {
                courseIds = new ArrayList<>();
            }
            if (!courseIds.contains(course.getId())) {
                courseIds.add(course.getId());
            }
            professor.setCourseIds(courseIds);

            // Guarda los cambios en el profesor
            professorRepository.save(professor);
        }
        
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
        
        // Eliminar el curso
        courseRepository.delete(course);
        
        // Redirigir a la lista de cursos
        return new RedirectView("/course/listar");
    }

    
    
    @GetMapping("/{courseId}")
    public ModelAndView showCourseDetails(@PathVariable String courseId) {
        ModelAndView modelAndView = new ModelAndView("DetallesCursos");

        // Obtén el curso si existe
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (!optionalCourse.isPresent()) {
            modelAndView.addObject("message", "Curso no encontrado.");
            return modelAndView;
        }
        Course course = optionalCourse.get();

        // Obtén el profesor asignado al curso
        Optional<Professor> optionalProfessor = professorRepository.findById(course.getProfessorId());
        Professor professor = optionalProfessor.orElse(null);

        // Obtén la lista de estudiantes asociados al curso usando el nuevo método
        List<Student> students = studentRepository.findByCourseEnrollments_CourseId(courseId);
        
        // Agrega el curso, los estudiantes y el profesor al modelo
        modelAndView.addObject("course", course);
        modelAndView.addObject("students", students);
        modelAndView.addObject("professor", professor);

        return modelAndView;
    }
    
}

