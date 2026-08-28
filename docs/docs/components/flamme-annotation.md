# The Flamme Annotation

## Component

Flamme's main abstraction is a component. A component is simply a Java interface with a single method, annotated with `@Flamme`, along with a class implementing that interface.

A Flamme component is network-agnostic, location-transparent and event-driven. In practice, this means it never contains code related to connecting to the network, sending or receiving messages, or serialization/deserialization. It only contains business logic, nothing more.

To create a Flamme component, you simply write a Java interface and annotate it with `@Flamme`:

```java
@Flamme(
    serviceName = "component"
    consumes = {},
    produces = {}
)
public interface Component {
    Any execute(Any args);
}
```

Here, the `@Flamme` annotation takes a service name, a `consumes` array and a `produces` array. We'll go into these in more detail later, in the Brokers section. For now, all you need to know is that they're arrays of events: a subject in `consumes` provides the argument for the method, and the method's result is published to the events listed in `produces`. We'll keep both arrays empty for now.

## Method Signature

Let's talk more about the method signature. All `@Flamme` methods should follow the same signature as in the component above: they take a `com.google.protobuf.Any` as their argument.


## Headers

Flamme also supports optional headers as a second method parameter.

Accepted signatures are:

```java
Any execute(Any payload);
Any execute(Any payload, Map<String, String> headers);
```

Headers are propagated by Flamme across local in-process routing and remote transport, so your business code can read metadata (for example tracing ids, tenant ids, locale, or auth context) without adding transport-specific code.

Example:

```java
@Flamme(
    serviceName = "component-b",
    consumes = {"message-published-a"},
    produces = {"message-published-b"}
)
public interface ComponentB {
    Any execute(Any payload, Map<String, String> headers);
}
```

If you annotate a component with `@Flamme` and the above structure is not followed, an error will be thrown at build time.

## FlammeImpl
After writing your interface you can then go ahead and write your implementation.

```java
@Unremovable
@FlammeImpl
public class ComponentImpl implements Component {
    Any execute(Any args) {
        // business logic goes here.
    };
}
```

The `@FlammeImpl` annotation is an extra qualifier which tells Flamme, at build time, that this specific bean is the implementation of our component. Since you won't call this bean directly anywhere in your code, Quarkus's CDI container may treat it as unused and remove it during build-time optimization. Adding `@Unremovable` prevents this from happening.

You might still be wondering how this component will communicate with other components, and how multiple components can make up an application. We're getting there, I promise!
