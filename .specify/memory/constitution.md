# SIG (Geographic Information System) Constitution

## Core Principles

### I. Meaningful Object-Oriented Design

- Use inheritance only for genuine "is-a" relationships.
- Use polymorphism only when behavior genuinely varies.
- Prefer composition over inheritance.
- Introduce no artificial hierarchies, relationships, or abstractions solely to demonstrate an OOP concept.
- Apply SOLID pragmatically: it should improve cohesion, maintainability, testability, or dependency direction, and MUST NOT produce artificial interfaces or abstractions.
- Every OOP concept applied must map to a real domain need.

### II. Rich, Infrastructure-Independent Domain

- Domain classes must not depend on Spring, Jackson, HTTP, JSON files, Leaflet, or frontend technologies.
- Domain objects contain behavior and protect their invariants.
- No anemic domain models.
- Geographic validity rules are always enforced inside the domain.

### III. Encapsulation and Domain Invariants

- Object state MUST be encapsulated; unrestricted mutation is avoided.
- Unnecessary setters are avoided; objects expose behavior instead of raw mutation.
- Domain invariants are protected inside the domain; invalid state cannot be reached through public behavior.

### IV. Domain Concepts over Primitives

- Prefer explicit domain concepts over primitive values when the concept carries meaningful rules or invariants.
- Avoid primitive obsession without introducing unnecessary abstractions.

### V. Explicit and Meaningful Errors

- Use explicit and meaningful domain/application errors; avoid generic runtime exceptions when a meaningful error can be used.
- Exceptions MUST NOT be silently ignored.
- Technical errors MUST NOT leak directly into user-facing behavior; user-facing messages remain clear and appropriate.

### VI. Simplicity First

- Follow KISS and YAGNI.
- Apply DRY to meaningful conceptual duplication only.
- No speculative abstractions or overengineering.
- No unrequested features or infrastructure.

### VII. Behavior-Oriented Testing (NON-NEGOTIABLE)

- Tests focus on behavior: domain rules, application behavior, invalid cases, and relevant boundary cases.
- Tests must assert meaningful behavior, never implementation details.
- Tests run on every change: `./mvnw test`, `./mvnw verify`.
- Tests are never removed, disabled, or weakened to make the build pass.

### VIII. Clear Responsibility Boundaries

- Controllers handle HTTP/presentation concerns.
- Repositories handle persistence (local JSON files behind repository abstractions).
- Application services coordinate operations.
- Domain contains rules and behavior.

### IX. Academic Traceability

- OOP concepts MUST be identifiable and justifiable in the implemented design.
- UML class diagrams MUST reflect the actual domain design.
- Use case documentation MUST reflect implemented behavior.
- UML and technical documentation MUST remain synchronized with relevant changes.
- Final documentation MUST explain where and why encapsulation, abstraction, inheritance, polymorphism, object relationships, and code reuse are applied.

## Technical Constraints

- Java 21, Spring Boot, Spring Web, Maven.
- No databases, JPA, Hibernate, or Spring Data.
- Persistence via local JSON files using Jackson, always behind repository abstractions.
- Persistence operations MUST preserve data integrity: failed writes MUST NOT leave local JSON data corrupted or partially written.
- Frontend: vanilla HTML, CSS, and JavaScript with Leaflet, part of the same application; no frontend framework.
- GeoJSON is allowed where appropriate.
- Base package: `edu.udistrital.sig`.
- Language: source code identifiers, technical documentation, specifications, and commit messages are written in English; all user-facing interface text, validation messages, errors, labels, and controls are written in Spanish.
- Testing with JUnit 5 and Spring Boot Test; Mockito only when isolation is genuinely necessary.
- Users of each type are preloaded from local JSON files.
- Every new dependency MUST have a concrete purpose; prefer existing dependencies or the Java standard library when sufficient.

## Development Workflow

- The repository is managed with git.
- Commits follow Conventional Commits: `feat:`, `fix:`, `chore:`, `refactor:`, `docs:`, `test:`.
- Every commit has a descriptive title and a body describing the change.
- The project must compile, run its tests, and meet the acceptance criteria before work is considered complete.
- The specification is the source of truth; spec and plan are updated before code changes.

## Governance

- This constitution supersedes all other development practices.
- The specification is the source of truth for expected system behavior; requirements are never invented beyond what the specification defines.
- Amendments require documentation and approval and follow Semantic Versioning (semver):
  - MAJOR: backward-incompatible removal or redefinition of a principle.
  - MINOR: addition of a new principle/section or materially expanded guidance.
  - PATCH: clarifications, wording, and typo fixes.
- Every change must verify compliance with this constitution.

**Version**: 2.0.0 | **Ratified**: 2026-08-13 | **Last Amended**: 2026-08-13
