# Dispatch Agent

This is a minimal Spring Boot application scaffold for "Dispatch Agent".

Run (PowerShell):

```powershell
# Build
mvn -B package

# Run
mvn spring-boot:run
```

Or run the produced jar:

```powershell
java -jar target\dispatch-agent-0.1.0-SNAPSHOT.jar
```

Health endpoint:

GET http://localhost:8080/health -> { "status": "UP" }
