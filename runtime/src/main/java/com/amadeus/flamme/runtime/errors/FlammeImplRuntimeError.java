package com.amadeus.flamme.runtime.errors;

public class FlammeImplRuntimeError extends Exception {
  public FlammeImplRuntimeError(String message) {
    super(message);
  }

  public FlammeImplRuntimeError(String message, Throwable cause) {
    super(message, cause);
  }
}
