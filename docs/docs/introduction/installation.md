# Installation

## Prerequisites

Since Flamme is a Quarkus extension, it's only suitable for Java applications that already use Quarkus as their main framework.

- Java 21+
- Maven 3.9+
- A running NATS server (for distributed deployments)

## Maven

> Flamme is not yet published to a remote artifactory. You should clone the repository and install it locally. v1.0.0 is coming soon.

```bash
git clone https://github.com/AmadeusITGroup/flamme.git
cd flamme/
mvn clean install
```

Add the dependency to the `pom.xml` of your quarkus application:
```xml
<dependency>
    <groupId>com.amadeus</groupId>
    <artifactId>flamme</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
