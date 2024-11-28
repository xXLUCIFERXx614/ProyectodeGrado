package com.idiomas.app.controller;

import org.springframework.web.servlet.view.RedirectView;
import com.idiomas.app.entity.Course;

import com.idiomas.app.entity.Professor;
import com.idiomas.app.entity.Student;
import com.idiomas.app.repository.CourseRepository;
import com.idiomas.app.repository.ProfessorRepository;
import com.idiomas.app.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/professors")
public class ProfessorController {

    @Autowired
    private ProfessorRepository professorRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private StudentRepository studentRepository;

    
    @GetMapping("/details/{professorId}")
    public ModelAndView viewProfessorDetails(@PathVariable String professorId) {
        // Crear ModelAndView para el detalle del profesor
        ModelAndView modelAndView = new ModelAndView();

        try {
            // Buscar el profesor por su ID
            Professor professor = professorRepository.findById(professorId)
                    .orElseThrow(() -> new RuntimeException("Profesor no encontrado con el ID: " + professorId));

            // Obtener la lista de cursos asociados
            List<Course> courses = courseRepository.findByProfessorId(professorId);

            // Configurar la vista y los datos
            modelAndView.setViewName("Professor");
            modelAndView.addObject("professor", professor);
            modelAndView.addObject("courses", courses);
        } catch (RuntimeException e) {
            // Manejar error y redirigir a una vista de error
            modelAndView.setViewName("error/ProfesorNoEncontrado");
            modelAndView.addObject("errorMessage", e.getMessage());
        }

        return modelAndView;
    }


    // Método para obtener todos los cursos y redirigir a la vista ListaCursos.html
    @GetMapping("/listar")
    public ModelAndView getAllProfessors() {
        List<Professor> professors = professorRepository.findAll(); // Obtiene la lista de profesores
        ModelAndView modelAndView = new ModelAndView("ListaProfesores"); // Especifica la vista
        modelAndView.addObject("professors", professors); // Añade la lista de profesores al modelo
        return modelAndView;
    }
    @GetMapping("/")
    public ModelAndView showCreateProfessorForm() {
        ModelAndView modelAndView = new ModelAndView("CrearProfesor"); // Especifica la vista del formulario
        modelAndView.addObject("professor", new Professor()); // Añade un objeto vacío para el formulario
        return modelAndView;
    }

    // Método POST para procesar el formulario y crear el profesor
    @PostMapping("/new")
    public RedirectView createProfessor(@ModelAttribute Professor professor) {
        professorRepository.save(professor); // Guarda el nuevo profesor en la base de datos
        return new RedirectView("/professors/listar"); // Redirige a la lista de profesores
    }
    @GetMapping("/{id}")
    public ModelAndView showProfessorDetails(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView("DetallesProfesor");

        // Buscar el profesor por ID
        Optional<Professor> optionalProfessor = professorRepository.findById(id);
        if (!optionalProfessor.isPresent()) {
            modelAndView.addObject("message", "Profesor no encontrado.");
            modelAndView.setViewName("error"); // Puedes redirigir a una página de error personalizada
            return modelAndView;
        }
        
        Professor professor = optionalProfessor.get();
        modelAndView.addObject("professor", professor);

        // Buscar los cursos asociados al profesor
        List<Course> courses = courseRepository.findByProfessorId(id);
        modelAndView.addObject("courses", courses);

        return modelAndView;
    }
    @GetMapping("/course/{courseId}")
    public ModelAndView showCourseStudents(@PathVariable String courseId) {
        ModelAndView modelAndView = new ModelAndView("ListaAlumnosProfessor");

        // Buscar el curso
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (!optionalCourse.isPresent()) {
            modelAndView.addObject("message", "Curso no encontrado.");
            modelAndView.setViewName("error/CursoNoEncontrado"); // Vista personalizada para errores
            return modelAndView;
        }

        Course course = optionalCourse.get();

        // Buscar el profesor asociado al curso
        Optional<Professor> optionalProfessor = professorRepository.findById(course.getProfessorId());
        if (!optionalProfessor.isPresent()) {
            modelAndView.addObject("message", "Profesor asociado al curso no encontrado.");
            modelAndView.setViewName("error/ProfesorNoEncontrado"); // Vista personalizada para errores
            return modelAndView;
        }

        Professor professor = optionalProfessor.get();

        // Obtener la lista de estudiantes inscritos en el curso
        List<Student> students = studentRepository.findByCourseEnrollments_CourseId(courseId);

        // Agregar datos al modelo
        modelAndView.addObject("course", course);
        modelAndView.addObject("professor", professor);
        modelAndView.addObject("students", students);

        return modelAndView;
    }

    
    
