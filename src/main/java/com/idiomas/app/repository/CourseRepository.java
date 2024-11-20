package com.idiomas.app.repository;




import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import com.idiomas.app.entity.Course;
import com.idiomas.app.entity.Professor;

public interface CourseRepository extends MongoRepository<Course, String> {
  
	 List<Course> findByProfessorId(String professorId);

	    /**
	     * Método para buscar cursos por estado.
	     * @param status El estado del curso (por ejemplo, "Activo").
	     * @return Lista de cursos con el estado especificado.
	     */
	    @Query("SELECT c FROM Course c WHERE c.status = :status")
	    List<Course> findByStatus(@Param("status") String status);
   
}


