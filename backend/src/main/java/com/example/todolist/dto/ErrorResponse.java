package com.example.todolist.dto;

public class ErrorResponse {
  private String code;
  private String message;
  private String details;

  public ErrorResponse(String code, String message, String details) {
    this.code = code;
    this.message = message;
    this.details = details;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public String getDetails() {
    return details;
  }
}
