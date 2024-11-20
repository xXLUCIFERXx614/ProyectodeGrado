package com.idiomas.app.repository;

import java.util.List;


import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import com.idiomas.app.entity.Course;
import com.idiomas.app.entity.Professor;
import com.idiomas.app.entity.Student;


public interface StudentRepository extends MongoRepository<Student, String> {
	 Student findByIdentificationNumberAndEmail(String identificationNumber, String email);
	 


	    /**
	     * Busca una lista de estudiantes que están asociados a un curso específico usando la entidad `Course`.
	     * @param course la entidad `Course` con la cual se asociaron los estudiantes.
	     * @return una lista de estudiantes que pertenecen al curso indicado por la entidad `Course`.
	     */
	  
	 List<Student> findByCourseEnrollments_CourseId(String courseId);
	  
	    
	    Optional<Student>findByIdentificationNumber(String identificationNumber );
	    
	    Optional<Student> findById(String id);
}
