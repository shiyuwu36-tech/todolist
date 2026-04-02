package com.example.todolist.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
  public NotFoundException(String message) {
    super("NOT_FOUND", message, null, HttpStatus.NOT_FOUND);
  }
}
