# Implementation Plan: Dispatch Agent

## Technical Context

**Language/Version**: Java 17

**Primary Dependencies**: Spring Boot 3.x (spring-boot-starter-web, spring-boot-starter-actuator)

**Build Tool**: Maven

**Storage/DB**: N/A (no persistent storage required for initial iteration)

**Project Type**: Backend service (lightweight agent)

**Endpoints**: GET /health (primary), GET /info (optional metadata)

**Non-functional targets**: Response time < 200ms local under normal conditions; lightweight and safe for frequent polling.

## Constitution Check

The repository constitution at `.specify/memory/constitution.md` prescribes project-wide MUSTs that include Java 21 and Gradle as defaults for the broader product.

- Gate: Java version MUST be 21 — Current plan uses Java 17 -> DEVIATION
  - Justification: This initial agent targets a minimal operational surface and aims for broad compatibility across existing hosts that currently run Java 17. The team may upgrade to Java 21 in a follow-up iteration once pairwise compatibility checks and CI updates are scheduled.

- Gate: Build tool MUST be Gradle — Current plan uses Maven -> DEVIATION
  - Justification: Existing org conventions include both Maven and Gradle; Maven is used here for a minimal, widely-known scaffold and to match user preference for quick local builds. We will produce a migration plan to Gradle if the project standardization effort proceeds.

Decision: Both deviations are intentionally justified for this first, low-risk scaffold. These justifications should be reviewed by a lead and recorded in an ADR before merging to mainline.

## Gates / Validation

- Gate A: No unresolved NEEDS CLARIFICATION items in `spec.md` — PASS (no NEEDS CLARIFICATION present)
- Gate B: Plan must include Language/Version and Primary Dependencies — PASS
- Gate C: Any constitution deviations must be justified — PASS (see justifications above)

## Phase 0: Research (research.md)

Goals:
- Resolve any open technical clarifications.
- Decide language/build/runtime choices and record rationale.

Action: See `research.md` for decisions and alternatives considered.

## Phase 1: Design & Contracts

Artifacts to produce:
- `data-model.md` — identifies the health payload and metadata shape
- `contracts/openapi.yaml` — minimal OpenAPI 3 contract describing `/health`
- `quickstart.md` — short developer quickstart (build & run with PowerShell)

## Agent context update

Goal: Update agent-specific context files (Copilot) so future agents know this project uses Java 17 + Spring Boot and exposes a `/health` endpoint.

Method: Run `.specify/scripts/powershell/update-agent-context.ps1 -AgentType copilot`. If the template is missing, create the agent file manually.

## Next steps (after Phase 1)

1. Create small unit test for the `/health` endpoint.
2. Add Maven wrapper (`mvnw`) so the build is reproducible without local Maven install.
3. Consider migrating to Java 21 and Gradle in a follow-up ADR.
