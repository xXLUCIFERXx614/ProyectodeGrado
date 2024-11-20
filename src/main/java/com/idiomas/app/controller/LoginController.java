package com.idiomas.app.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.idiomas.app.entity.Student;
import com.idiomas.app.entity.Admin;
import com.idiomas.app.entity.Course;
import com.idiomas.app.entity.Professor;
import com.idiomas.app.repository.StudentRepository;
import com.idiomas.app.repository.AdminRepository;
import com.idiomas.app.repository.CourseRepository;
import com.idiomas.app.repository.ProfessorRepository;

import java.util.List;

@Controller
public class LoginController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @GetMapping("/upload-excel")
    public String uploadExcelPage() {
        return "uploadExcel";
    }

    @GetMapping("/index")
    public String showIndex() {
        return "index";
    }

    @GetMapping("/studentsList")
    public String showStudentsList() {
        return "studentsList";
    }
    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("identificationNumber") String identificationNumber,
                        HttpSession session,
                        Model model) {
        // Validación de campos vacíos
        if (email == null || identificationNumber == null || email.isEmpty() || identificationNumber.isEmpty()) {
            model.addAttribute("error", "Por favor, ingresa un correo y un número de identificación.");
            return "index";
        }

        email = email.toLowerCase();

        // Primero busca el estudiante
        Student student = studentRepository.findByIdentificationNumberAndEmail(identificationNumber, email);
        if (student != null) {
            session.setAttribute("user", student);
            model.addAttribute("student", student);
            return "Student";
        }

        // Luego busca el admin
        Admin admin = adminRepository.findByEmailAndIdentificationNumber(email, identificationNumber);
        if (admin != null) {
            List<Student> students = studentRepository.findAll();
            model.addAttribute("students", students);
            session.setAttribute("user", admin);
            return "Admin";
        }

        // Finalmente, busca el profesor
        Professor professor = professorRepository.findByEmailAndIdentificationNumber(email, identificationNumber);
        if (professor != null) {
            session.setAttribute("user", professor);
            List<Course> courses = courseRepository.findByProfessorId(professor.getId());
            model.addAttribute("professor", professor);
            model.addAttribute("courses", courses);
            return "Professor";
        }

        // Si no encuentra ninguna coincidencia
        model.addAttribute("error", "Credenciales inválidas.");
        return "index";
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
