# Copilot agent context: Dispatch Agent

Project: Dispatch Agent (short: dispatch-agent)

Active Technologies
- Java 17
- Spring Boot 3.x (spring-boot-starter-web, spring-boot-starter-actuator)
- Build: Maven

Project surface
- Exposes a lightweight `/health` endpoint returning JSON: { status: string, reason?: string, metadata?: object }
- No persistent storage in this iteration

Notes
- This context file was generated because `.specify/scripts/powershell/update-agent-context.ps1` was available; the script will keep agent files up-to-date from plans.
