package com.amadeus.flamme.runtime;

import com.amadeus.flamme.runtime.annotations.AnnotationData;
import com.amadeus.flamme.runtime.errors.FlammeServiceRegistrationError;
import com.amadeus.flamme.runtime.utils.FlammeUtils;
import com.amadeus.flamme.runtime.utils.Strings;
import com.google.protobuf.Message;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Recorder
public class FlammeRecorder {

  private final RuntimeValue<FlammeConfig> config;

  public FlammeRecorder(RuntimeValue<FlammeConfig> config) {
    this.config = config;
  }

  public void registerService(String interfaceClassName, AnnotationData annotationData)
      throws FlammeServiceRegistrationError {
    try (InstanceHandle<Broker> brokerHandle = Arc.container().instance(Broker.class)) {
      Broker broker = brokerHandle.get();
      Class<?> interfaceClass;
      try {
        interfaceClass =
            Thread.currentThread().getContextClassLoader().loadClass(interfaceClassName);
      } catch (ClassNotFoundException cause) {
        throw new FlammeServiceRegistrationError(
            Strings.classNotFoundThreadContext(interfaceClassName), cause);
      }
      Method method = FlammeUtils.resolveMethod(interfaceClass);
      boolean isServiceRemote =
          FlammeUtils.isServiceRemote(annotationData.serviceName(), config.getValue());
      Arrays.stream(annotationData.consumers())
          .forEach(
              consumerSubject -> {
                if (isServiceRemote) {
                  // if service is remote we route the messages published to the subject to NATS.
                  broker.routeMessagesToNats(consumerSubject);
                } else {
                  Consumer<FlammeMessage> handler =
                      Handler.buildServiceHandler(
                          interfaceClass, method, annotationData.producers(), broker);
                  Class<?> payloadType =
                      method.getParameterCount() > 0 ? method.getParameterTypes()[0] : null;
                  // if service is local we subscribe locally and create a NATS subscription to
                  // listen for remote
                  broker.registerLocalSubscription(consumerSubject, handler);
                  if (payloadType != null) {
                    broker.registerNatsSubscription(
                        consumerSubject, handler, annotationData.multipayloadKeys());
                  }
                }
              });
    }
  }

  public Supplier<?> createProxyInstance(String interfaceclassName, AnnotationData annotationData) {
    return () -> {
      Class<?> interfaceClass;
      try {
        interfaceClass =
            Thread.currentThread().getContextClassLoader().loadClass(interfaceclassName);
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException(Strings.classNotFoundThreadContext(interfaceclassName));
      }

      Broker broker = Arc.container().instance(Broker.class).get();
      return Proxy.newProxyInstance(
          Thread.currentThread().getContextClassLoader(),
          new Class[] {interfaceClass},
          buildProxyMethod(broker, annotationData));
    };
  }

  @SuppressWarnings("unchecked")
  private InvocationHandler buildProxyMethod(Broker broker, AnnotationData annotation) {
    return (proxy, method, args) -> {
      String replyToSbuject = FlammeUtils.newReplyTo();
      CompletableFuture<Map<String, Message>> replyFuture = new CompletableFuture<>();
      replyFuture.orTimeout(config.getValue().replyTimeout(), TimeUnit.SECONDS);
      replyFuture.whenComplete(
          (result, exception) -> {
            if (exception != null) {
              broker.cancelReply(replyToSbuject);
            }
          });
      broker.subscribeForReply(replyToSbuject, replyFuture, annotation.multipayloadKeys());
      Map<String, Message> payloadMessage = (Map<String, Message>) args[0];
      Map<String, String> headers = args.length > 1 ? (Map<String, String>) args[1] : null;
      FlammeMessage message = new FlammeMessage(payloadMessage, headers, replyToSbuject);
      // publish messages to the gateway's producers
      for (String subject : annotation.producers()) {
        broker.publish(subject, message);
      }
      return replyFuture;
    };
  }
}
