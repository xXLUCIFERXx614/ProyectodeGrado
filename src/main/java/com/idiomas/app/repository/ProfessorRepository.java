package com.idiomas.app.repository;

import org.springframework.data.mongodb.repository.MongoRepository;


import org.springframework.stereotype.Repository;
import com.idiomas.app.entity.Professor;

@Repository
public interface ProfessorRepository extends MongoRepository<Professor, String> {
	 Professor findByEmailAndIdentificationNumber(String email, String identificationNumber);
    
    
}


