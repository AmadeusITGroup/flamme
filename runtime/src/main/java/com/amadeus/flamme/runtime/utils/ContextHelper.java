package com.amadeus.flamme.runtime.utils;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;

public class ContextHelper {

  private static Vertx vertx;

  private static Vertx vertx() {
    if (vertx == null) {
      vertx = Arc.container().instance(Vertx.class).get();
    }
    return vertx;
  }

  public static void runWithDuplicatedContext(Runnable task) {
    if (Vertx.currentContext() != null) {
      task.run();
      return;
    }
    ContextInternal duplicate = ((ContextInternal) vertx().getOrCreateContext()).duplicate();
    duplicate.beginDispatch();
    ManagedContext requestContext = Arc.container().requestContext();
    boolean activatedRequest = !requestContext.isActive();
    if (activatedRequest) {
      requestContext.activate();
    }
    try {
      task.run();
    } finally {
      if (activatedRequest) {
        requestContext.terminate();
      }
      duplicate.endDispatch(null);
    }
  }
}
