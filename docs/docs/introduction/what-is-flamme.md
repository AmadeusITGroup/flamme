# What is Flamme ?
Flamme is a Quarkus extension which addresses the current problems in modern microservice architectures.

When developing microservice applications, people tend to define logical boundaries between their different services: two services are separated modularly because they execute distinct business logics. From there, it's natural to also separate those services physically: each service lives on its own process/pod/machine and communicates with other services via the network.

The issue is that, with microservices, logical boundaries end up implying physical boundaries, which is not optimal for performance. When an application is oversplit, the overhead of serializing/deserializing data and sending it over the network increases.

This is exactly what Flamme addresses: it provides developers with a programming model that lets them write network-agnostic and location-transparent components, and decide later, meaning at runtime, which components will run together on the same process and which will be separated on different machines. It goes without saying that this decision should be solely based on what benefits performance more.

Flamme also tackles more problems that microservices bring. Most of which are raised by [this paper from google](https://sigops.org/s/conferences/hotos/2023/papers/ghemawat.pdf). Specifically, Flamme lets you package your entire application as a single Quarkus jar, which can then be deployed either as a distributed topology or as a monolith, simply by tweaking some runtime configurations. This addresses problems related to slow application development, correctness, and atomic rollouts.
