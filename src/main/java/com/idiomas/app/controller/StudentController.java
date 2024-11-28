package com.idiomas.app.controller;

import com.idiomas.app.entity.Course;
import com.idiomas.app.entity.CourseEnrollment;
import com.idiomas.app.entity.Student;
import com.idiomas.app.repository.CourseRepository;
import com.idiomas.app.repository.StudentRepository;
import com.idiomas.app.service.StudentService;



import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.Normalizer;

import java.util.Collections;

import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private  StudentService studentService;
   
    @Autowired
    private CourseRepository courseRepository;

   
    // Método para listar todos los estudiantes utilizando ModelAndView
    @GetMapping("/listar")
    public ModelAndView listAllStudents() {
        ModelAndView modelAndView = new ModelAndView("ListaAlumnos"); // Especifica la vista
        List<Student> students = studentRepository.findAll(); // Obtiene todos los estudiantes de la base de datos
        modelAndView.addObject("students", students); // Añade la lista de estudiantes al objeto ModelAndView
        return modelAndView; // Retorna el objeto ModelAndView
    }
    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file, @RequestParam("courseId") String courseId) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Verificar si el curso existe en la base de datos
            Optional<Course> optionalCourse = courseRepository.findById(courseId);
            if (!optionalCourse.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body(Collections.singletonMap("message", "Curso no encontrado."));
            }

            Course course = optionalCourse.get();
            String courseName = course.getCourseName();
            String englishLevel = course.getEnglishLevel();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Omite la fila de encabezado

                // Leer los datos de la fila
                String firstName = getCellValueAsString(row.getCell(0));
                String lastName = getCellValueAsString(row.getCell(1));
                Cell identificationCell = row.getCell(2);
                if (identificationCell != null) {
                    identificationCell.setCellType(CellType.STRING);
                }
                String identificationNumber = identificationCell != null ? identificationCell.getStringCellValue() : "";

                // Verificar si existe un correo en el Excel
                String email = getCellValueAsString(row.getCell(3)); // Asume que la columna 3 es para correos
                if (email.isEmpty()) {
                    email = generateEmail(firstName, lastName); // Genera el correo si no existe
                }

                // Buscar al estudiante por número de identificación
                Optional<Student> studentOptional = studentRepository.findByIdentificationNumber(identificationNumber);

                Student student;
                if (studentOptional.isPresent()) {
                    student = studentOptional.get();
                } else {
                    student = new Student();
                    student.setFirstName(firstName);
                    student.setLastName(lastName);
                    student.setIdentificationNumber(identificationNumber);
                    student.setEmail(email); // Asigna el correo leído o generado
                    student.setProgram(getCellValueAsString(row.getCell(5)));
                }

                // Agregar detalles específicos del curso
                String finalGrade = getCellValueAsString(row.getCell(4));
                student.addCourseEnrollment(courseId, courseName, englishLevel, finalGrade);
                studentRepository.save(student);
            }

            return ResponseEntity.ok(Collections.singletonMap("message", "Archivo subido con éxito."));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Collections.singletonMap("message", "Error al procesar el archivo."));
        }
    }





    public String generateEmail(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            return "";
        }

        // Normaliza y elimina caracteres especiales
        firstName = removeAccents(firstName);
        lastName = removeAccents(lastName);

        // Obtén la primera letra de cada nombre
        String[] firstNameParts = firstName.split(" ");
        StringBuilder firstInitials = new StringBuilder();
        for (String namePart : firstNameParts) {
            if (!namePart.isEmpty()) {
                firstInitials.append(namePart.substring(0, 1).toLowerCase());
            }
        }

        // Solo el primer apellido en minúscula
        String[] lastNameParts = lastName.split(" ");
        String primaryLastName = lastNameParts[0].toLowerCase();

        return firstInitials.toString() + primaryLastName + "@uts.edu.co";
    }

    // Método para eliminar acentos y otros caracteres diacríticos
    private String removeAccents(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                         .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }


    // Método auxiliar para obtener el valor de la celda como String
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    

    @GetMapping("/{id}")
    public ModelAndView showStudentDetails(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView("DetallesAlumno");

        // Buscar al estudiante por ID
        Optional<Student> optionalStudent = studentRepository.findById(id);
        if (!optionalStudent.isPresent()) {
            modelAndView.addObject("message", "Estudiante no encontrado.");
            return modelAndView;
        }

        Student student = optionalStudent.get();

        // Asocia la fecha de finalización de cada curso a CourseEnrollment
        List<CourseEnrollment> enrollments = student.getCourseEnrollments();
        enrollments.forEach(enrollment -> {
            Optional<Course> optionalCourse = courseRepository.findById(enrollment.getCourseId());
            optionalCourse.ifPresent(course -> enrollment.setEndDate(course.getEndDate())); // Configura el endDate en cada enrollment
        });

        // Agregar el estudiante y sus detalles al modelo
        modelAndView.addObject("student", student);
        modelAndView.addObject("enrollments", enrollments);

        return modelAndView;
    }


    @PostMapping("/guardar")
    public ModelAndView saveStudent(@ModelAttribute("student") Student student,
                                    @RequestParam("courseId") String courseId,
                                    ModelAndView mav) {
        // Verifica si el ID es nulo o vacío
        if (student.getId() == null || student.getId().isEmpty()) {
            student.setId(null); // Asegúrate de que MongoDB genere un nuevo ID
        }

        Optional<Student> existingStudentOpt = student.getId() != null 
            ? studentRepository.findById(student.getId()) 
            : Optional.empty();

        if (existingStudentOpt.isPresent()) {
            Student existingStudent = existingStudentOpt.get();

            // Actualizar datos básicos del estudiante
            existingStudent.setFirstName(student.getFirstName());
            existingStudent.setLastName(student.getLastName());
            existingStudent.setIdentificationNumber(student.getIdentificationNumber());
            existingStudent.setEmail(student.getEmail());
            existingStudent.setProgram(student.getProgram());

            // Manejar la inscripción al curso
            CourseEnrollment enrollment = existingStudent.getCourseEnrollments().stream()
                .filter(e -> e.getCourseId().equals(courseId))
                .findFirst()
                .orElse(null);

            if (enrollment == null) {
                enrollment = createNewCourseEnrollment(student, courseId);
                existingStudent.getCourseEnrollments().add(enrollment);
            } else {
                updateCourseEnrollment(enrollment, student);
            }

            studentRepository.save(existingStudent);
        } else {
            // Crear un nuevo estudiante
            CourseEnrollment enrollment = createNewCourseEnrollment(student, courseId);
            student.getCourseEnrollments().clear();
            student.getCourseEnrollments().add(enrollment);
            studentRepository.save(student);
        }

        mav.setViewName("redirect:/course/" + courseId);
        return mav;
    }

    // Método para crear una nueva inscripción
    private CourseEnrollment createNewCourseEnrollment(Student student, String courseId) {
        // Obtener el curso para completar los datos de inscripción
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado con ID: " + courseId));

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourseId(courseId);
        enrollment.setCourseName(course.getCourseName());
        enrollment.setEnglishLevel(course.getEnglishLevel());
        enrollment.setFinalGrade(student.getCourseEnrollments().get(0).getFinalGrade());

        // Calcular el estado
        updateEnrollmentStatus(enrollment);

        return enrollment;
    }

    // Método para actualizar los datos de una inscripción existente
    private void updateCourseEnrollment(CourseEnrollment enrollment, Student student) {
        enrollment.setFinalGrade(student.getCourseEnrollments().get(0).getFinalGrade());
        updateEnrollmentStatus(enrollment);
    }

    // Método para calcular el estado en función de la nota final
    private void updateEnrollmentStatus(CourseEnrollment enrollment) {
        String finalGrade = enrollment.getFinalGrade();
        if ("NP".equalsIgnoreCase(finalGrade)) {
            enrollment.setStatus("No Aprobado");
        } else {
            try {
                double grade = Double.parseDouble(finalGrade);
                enrollment.setStatus(grade >= 3.0 ? "Aprobado" : "No Aprobado");
            } catch (NumberFormatException e) {
                enrollment.setStatus("No Aprobado"); // Valor por defecto si la nota no es válida
            }
        }
    }

    @GetMapping("/edit/{id}")
    public ModelAndView showEditForm(@PathVariable("id") String studentId, 
                                     @RequestParam("courseId") String courseId) {
        ModelAndView mav = new ModelAndView("CrearStudent"); // Nombre de la vista

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            mav.addObject("student", student); // Pasa el estudiante a la vista
        } else {
            // Si el estudiante no existe, creamos un nuevo objeto vacío
            Student newStudent = new Student();
            mav.addObject("student", newStudent); // Pasa el nuevo estudiante a la vista
        }

        mav.addObject("courseId", courseId); // Agrega el courseId al modelo

        // Busca el curso por ID y pasa sus datos al modelo
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            mav.addObject("courseName", course.getCourseName());
            mav.addObject("englishLevel", course.getEnglishLevel());
        }

        return mav;
    }

    
    @GetMapping("/create/{courseId}")
    public ModelAndView showCreateStudentForm(@PathVariable String courseId) {
        ModelAndView mav = new ModelAndView("CrearStudent"); // Nombre de la vista de creación/edición
        Student newStudent = new Student(); // Crear un nuevo objeto Student (sin ID)
        mav.addObject("student", newStudent);
        mav.addObject("courseId", courseId); // Pasa el ID del curso al modelo para el formulario

        // Busca el curso por ID y pasa sus datos al modelo
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            mav.addObject("courseName", course.getCourseName());
            mav.addObject("englishLevel", course.getEnglishLevel());
            mav.addObject("status", course.getStatus());
        }
        
        return mav;
    }

    @GetMapping("/{studentId}/removeCourse/{courseId}")
    public ModelAndView removeCourseEnrollment(@PathVariable("studentId") String studentId, 
                                               @PathVariable("courseId") String courseId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();

            // Eliminar la inscripción específica del curso
            student.getCourseEnrollments().removeIf(enrollment -> enrollment.getCourseId().equals(courseId));

            // Guardar el estudiante actualizado sin la inscripción del curso
            studentRepository.save(student);
        }

        // Redirige de regreso a la página de detalles del curso
        ModelAndView modelAndView = new ModelAndView("redirect:/course/" + courseId);
        modelAndView.addObject("message", "El curso ha sido eliminado del estudiante.");

        return modelAndView;
    }


    @PostMapping("/registro/save")
    public ModelAndView saveStudentRegistration(@ModelAttribute Student student) {
        ModelAndView modelAndView = new ModelAndView("RegistroStudent");

        String result = studentService.registerStudent(
                student.getIdentificationNumber(),
                student.getCourseEnrollments().get(0).getCourseId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail()
        );

        if (result.startsWith("error:")) {
            modelAndView.addObject("isError", true);
            modelAndView.addObject("message", result.replace("error:", ""));
        } else {
            modelAndView.addObject("isError", false);
            modelAndView.addObject("message", result.replace("success:", ""));
        }

        // Recargar datos de cursos para el formulario
        List<Course> activeCourses = courseRepository.findByStatus("Activo");
        modelAndView.addObject("courses", activeCourses);
        modelAndView.addObject("student", student);

        return modelAndView;
    }






    @GetMapping("/registro")
    public ModelAndView showRegistrationForm() {
        ModelAndView modelAndView = new ModelAndView("RegistroStudent");

        // Obtener la lista de cursos activos
        List<Course> activeCourses = courseRepository.findByStatus("Activo");
        modelAndView.addObject("courses", activeCourses);

        // Crear un nuevo estudiante vacío con una inscripción inicial
        Student student = new Student();
        CourseEnrollment enrollment = new CourseEnrollment();
        student.getCourseEnrollments().add(enrollment);
        modelAndView.addObject("student", student);

        return modelAndView;
    }



    
    @GetMapping("/registro/search")
    @ResponseBody
    public ResponseEntity<Student> searchStudent(@RequestParam String identificationNumber) {
        Student student = studentService.findStudentByIdentificationNumber(identificationNumber);
        if (student != null) {
            return ResponseEntity.ok(student);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


}
