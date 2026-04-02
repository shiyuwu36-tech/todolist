package com.example.todolist.exception;

import org.springframework.http.HttpStatus;

public class UnsupportedMediaTypeException extends ApiException {
  public UnsupportedMediaTypeException(String message) {
    super("UNSUPPORTED_MEDIA_TYPE", message, null, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
  }
}
