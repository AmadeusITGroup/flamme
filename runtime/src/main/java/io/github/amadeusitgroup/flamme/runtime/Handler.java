package io.github.amadeusitgroup.flamme.runtime;

import com.google.protobuf.Any;
import io.github.amadeusitgroup.flamme.runtime.annotations.FlammeImpl;
import io.github.amadeusitgroup.flamme.runtime.errors.FlammeImplRuntimeError;
import io.github.amadeusitgroup.flamme.runtime.utils.Strings;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Handler {

  private static final Logger log = LoggerFactory.getLogger(Broker.class);

  public static Consumer<FlammeMessage> buildServiceHandler(
      Class<?> interfaceClass, Method method, String[] outputSubjects, Broker broker) {
    return message -> {
      try {
        InstanceHandle<?> implHandle = resolveFlammeImpl(interfaceClass);
        Object[] args = constructArgs(method, message);
        Any result = invoke(method, implHandle.get(), args, interfaceClass.getName());
        publish(message, outputSubjects, broker, result);
      } catch (FlammeImplRuntimeError e) {
        // get the error message that the component raised.
        // InvocationTargetException -> FlammeRuntimeException
        String errorMessage =
            Optional.ofNullable(e.getCause())
                .map(Throwable::getCause)
                .map(Throwable::getMessage)
                .orElse(e.getMessage());
        log.atDebug().addArgument(errorMessage).log("component level error message: {}");
        FlammeError error = FlammeError.newBuilder().setErrorMessage(errorMessage).build();
        broker.publish(message.replyTo(), message.error(error));
        log.atError().log(e.getLocalizedMessage());
      }
    };
  }

  private static InstanceHandle<?> resolveFlammeImpl(Class<?> interfaceClass)
      throws FlammeImplRuntimeError {
    InstanceHandle<?> implHandle =
        Arc.container().instance(interfaceClass, FlammeImpl.Literal.INSTANCE);
    if (!implHandle.isAvailable()) {
      throw new FlammeImplRuntimeError(Strings.flammeImplResolutionError(interfaceClass.getName()));
    }
    return implHandle;
  }

  private static Object[] constructArgs(Method method, FlammeMessage message) {
    return switch (method.getParameterCount()) {
      case 0 -> null;
      case 1 -> new Object[] {message.envelope().getPayload()};
      default -> new Object[] {message.envelope().getPayload(), message.headers()};
    };
  }

  private static Any invoke(Method method, Object impl, Object[] args, String className)
      throws FlammeImplRuntimeError {
    try {
      // It is safe to cast because signature was validated at build time.
      return (Any) method.invoke(impl, args);
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new FlammeImplRuntimeError(Strings.invokationError(className), e);
    }
  }

  private static void publish(
      FlammeMessage message, String[] outputSubjects, Broker broker, Any result) {
    if (message.replyTo() != null && outputSubjects.length == 0) {
      broker.publish(message.replyTo(), message.wrap(result));
      return;
    }
    for (String subject : outputSubjects) {
      broker.publish(subject, message.wrap(result));
    }
  }
}
