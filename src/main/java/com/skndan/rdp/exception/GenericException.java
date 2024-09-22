package com.skndan.rdp.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenericException extends RuntimeException {
  private final int statusCode;
  private final String message;

  public GenericException(int statusCode, String message) {
    this.statusCode = statusCode;
    this.message = message;
  }
}