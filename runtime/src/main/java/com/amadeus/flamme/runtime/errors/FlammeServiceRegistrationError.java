package com.amadeus.flamme.runtime.errors;

public class FlammeServiceRegistrationError extends Exception {
  public FlammeServiceRegistrationError(String message) {
    super(message);
  }

  public FlammeServiceRegistrationError(String message, Throwable cause) {
    super(message, cause);
  }
}
