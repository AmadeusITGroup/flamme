package io.github.amadeusitgroup.flamme.runtime.errors;

public class NatsConnectionError extends Exception {
  public NatsConnectionError(String message) {
    super(message);
  }

  public NatsConnectionError(String message, Throwable cause) {
    super(message, cause);
  }
}
