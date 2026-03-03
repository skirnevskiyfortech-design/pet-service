package com.example.petservice.exception;

public class PetAlreadyExistsException extends RuntimeException {
    public PetAlreadyExistsException(Long id) {
        super("Pet with id "+ id +" already exists");
    }
}
