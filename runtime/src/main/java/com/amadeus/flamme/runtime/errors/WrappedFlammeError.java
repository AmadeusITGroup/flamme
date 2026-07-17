package com.amadeus.flamme.runtime.errors;

public final class WrappedFlammeError extends RuntimeException {
  private final FlammeImplRuntimeError error;

  public WrappedFlammeError(FlammeImplRuntimeError error) {
    super(error);
    this.error = error;
  }

  public FlammeImplRuntimeError getError() {
    return error;
  }
}
