package com.example.todolist.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
  private final String code;
  private final HttpStatus status;
  private final String details;

  public ApiException(String code, String message, String details, HttpStatus status) {
    super(message);
    this.code = code;
    this.status = status;
    this.details = details;
  }

  public String getCode() {
    return code;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getDetails() {
    return details;
  }
}
