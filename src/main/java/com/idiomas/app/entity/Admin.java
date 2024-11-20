package com.idiomas.app.entity;
import org.springframework.data.annotation.Id;


import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Admins")
public class Admin {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String identificationNumber;

    // Empty constructor
    public Admin() {}

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }
}
