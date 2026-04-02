package com.example.todolist.exception;

import org.springframework.http.HttpStatus;

public class StorageException extends ApiException {
  public StorageException(String message, String details) {
    super("STORAGE_ERROR", message, details, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
