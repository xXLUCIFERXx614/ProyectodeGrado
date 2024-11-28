package com.idiomas.app.repository;




import org.springframework.data.mongodb.repository.MongoRepository;


import org.springframework.stereotype.Repository;

import com.idiomas.app.entity.Admin;


@Repository

public interface AdminRepository extends MongoRepository<Admin, String> {
	
	Admin findByEmailAndIdentificationNumber(String email, String identificationNumber);
}

