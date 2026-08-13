# Implementation Plan: Geographic Information System

**Branch**: `001-geographic-information-system` | **Date**: 2026-08-13 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-geographic-information-system/spec.md`

## Summary

Build a self-contained academic Geographic Information System (GIS) as a single Spring Boot application with a vanilla JavaScript + Leaflet frontend. Users authenticate with preloaded local accounts (consultation and administrator) through a login screen. Authorized users manage geographic entities (Point, LineString, Polygon) stored in local JSON files behind repository abstractions. All users can query entities by category, descriptive attributes, or proximity (coordinate + radius) and view results on an offline local vector base map. The frontend presents a sidebar-based interface with navigation tabs (search / registration), results as cards with Spanish nature/category labels, an empty state, and a map with legend, visible-entity counter, tooltips, and popups. Validation rules and domain invariants live in the domain layer; persistence writes are atomic to preserve data integrity.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot, Spring Web, Jackson; frontend uses vanilla HTML/CSS/JS with Leaflet (bundled locally as static assets) and a custom CSS theme shared by the login and application views; GeoJSON for geometry representation and the base map.

**Storage**: Local JSON files managed through repository abstractions (Jackson for serialization/deserialization). Seed data and the base map are bundled as resources.

**Testing**: JUnit 5, Spring Boot Test, Mockito (only when isolation is genuinely necessary).

**Target Platform**: Local desktop browser; the application runs as a single Spring Boot process and serves its own UI.

**Project Type**: Web application (Spring Boot backend + vanilla JS frontend served by the same application).

**Performance Goals**: No strict performance targets; dataset is small (tens to low hundreds of entities). Correctness, clarity, maintainability, and meaningful OOP demonstration take priority over scale.

**Constraints**: Self-contained and offline-capable; no external services, tile servers, or APIs; no database, JPA, Hibernate, or Spring Data; no frontend framework; user-facing interface in Spanish; code/specs/documentation in English.

**Scale/Scope**: Academic scope; small dataset; two user types (consultation, administrator); predefined entity natures and categories.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status |
|------|--------|
| Java 21, Spring Boot/Web, Maven | PASS |
| No databases, JPA, Hibernate, or Spring Data | PASS |
| Persistence via local JSON + Jackson, behind repository abstractions | PASS |
| Failed writes must not leave JSON corrupted or partially written (atomic writes) | PASS (planned in persistence design) |
| Frontend: vanilla HTML/CSS/JS + Leaflet, part of the same app, no framework | PASS |
| Base package `edu.udistrital.sig` | PASS |
| English for code/docs/specs; Spanish for all user-facing text | PASS |
| Testing JUnit 5 + Spring Boot Test; Mockito only when necessary | PASS |
| Behavior-oriented tests (domain rules, invalid cases, boundary cases) | PASS (test plan covers these) |
| Encapsulation, invariants protected in domain, no unrestricted mutation | PASS (domain design) |
| No artificial abstractions; SOLID applied pragmatically | PASS |
| Every new dependency has a concrete purpose; prefer stdlib | PASS (leaflet only; map data via stdlib/Jackson) |
| Academic traceability (UML, use cases, docs synchronized with design) | PASS (documentation artifacts planned) |

No violations requiring justification; **Complexity Tracking** is left empty.

## Project Structure

### Documentation (this feature)

```text
specs/001-geographic-information-system/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
pom.xml

src/main/java/edu/udistrital/sig/
├── domain/
│   ├── model/                 # Entity, Geometry (Point/LineString/Polygon),
│   │                          #   Category, User, Role, Coordinates
│   ├── repository/            # Repository interfaces (ports)
│   └── exception/             # Domain exceptions
├── application/
│   └── service/               # Use cases (entity management, query, auth)
└── infrastructure/
    ├── persistence/           # JSON repository implementations
    └── web/                   # Controllers and DTOs

src/main/resources/
├── static/                    # Frontend served by the application
│   ├── index.html             # Login view + app view (sidebar, tabs, map)
│   ├── css/style.css          # Shared theme (login + app, responsive)
│   ├── js/app.js              # Vanilla JS (auth, map, queries, CRUD)
│   └── leaflet/               # Leaflet assets bundled locally (offline)
├── data/                      # Seed data
│   ├── users.json             # Preloaded users per type
│   ├── entities.json          # Seed geographic entities (200-item academic dataset)
│   └── colombia-boundaries.geojson  # Local vector base map (offline)

src/test/java/edu/udistrital/sig/
├── domain/                    # Domain unit tests (validation, rules)
├── application/               # Use-case tests
└── infrastructure/            # Persistence + web integration tests
```

**Structure Decision**: Single Maven Spring Boot project (no modules) with a pragmatic layered layout that preserves the constitution's responsibility boundaries: `domain` (rules and invariants), `application` (use case orchestration), `infrastructure/persistence` (JSON repositories), `infrastructure/web` (HTTP controllers and DTOs). The frontend is served from `src/main/resources/static` by the same application, keeping it self-contained. `domain` has no dependency on Spring, Jackson, HTTP, JSON, or Leaflet.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

None. All gates pass without violations.
