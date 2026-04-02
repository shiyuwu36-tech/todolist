package com.example.todolist.exception;

import org.springframework.http.HttpStatus;

public class PayloadTooLargeException extends ApiException {
  public PayloadTooLargeException(String message) {
    super("PAYLOAD_TOO_LARGE", message, null, HttpStatus.PAYLOAD_TOO_LARGE);
  }
}
