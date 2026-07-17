# Multipayload

## `@MultipayloadKey`

A flamme component takes a `Map<String, Message>` as argument and returns it as output.  

`Message` is `com.google.protobuf.Message`. A lot of well known types extend this class, such as `StringValue`, `DoubleValue`, `Int32Value`, `Int64Value`, `NullValue`, `ListValue` and [more](https://protobuf.dev/reference/protobuf/google.protobuf/). 

As stated before, we use a simple java `Map` in order to support multiple `Message` objects as arguments, and we key them by an id, thus the `Map<String, Message>`.

We'll see later that, depending on runtime configuration, a component may receive its arguments from a remote component, in which case it will need to deserialize the arguments coming from the network. It may also need to send its output to a remote component, in which case it will need to serialize the result before sending it.

`@MultipayloadKey` is what tells Flamme which key in the map corresponds to which protobuf type, so it can deserialize each entry correctly before invoking your method.
For example, imagine a component `Greeter` that receives a person's `NAME` and `AGE` from an event, and produces a greeting `String`:

```java
@Flamme(
    serviceName = "greeter"
    consumes = {"source-event"},
    produces = {"greetings-event"}
    multipayloadKeys = {
        @Flamme.MultipayloadKey(id = "AGE", type = Int32Value.class)
        @Flamme.MultipayloadKey(id = "NAME", type = StringValue.class)
    }
)
public interface Greeter {
   Map<String, Message>  greet(Map<String, Message> arguments);
}
```

In your implementation you can then retrieve each payload by its key. 

```java
@Unremovable
@FlammeImpl
public class GreeterImpl implements Greeter {
    Map<String, Message> greet(Map<String, Message> arguments) {
        // retrieve payloads by key.
        String name = ((StringValue) arguments.get("NAME")).getValue();
        int age = ((Int32Value) arguments.get("AGE")).getValue();

        // create a greeting.
        String greeting = "Hello, " + name + ". You are " + age + " years old !";

        // return Map<String, Message> with the greeting.
        return Map.of("GREETING", StringValue.of(greeting));
    }
}
```

In complex applications, it is recommended to keep your key strings in one place (for example a `Keys.java`), and to reuse them when sending or receiving (`Keys.GREETING`, `Keys.NAME`). This avoids hardcoded strings being scattered across your application, and makes debugging easier.

## Serialization / Deserialization

When using Flamme, you won't have to write any specific serialization or deserialization logic. This section aims to explain how Flamme serializes the `Map<String, Message>` under the hood, in case you're curious (feel free to skip it if you're not).

Encoding a `Map<String, Message>` goes as follows: 
- encode the map size.

For each entry in the map: 

- encode the key: 2 bytes length prefix + key bytes in modified UTF-8.
- encode the class name: 2 bytes length prefix + class-name bytes in modified UTF-8.
- encode the size of the instance: 4 bytes.
- encode the instance (`toByteArray()` for protocol buffers): `N` bytes depending on the size.
