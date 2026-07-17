package com.amadeus.flamme.deployment.errors;

public class InvalidFlammeComponent extends Exception {
  public InvalidFlammeComponent(String message) {
    super(message);
  }

  public InvalidFlammeComponent(String message, Throwable cause) {
    super(message, cause);
  }
}
