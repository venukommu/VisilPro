# Research: Dispatch Agent (phase 0)

Decision: Implement a minimal Spring Boot (Java 17) service using Maven and Spring Actuator for the health surface.

Rationale
- Java 17: chosen for widest immediate compatibility with existing developer machines and CI images; avoids forcing an urgent platform upgrade for a small scaffold.
- Maven: standard, well-known build tool; easier to run quickly on developer machines. We'll add a Maven Wrapper (`mvnw`) so CI and developers don't need a global Maven installation.
- Spring Boot + Actuator: provides a simple, battle-tested health pattern and will allow expanding to readiness and dependency checks later.

Alternatives considered
- Java 21 + Gradle — aligns with repository constitution, but requires platform and CI updates. Consider for next iteration.
- Quarkus or Micronaut — lower memory footprint, faster startup, but more friction for teams currently familiar with Spring.

Dependencies to track
- Spring Boot 3.x, Micrometer/Actuator for metrics and health endpoints.

Open issues
- None for this iteration. If we add external dependency checks (DB, queues), list them explicitly so readiness checks can include dependency probes.
