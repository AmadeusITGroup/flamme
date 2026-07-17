
# Flamme

> A Quarkus extension for writing and managing distributed applications.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)
[![Java 21+](https://img.shields.io/badge/Java-21%2B-blue?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Quarkus 3.x](https://img.shields.io/badge/Quarkus-3.x-4695EB?logo=quarkus&logoColor=white)](https://quarkus.io/)
[![NATS](https://img.shields.io/badge/NATS-messaging-27AAE1?logo=natsdotio&logoColor=white)](https://nats.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Build](https://github.com/amadeusitgroup/flamme/actions/workflows/deploy-docs.yml/badge.svg)](https://github.com/amadeusitgroup/flamme/actions)


## Overview


Flamme is a Quarkus extension which addresses the current problems in modern microservice architectures.

A **Flamme** application is composed of several components. A component is represented by a Java interface annotated with `@Flamme`, containing a single method that uses `Map<String, Message>` (a `MultiPayload`) for both its argument and return types.

Flamme is **network agnostic** and **event-driven** — you do not write networking or serialization code. Instead, you declare within the `@Flamme` annotation which subjects a component subscribes to and publishes to. The framework handles the remaining infrastructure logic.

Using runtime configurations, you can decide which components are co-located in the same process/pod or split across different ones. Flamme ensures all components communicate regardless of their location — via **NATS** for remote components or **direct method invocation** for local ones.


## Installation

> [!IMPORTANT]
> Flamme is not yet published to a remote artifactory. Follow the [Getting Started](#getting-started) guide to install it locally first.

Add the Flamme dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.amadeus</groupId>
    <artifactId>flamme</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```


## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- A running [NATS](https://nats.io) server (for distributed deployments)

### Clone and build

```bash
git clone https://github.com/amadeusitgroup/flamme.git
cd flamme
mvn clean compile -DskipTests 
```

### Run the example
You can find an example application and instructions on how to run it [here](./flamme-example/README.md).


## Documentation

Full documentation is available at [amadeusitgroup.github.io/flamme](https://amadeusitgroup.github.io/flamme).


## License

This project is published under the [MIT License](./LICENSE.md).
