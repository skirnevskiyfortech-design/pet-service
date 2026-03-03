package com.example.petservice.exception;


import lombok.extern.slf4j.Slf4j;
import org.openapi.example.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler{


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode("UPLOAD_ERROR");
        error.setMessage(ex.getMessage());
        error.setTimestamp(OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(PetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePetNotFound(PetNotFoundException ex){
        ErrorResponse error = new ErrorResponse();
        error.setCode("PET_NOT_FOUND");
        error.setMessage(ex.getMessage());
        error.setTimestamp(OffsetDateTime.now());
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<ErrorResponse> handleFileTooLarge(FileTooLargeException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode("FILE_TOO_LARGE");
        error.setMessage(ex.getMessage());
        error.setTimestamp(OffsetDateTime.now());
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    @ExceptionHandler(PetAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePetAlreadyExists(PetAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode("PET_ALREADY_EXISTS");
        error.setMessage(ex.getMessage());
        error.setTimestamp(OffsetDateTime.now());
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setCode("VALIDATION_ERROR");
        error.setMessage(ex.getMessage());
        error.setTimestamp(OffsetDateTime.now());
        log.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
