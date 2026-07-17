package com.amadeus.flamme.runtime;

import com.amadeus.flamme.runtime.errors.FlammeImplRuntimeError;
import com.amadeus.flamme.runtime.payload.PayloadCodec;
import com.amadeus.flamme.runtime.transport.TransportClient;
import com.amadeus.flamme.runtime.transport.TransportDispatcher;
import com.amadeus.flamme.runtime.transport.TransportMessage;
import com.amadeus.flamme.runtime.utils.ContextHelper;
import com.amadeus.flamme.runtime.utils.FlammeUtils;
import com.amadeus.flamme.runtime.utils.Strings;
import com.google.protobuf.Message;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.context.SmallRyeManagedExecutor;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Broker {
  private static final Logger log = LoggerFactory.getLogger(Broker.class);

  @Inject private TransportClient transportClient;

  @Inject private PayloadCodec payloadCodec;

  private ManagedExecutor executor;

  private Map<String, List<Consumer<FlammeMessage>>> localSubscriptions = new ConcurrentHashMap<>();
  private Map<String, CompletableFuture<Map<String, Message>>> replyFutures =
      new ConcurrentHashMap<>();
  private Map<String, Map<String, String>> replyMultiPayloadKeys = new ConcurrentHashMap<>();
  private Set<String> natsRouteSubjects = new HashSet<>();
  private List<Runnable> deferredNatsSubscriptions = new ArrayList<>();

  public Map<String, List<Consumer<FlammeMessage>>> getLocalSubscriptions() {
    return localSubscriptions;
  }

  public Broker() {
    this.executor =
        SmallRyeManagedExecutor.builder()
            .withExecutorService(Executors.newVirtualThreadPerTaskExecutor())
            .propagated(ThreadContext.ALL_REMAINING)
            .build();
  }

  void onStart(@Observes @Priority(2) StartupEvent event) {
    if (isNatsAvailable()) {
      deferredNatsSubscriptions.forEach(Runnable::run);
      registerSharedInboxSubscriptions();
    }
    deferredNatsSubscriptions.clear();
  }

  private void registerSharedInboxSubscriptions() {
    TransportDispatcher dispatcher = transportClient.dispatcher();
    String wildCard = Strings.REPLY_TO_PREFIX + ">";
    dispatcher.subscribe(
        wildCard,
        (message) -> {
          executor.submit(
              FlammeUtils.uncheckedRunnable(
                  () -> {
                    String replyTo = message.subject();
                    var replySubjectHandlerOptional = replySubjectHandler(replyTo);
                    if (replySubjectHandlerOptional.isEmpty()) return;
                    var multiPayloadKeysOptional = replyMultipayloadKeys(replyTo);
                    var multiPayloadKeys = multiPayloadKeysOptional.orElse(Map.of());
                    Optional<String> errorMessageOptional =
                        payloadCodec.decodeError(message.data());
                    if (errorMessageOptional.isPresent()) {
                      replySubjectHandlerOptional
                          .get()
                          .completeExceptionally(
                              new FlammeImplRuntimeError(errorMessageOptional.get()));
                    } else {
                      replySubjectHandlerOptional
                          .get()
                          .complete(payloadCodec.decodePayload(message.data(), multiPayloadKeys));
                    }
                  },
                  Strings.ERROR_WHEN_RECEIVING_REPLY));
        });
  }

  void routeMessagesToNats(String subject) {
    natsRouteSubjects.add(subject);
  }

  void publish(String subject, FlammeMessage message) {
    if (FlammeUtils.isReplyTo(subject)) {
      boolean handledLocally = handleReplySubjectLocally(subject, message);
      if (!handledLocally) {
        forwardReplyToNats(subject, message);
      }
      return;
    }
    publishToSubscribers(subject, message);
  }

  private boolean handleReplySubjectLocally(String subject, FlammeMessage message) {
    var replySubjectHandlerOptional = replySubjectHandler(subject);
    if (replySubjectHandlerOptional.isEmpty()) return false;
    var replyFuture = replySubjectHandlerOptional.get();
    replyFuture.complete(message.payload());
    return true;
  }

  private void forwardReplyToNats(String subject, FlammeMessage message) {
    if (!FlammeUtils.isReplyTo(subject)) return;
    try {
      transportClient.publish(
          new TransportMessage(
              subject, message.headers(), payloadCodec.encodePayload(message.payload()), null));
    } catch (Throwable t) {
    }
  }

  private void publishToSubscribers(String subject, FlammeMessage message) {
    fanOutLocally(subject, message);
    if (isNatsAvailable() && natsRouteSubjects.contains(subject)) {
      publishToNats(subject, message);
    }
  }

  private void fanOutLocally(String subject, FlammeMessage message) {
    List<Consumer<FlammeMessage>> subscribers = localSubscriptions.get(subject);
    if (subscribers != null) {
      subscribers.forEach(
          s ->
              executor.submit(
                  () -> {
                    ContextHelper.runWithDuplicatedContext(
                        () -> {
                          try {
                            s.accept(message);
                          } catch (Throwable t) {
                          }
                        });
                  }));
    }
  }

  private void publishToNats(String subject, FlammeMessage message) {
    try {
      TransportMessage transportMessage =
          new TransportMessage(
              subject,
              message.headers(),
              payloadCodec.encodePayload(message.payload()),
              message.replyTo());
      transportClient.publish(transportMessage);
    } catch (Throwable t) {
    }
  }

  private Optional<Map<String, String>> replyMultipayloadKeys(String replyTo) {
    return Optional.ofNullable(replyMultiPayloadKeys.remove(replyTo));
  }

  private Optional<CompletableFuture<Map<String, Message>>> replySubjectHandler(String subject) {
    return Optional.ofNullable(replyFutures.remove(subject));
  }

  void registerNatsSubscription(
      String subject, Consumer<FlammeMessage> handler, Map<String, String> multipayloadKeys) {
    Runnable subscribeAction =
        () -> {
          TransportDispatcher dispatcher = transportClient.dispatcher();
          if (dispatcher == null) {
            return;
          }
          dispatcher.subscribe(
              subject,
              (message) -> {
                executor.submit(
                    () -> {
                      ContextHelper.runWithDuplicatedContext(
                          () -> {
                            try {
                              Map<String, com.google.protobuf.Message> payload =
                                  payloadCodec.decodePayload(message.data(), multipayloadKeys);
                              handler.accept(
                                  new FlammeMessage(payload, message.headers(), message.replyTo()));
                            } catch (Throwable t) {
                              log.atError().log(Strings.failedToProcessMessage(subject));
                            }
                          });
                    });
              });
        };

    if (!transportClient.isAvailable()) {
      deferredNatsSubscriptions.add(subscribeAction);
      return;
    }
    subscribeAction.run();
  }

  void registerLocalSubscription(String subject, Consumer<FlammeMessage> handler) {
    localSubscriptions.computeIfAbsent(subject, (s) -> new ArrayList<>()).add(handler);
  }

  void cancelReply(String replyTo) {
    replyFutures.remove(replyTo);
    replyMultiPayloadKeys.remove(replyTo);
  }

  void subscribeForReply(
      String replyTo,
      CompletableFuture<Map<String, com.google.protobuf.Message>> replyFuture,
      Map<String, String> multiPayloadKeys) {
    replyFutures.put(replyTo, replyFuture);
    replyMultiPayloadKeys.put(replyTo, multiPayloadKeys);
  }

  private boolean isNatsAvailable() {
    return transportClient.isAvailable();
  }

  @PreDestroy
  void onDestroy() {
    executor.shutdown();
  }
}
