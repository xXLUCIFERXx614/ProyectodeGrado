package com.idiomas.app.controller;

import com.idiomas.app.entity.Admin;
import com.idiomas.app.entity.Professor;
import com.idiomas.app.entity.Student;
import com.idiomas.app.repository.AdminRepository;
import com.idiomas.app.repository.ProfessorRepository;
import com.idiomas.app.repository.StudentRepository;

import ch.qos.logback.core.model.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admins")
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/Link")
    public ModelAndView showAdminPage() {
        return new ModelAndView("Admin"); // Debe coincidir con el nombre del archivo Admin.html en templates
    }
    
    @GetMapping("/listar")
    public ModelAndView listAdmins() {
        List<Admin> admins = adminRepository.findAll(); // Obtiene todos los administradores
        ModelAndView modelAndView = new ModelAndView("ListaAdmin"); // Crea el objeto ModelAndView con la vista ListaAdmin
        modelAndView.addObject("admins", admins); // Añade la lista de administradores al modelo
        return modelAndView; // Retorna el ModelAndView con los datos y la vista
    }
    @GetMapping("/new")
    public ModelAndView showCreateAdminForm() {
        ModelAndView modelAndView = new ModelAndView("CrearAdmin");
        modelAndView.addObject("admin", new Admin()); // Objeto Admin vacío para el formulario
        modelAndView.addObject("title", "Crear Nuevo Administrador");
        modelAndView.addObject("actionUrl", "/admins/new"); // URL para el envío del formulario
        modelAndView.addObject("buttonText", "Guardar");
        return modelAndView;
    }

    @PostMapping("/new")
    public ModelAndView createAdmin(@ModelAttribute("admin") Admin admin) {
        adminRepository.save(admin); // Guarda el nuevo administrador
        return new ModelAndView("redirect:/admins/listar"); // Redirige correctamente a la lista de administradores
    }
    @GetMapping("/edit/{id}")
    public ModelAndView showEditAdminForm(@PathVariable("id") String id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado: " + id));
        ModelAndView modelAndView = new ModelAndView("CrearAdmin");
        modelAndView.addObject("admin", admin); // Pasa el administrador existente al formulario
        modelAndView.addObject("title", "Editar Administrador");
        modelAndView.addObject("actionUrl", "/admins/edit/" + id); // URL para el envío del formulario
        modelAndView.addObject("buttonText", "Actualizar");
        return modelAndView;
    }

    @PostMapping("/edit/{id}")
    public ModelAndView editAdmin(@PathVariable("id") String id, @ModelAttribute("admin") Admin updatedAdmin) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado: " + id));
        admin.setFirstName(updatedAdmin.getFirstName());
        admin.setLastName(updatedAdmin.getLastName());
        admin.setEmail(updatedAdmin.getEmail());
        adminRepository.save(admin); // Actualiza el administrador
        return new ModelAndView("redirect:/admins/listar"); // Redirige a la lista de administradores
    }

    @GetMapping("/delete/{id}")
    public ModelAndView deleteAdmin(@PathVariable("id") String id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado: " + id));
        adminRepository.delete(admin); // Elimina el administrador de la base de datos
        return new ModelAndView("redirect:/admins/listar"); // Redirige correctamente a la lista de administradores
    }

    @GetMapping("/credenciales")
    public ModelAndView viewCredentials() {
        // Obtener credenciales del primer administrador
        Admin admin = adminRepository.findAll().stream().findFirst().orElse(null);

        // Obtener credenciales del primer profesor
        Professor professor = professorRepository.findAll().stream().findFirst().orElse(null);

        // Obtener credenciales del primer estudiante
        Student student = studentRepository.findAll().stream().findFirst().orElse(null);

        // Crear y configurar ModelAndView
        ModelAndView modelAndView = new ModelAndView("VerCredenciales");
        modelAndView.addObject("admin", admin);
        modelAndView.addObject("professor", professor);
        modelAndView.addObject("student", student);

        return modelAndView; // Retorna la vista con los datos
    }

}

