package com.example.petservice.exception;

public class FileTooLargeException extends RuntimeException {
    public FileTooLargeException(String message) {
        super(message);
    }

    public FileTooLargeException(long size, long maxSize) {
        super("File size " + size + " bytes exceeds maximum allowed " + maxSize + " bytes");
    }
}