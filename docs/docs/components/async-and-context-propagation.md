# Async and Context Propagation

## Async Components

In a traditional microservice architecture, microservices often call external services. Such calls should be made asynchronously, i.e. the service should not block while waiting for a response from an external service. The same goes for Flamme components: when Flamme executes a component, it does so by leveraging virtual threads .

Virtual threads are a feature introduced in Java 21. They execute code on regular OS threads, but when blocking code is run inside a virtual thread, the underlying platform thread picks up another virtual thread instead of staying blocked. What's great about virtual threads is that the code you write can look blocking, while your application isn't really blocked. This is in contrast to using `CompletableFuture`, where your code becomes entangled in nested callbacks within nested callbacks, and your application's behavior becomes unclear when reading the code.

Flamme uses `SmallRyeManagedExecutor` and executes each method on a separate virtual thread.  Hence, in your implementation code, you can write blocking calls without fear of blocking your entire application. 

```java
@Unremovable
@FlammeImpl
public class ComponentImpl implements Component {
    @Override
    public Map<String, Message> execute(Map<String, Message> arguments) {
        // Running on a virtual thread, so this blocking call
        // doesn't tie up the underlying platform thread.
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        String result = response.body();
        return Map.of("result-event", StringValue.of(result));
    }
}
```

## Context Propagation


In Quarkus, incoming HTTP/NATS events are initially handled on [Vert.x-managed](https://vertx.io/docs/) threads (often event-loop threads).  
Flamme offloads subscriber/service work to virtual threads to avoid blocking those Vert.x threads with potentially blocking logic.
When Flamme offloads work, it restores two kinds of execution context:
- Thread context (MicroProfile / SmallRye Context Propagation): carries caller metadata (for example logging/security context) across the thread hop.
- Vert.x + CDI execution context: if no Vert.x context is active, Flamme creates/duplicates one and activates CDI request context around handler execution. This keeps Quarkus reactive/runtime behavior and `@RequestScoped` access consistent in async paths.

