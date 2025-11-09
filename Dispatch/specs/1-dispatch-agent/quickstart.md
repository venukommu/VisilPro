# Quickstart: Dispatch Agent

Build and run locally (PowerShell):

```powershell
cd 'c:\Users\Venu\Documents\GDP\Dispatch'
# Build
mvn -B package

# Run from Maven
mvn spring-boot:run

# Or run the packaged jar
java -jar target\dispatch-agent-0.1.0-SNAPSHOT.jar
```

Health check (after service is running):

GET http://localhost:8080/health

Expected response:

```json
{ "status": "UP" }
```

Notes:
- If you don't have Maven installed, add the Maven Wrapper (`mvnw`) to the project to make builds reproducible.
- To add readiness checks for dependencies (DB, queues), update `HealthController` or Spring Boot readiness probes in a follow-up iteration.
