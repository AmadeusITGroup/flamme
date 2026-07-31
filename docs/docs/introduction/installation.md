# Installation

## Prerequisites

Since Flamme is a Quarkus extension, it's only suitable for Java applications that already use Quarkus as their main framework.

- Java 21+
- Maven 3.9+
- A running NATS server (for distributed deployments)

## Maven

Add the dependency to the `pom.xml` of your quarkus application:

```xml
<dependency>
    <groupId>io.github.amadeusitgroup</groupId>
    <artifactId>flamme</artifactId>
    <version>${flamme.version}</version>
</dependency>
```

Use the latest release version, available on the [releases page](https://github.com/amadeusitgroup/flamme/releases).
