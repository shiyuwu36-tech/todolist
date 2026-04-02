package com.example.todolist.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends ApiException {
  public ValidationException(String message, String details) {
    super("VALIDATION_ERROR", message, details, HttpStatus.BAD_REQUEST);
  }
}