 // Método para actualizar la nota definitiva de un estudiante en un curso
    @PostMapping("/course/{courseId}/updateFinalGrade")
    public RedirectView updateFinalGrade(
            @PathVariable("courseId") String courseId,
            @RequestParam("studentId") String studentId,
            @RequestParam("finalGrade") String finalGrade,
            RedirectAttributes redirectAttributes) {

        // Buscar el curso
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (!optionalCourse.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Curso no encontrado.");
            return new RedirectView("/professors/course/" + courseId);
        }
        Course course = optionalCourse.get();

        // Buscar al estudiante
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (!studentOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Estudiante no encontrado.");
            return new RedirectView("/professors/course/" + courseId);
        }

        Student student = studentOpt.get();
        boolean updated = false;

        // Actualizar la nota definitiva en la inscripción del curso correspondiente
        for (var enrollment : student.getCourseEnrollments()) {
            if (enrollment.getCourseId().equals(courseId)) {
                enrollment.setFinalGrade(finalGrade);
                updated = true;
                break;
            }
        }

        // Verificar si la nota fue actualizada
        if (!updated) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la inscripción del estudiante en el curso.");
            return new RedirectView("/professors/course/" + courseId);
        }

        // Guardar los cambios en el estudiante
        studentRepository.save(student);
        redirectAttributes.addFlashAttribute("success", "Nota definitiva actualizada correctamente.");

        // Redirigir de vuelta a la página de detalles del curso
        return new RedirectView("/professors/course/" + courseId);
    }

    @GetMapping("/edit/{id}")
    public ModelAndView showEditProfessorForm(@PathVariable("id") String id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profesor no encontrado"));

        ModelAndView modelAndView = new ModelAndView("CrearProfesor");
        modelAndView.addObject("professor", professor);
        modelAndView.addObject("title", "Editar Profesor");
        modelAndView.addObject("actionUrl", "/professors/edit/" + id);
        modelAndView.addObject("buttonText", "Actualizar");
        return modelAndView;
    }

    @PostMapping("/edit/{id}")
    public String editProfessor(@PathVariable("id") String id, 
                                @ModelAttribute("professor") Professor updatedProfessor, 
                                RedirectAttributes redirectAttributes) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profesor no encontrado"));

        // Actualizar los campos del profesor
        professor.setFirstName(updatedProfessor.getFirstName());
        professor.setLastName(updatedProfessor.getLastName());
        professor.setEmail(updatedProfessor.getEmail());
        professor.setIdentificationNumber(updatedProfessor.getIdentificationNumber());
        professorRepository.save(professor);

        // Agregar un mensaje de éxito
        redirectAttributes.addFlashAttribute("message", "Profesor actualizado exitosamente");

        // Redirigir a la lista de profesores
        return "redirect:/professors/listar";
    }


    @GetMapping("/delete/{id}")
    public ModelAndView deleteProfessor(@PathVariable("id") String id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profesor no encontrado"));
        professorRepository.delete(professor);

        ModelAndView modelAndView = new ModelAndView("redirect:/professors/listar");
        modelAndView.addObject("message", "Profesor eliminado exitosamente");
        return modelAndView;
    }


}
