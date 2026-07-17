package com.amadeus.flamme.runtime.utils;

import com.amadeus.flamme.runtime.FlammeConfig;
import com.amadeus.flamme.runtime.errors.FlammeImplRuntimeError;
import com.amadeus.flamme.runtime.errors.WrappedFlammeError;
import io.nats.client.impl.Headers;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class FlammeUtils {

  @FunctionalInterface
  private interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;
  }

  @FunctionalInterface
  public interface ThrowingRunnable {
    void run() throws Exception;
  }

  public static <T, R> Function<T, R> unchecked(ThrowingFunction<T, R> fn, String errorMessage) {
    return t -> {
      try {
        return fn.apply(t);
      } catch (FlammeImplRuntimeError e) {
        throw new WrappedFlammeError(e);
      } catch (Throwable e) {
        throw new WrappedFlammeError(new FlammeImplRuntimeError(errorMessage, e));
      }
    };
  }

  public static Runnable uncheckedRunnable(ThrowingRunnable fn, String errorMessage) {
    return () -> {
      try {
        fn.run();
      } catch (FlammeImplRuntimeError e) {
        throw new WrappedFlammeError(e);
      } catch (Throwable e) {
        throw new WrappedFlammeError(new FlammeImplRuntimeError(errorMessage, e));
      }
    };
  }

  public static Method resolveMethod(Class<?> interfaceClass) {
    // At build time, FlammeSignatureValidator validates that there is only one method
    // declared in the FlammeComponent.
    return interfaceClass.getDeclaredMethods()[0];
  }

  public static boolean isServiceRemote(String serviceName, FlammeConfig config) {
    FlammeConfig.ServiceConfig serviceConfig = config.services().get(serviceName);
    return serviceConfig != null && serviceConfig.remote();
  }

  public static Map<String, String> getMapFromHeaders(Headers headers) {
    if (headers == null) return new HashMap<>();
    Map<String, String> headerMap = new HashMap<>();
    headers.forEach((k, v) -> headerMap.put(k, v.getFirst()));
    return headerMap;
  }

  public static Headers getHeadersFromMap(Map<String, String> headerMap) {
    Headers headers = new Headers();
    if (headerMap != null) {
      headerMap.forEach(headers::put);
    }
    return headers;
  }

  public static boolean isReplyTo(String subject) {
    return subject.startsWith(Strings.REPLY_TO_PREFIX);
  }

  public static String newReplyTo() {
    return Strings.REPLY_TO_PREFIX + UUID.randomUUID();
  }
}
