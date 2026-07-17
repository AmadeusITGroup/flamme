# Flamme

> A Quarkus extension for writing and managing distributed applications.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)


## Overview

A **Flamme** application is composed of several components. A component is represented by a Java interface annotated with `@Flamme`, containing a single method that uses `Map<String, Message>` (a `MultiPayload`) for both its argument and return types.

Flamme is **network agnostic** and **event-driven** — you do not write networking or serialization code. Instead, you declare within the `@Flamme` annotation which subjects a component subscribes to and publishes to. The framework handles the remaining infrastructure logic.

Using runtime configurations, you can decide which components are co-located in the same process/pod or split across different ones. Flamme ensures all components communicate regardless of their location — via **NATS** for remote components or **direct method invocation** for local ones.


## Installation

Add the Flamme dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.amadeus</groupId>
    <artifactId>flamme</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```


## Documentation

Full documentation is available at [amadeusitgroup.github.io/flamme](https://amadeusitgroup.github.io/flamme).


## License

This project is published under the [MIT License](./LICENSE.md).
