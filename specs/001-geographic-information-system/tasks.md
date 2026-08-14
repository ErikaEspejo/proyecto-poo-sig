---
description: "Task list template for feature implementation"
---

# Tasks: Geographic Information System

**Input**: Design documents from `/specs/001-geographic-information-system/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Included. Behavior-oriented tests are REQUIRED by the project constitution (principle VII): domain rules, application behavior, invalid cases, and boundary cases. Tests are written before implementation and must FAIL initially.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Single project at repository root: `src/main/java/edu/udistrital/sig/`, `src/main/resources/`, `src/test/java/edu/udistrital/sig/`
- Package base: `edu.udistrital.sig`; layers: `domain`, `application`, `infrastructure.persistence`, `infrastructure.web`
- Frontend served from `src/main/resources/static/`; seed data from `src/main/resources/data/`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Create Maven Spring Boot project with Java 21 in `pom.xml` (Spring Boot Web, Jackson; no JPA/Hibernate/Spring Data)
- [x] T002 [P] Create package structure `edu.udistrital.sig` (`domain`, `application`, `infrastructure/persistence`, `infrastructure/web`) under `src/main/java/`
- [x] T003 [P] Bundle Leaflet CSS/JS locally under `src/main/resources/static/leaflet/` (offline; no CDN, no webjar)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**CRITICAL**: No user story work can begin until this phase is complete

- [x] T004 Create geometry domain module in `src/main/java/edu/udistrital/sig/domain/model/` (abstract `Geometry`; `Point`, `LineString`, `Polygon`; coordinate range validation per FR-004; Haversine distance; point-to-segment distance; point-in-polygon via ray casting) — domain has no Spring/Jackson/HTTP dependency
- [x] T005 Create `GeographicEntity`, `Category`, `EntityNature` (enum), `User`, `Role` (enum) in `src/main/java/edu/udistrital/sig/domain/model/` (encapsulated state, no unnecessary setters, invariants per data-model.md)
- [x] T006 Create domain exceptions in `src/main/java/edu/udistrital/sig/domain/exception/` (e.g., `InvalidGeometryException`, `EntityNotFoundException`, `UnauthorizedOperationException`)
- [x] T007 Create repository interfaces in `src/main/java/edu/udistrital/sig/domain/repository/` (EntityRepository, CategoryRepository, UserRepository)
- [x] T008 Implement atomic JSON persistence (write temp file + atomic rename; failed writes never corrupt data) in `src/main/java/edu/udistrital/sig/infrastructure/persistence/` using Jackson
- [x] T009 Create seed data `users.json`, `categories.json`, `entities.json` under `src/main/resources/data/` (one consultation + one administrator user; categories; academic dataset covering all geometry types)
- [x] T010 Create `colombia-boundaries.geojson` local vector base map under `src/main/resources/data/` (simplified, offline)
- [x] T011 Implement authentication (login check against preloaded users, token issuance) and authorization filter (admin required for writes, server-side) in `src/main/java/edu/udistrital/sig/infrastructure/web/`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Register and manage geographic entities (Priority: P1) (MVP)

**Goal**: An authorized (administrator) user creates, reads, updates, and deletes geographic entities with valid Point/LineString/Polygon geometry; changes persist across restarts.

**Independent Test**: Register an entity with valid data, restart the app, confirm it persists; invalid geometry and missing data are rejected with Spanish messages; non-admin writes are denied.

### Tests for User Story 1 (REQUIRED - write first, ensure they FAIL)

- [x] T012 [P] [US1] Domain unit tests for geometry validity rules (Point/LineString/Polygon shapes, coordinate ranges, polygon closure, boundary cases) in `src/test/java/edu/udistrital/sig/domain/`
- [x] T013 [P] [US1] Domain unit tests for `GeographicEntity` invariants (missing name/category/geometry rejected) in `src/test/java/edu/udistrital/sig/domain/`
- [x] T014 [US1] Application tests for entity use cases (create/update/delete, nonexistent id rejected with data unchanged) in `src/test/java/edu/udistrital/sig/application/`

### Implementation for User Story 1

- [x] T015 [P] [US1] Implement `EntityService` (create/update/delete/find, validates invariants, authorization-aware) in `src/main/java/edu/udistrital/sig/application/service/`
- [x] T016 [US1] Implement JSON `EntityRepository` (atomic writes) in `src/main/java/edu/udistrital/sig/infrastructure/persistence/`
- [x] T017 [US1] Implement CRUD controllers `POST/GET/PUT/DELETE /api/entities` per `contracts/api.md` with admin authorization and Spanish validation messages in `src/main/java/edu/udistrital/sig/infrastructure/web/`
- [x] T018 [P] [US1] Create CRUD frontend (entity form, list, update/delete actions) in `src/main/resources/static/` (vanilla JS, Spanish labels/messages)
- [x] T019 [US1] Wire CRUD UI to API client with error display (Spanish) in `src/main/resources/static/js/`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Query entities and view them on a map (Priority: P1)

**Goal**: Users query entities by category, descriptive attributes, and proximity (coordinate + radius) and see results as a list and on an offline map, rendered per geometry type.

**Independent Test**: Run a category query and confirm only matching entities return and are highlighted on the map; a proximity query uses the documented minimum-distance criterion; no-match shows an empty-state message, not an error.

### Tests for User Story 2 (REQUIRED - write first, ensure they FAIL)

- [x] T020 [P] [US2] Domain unit tests for distance/proximity (point distance, line min-distance, polygon containment = 0, boundary of radius) in `src/test/java/edu/udistrital/sig/domain/`
- [x] T021 [US2] Application tests for query use case (category, attribute, proximity filters; empty result set not an error) in `src/test/java/edu/udistrital/sig/application/`

### Implementation for User Story 2

- [x] T022 [P] [US2] Implement `QueryService` (category / attribute text / proximity filters using minimum-distance criterion) in `src/main/java/edu/udistrital/sig/application/service/`
- [x] T023 [US2] Implement `GET /api/entities/query` and `GET /api/categories` per `contracts/api.md` in `src/main/java/edu/udistrital/sig/infrastructure/web/`
- [x] T024 [P] [US2] Create Leaflet map controller loading offline base map from `data/colombia-boundaries.geojson` in `src/main/resources/static/js/`
- [x] T025 [P] [US2] Create query UI (filters + results list) in `src/main/resources/static/`
- [x] T026 [US2] Render geometry per type (markers/lines/polygons) and show entity details on click in `src/main/resources/static/js/`
- [x] T027 [US2] Show empty-results message in Spanish (not an error) in `src/main/resources/static/js/`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Use the system as a consultation-only user (Priority: P2)

**Goal**: A consultation-only user browses, queries, and views entities but cannot create, update, or delete them; write attempts are denied server-side even via direct access.

**Independent Test**: A consultation-only user completes queries and map browsing; every write attempt returns 403.

### Tests for User Story 3 (REQUIRED - write first, ensure they FAIL)

- [x] T028 [P] [US3] Application tests for authorization (consultation user denied create/update/delete) in `src/test/java/edu/udistrital/sig/application/`
- [x] T029 [US3] Web integration tests for `403 Forbidden` on non-admin writes via API in `src/test/java/edu/udistrital/sig/infrastructure/`

### Implementation for User Story 3

- [x] T030 [P] [US3] Implement login UI (Spanish) in `src/main/resources/static/` with role-aware navigation
- [x] T031 [US3] Hide/disable write controls for consultation users while keeping server-side enforcement (FR-024) in `src/main/resources/static/js/`
- [x] T032 [US3] Verify seed `users.json` contains one consultation user and one administrator; add missing seed if needed

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [x] T033 [P] Persistence integrity test: simulated failed write leaves JSON uncorrupted in `src/test/java/edu/udistrital/sig/infrastructure/`
- [x] T034 [P] Documentation: UML class diagram of the actual domain design in `docs/uml/`
- [x] T035 [P] Documentation: use case documentation reflecting implemented behavior in `docs/use-cases.md`
- [x] T036 [P] Documentation: final OOP traceability report (where/why encapsulation, abstraction, inheritance, polymorphism, relationships, reuse are applied) in `docs/`
- [x] T037 Run `quickstart.md` validation scenarios end-to-end
- [x] T038 Run `./mvnw test` and `./mvnw verify`; fix any failures
- [x] T039 Final review against constitution v2.0.0 (SOLID pragmático, sin abstracciones artificiales, política de dependencias, idioma UI español)

---

## Phase 7: UI/UX polish (post-implementation, after Phase 6)

**Purpose**: Visual redesign of the frontend, consistent with the login screen; presentation-only, no changes to domain, API contracts, or behavior.

- [x] T040 Redesign login screen (split layout with brand/cartography panel, styled form, password visibility toggle, loading state) in `src/main/resources/static/`
- [x] T041 Redesign main interface layout (sidebar with app identity, user bar with avatar/role/logout, navigation tabs Búsqueda/Registrar) in `src/main/resources/static/`
- [x] T042 Redesign search view (grouped filters with "Búsqueda por ubicación" subsection, result cards with nature icon and Spanish nature/category labels, results counter, empty state) in `src/main/resources/static/`
- [x] T043 Redesign entity management view (form styling, primary/secondary/link action hierarchy) in `src/main/resources/static/`
- [x] T044 Map layer polish (flat `divIcon` markers, styled popups and hover/sticky tooltips, legend, floating visible-entity counter) in `src/main/resources/static/js/` and `src/main/resources/static/css/`
- [x] T045 Selection highlighting between results list and map (list selection highlights map feature; map selection highlights list card) in `src/main/resources/static/js/`
- [x] T046 Expand bundled academic seed dataset to 200 entities covering natures, categories, and geometry types in `src/main/resources/data/entities.json`
- [x] T047 Run `./mvnw test` and `./mvnw verify` after UI changes; confirm no regression

---

## Phase 8: Convergence review fixes (post-implementation, after Phase 7)

**Purpose**: Address findings from the reopened convergence review: combined query semantics, use-case-level authorization, DIP for password hashing, and documentation accuracy.

- [x] T048 Fix `QueryService` to apply all provided criteria with AND semantics and report the full set of applied criteria (`matchedBy`) in `src/main/java/edu/udistrital/sig/application/service/QueryService.java`
- [x] T049 Enforce write authorization inside the entity use case (`EntityService` requires `Role.canModifyEntities()`) and pass the authenticated role from `EntityController` in `src/main/java/edu/udistrital/sig/`
- [x] T050 Introduce the `PasswordHasher` port in `application` with `Sha256PasswordHasher` (infrastructure) so `AuthService` no longer depends on infrastructure
- [x] T051 Update `spec.md` (status Implemented, provenance note, FR-036), `contracts/api.md` (`matchedBy` semantics), and `checklists/requirements.md`
- [x] T052 Correct `docs/traceability.md` (AuthInterceptor is conditional role enforcement, not polymorphism; partial OCP; DIP port; test counts) and `docs/use-cases.md` (AND semantics)
- [x] T053 Run `./mvnw verify`; validate the Maven Wrapper and update the final test count in `docs/traceability.md`

---

## Phase 9: Map enhancement — base layers, fallback, coordinates, and click-based drawing (post-implementation, after Phase 8)

**Purpose**: Frontend-only enhancement per FR-037..FR-045: OpenStreetMap as an optional online base map with an explicit fallback policy to the bundled local map, a base-map selector (`L.control.layers`), a cursor-coordinate control, and click-based geometry definition. No changes to domain, application services, REST contracts, permissions, or JSON persistence.

- [ ] T054 Update `spec.md` (amend FR-016; add FR-037..FR-045, User Story 4, assumptions, clarifications), `plan.md`, `tasks.md`, `quickstart.md`, `contracts/api.md` note, `checklists/requirements.md`, and `data-model.md` note
- [ ] T055 Implement dual base maps in `src/main/resources/static/js/app.js`: OSM `tileLayer` (maxZoom 19, attribution) as initial layer when tiles can load + bundled local vector layer as alternative; `L.control.layers` selector with Spanish options "OpenStreetMap"/"Mapa local"; no map rebuild, center/zoom and entity layers preserved on switch; automatic fallback reflected in the selector
- [ ] T056 Implement the fallback policy in `src/main/resources/static/js/app.js`: `tileerror` counter of consecutive failures, `tileload` resets it, fallback at 3 consecutive errors activates "Mapa local" exclusively with the Spanish notice "No fue posible cargar OpenStreetMap. Se activó el mapa local."; no automatic return during the session; manual OSM re-selection resets the counter and starts a new attempt; single isolated error does not trigger fallback
- [ ] T057 Implement the cursor-coordinate control (Leaflet `L.Control`, `mousemove`/`mouseout`, six decimals, neutral state, both roles, non-blocking) in `src/main/resources/static/js/app.js` and `src/main/resources/static/css/style.css`
- [ ] T058 Implement click-based geometry definition gating in `src/main/resources/static/js/app.js`: draw mode active only for administrators with the register/edit view open and the form in geometry-definition mode; Point replace-on-click; LineString >= 2 coordinates; Polygon >= 3 distinct points with single auto-close; "Terminar línea/polígono" validation with Spanish messages; GeoJSON `[longitude, latitude]`
- [ ] T059 Implement draw-state preservation and cleanup in `src/main/resources/static/js/app.js` and `index.html`: preserve vertices/preview/type/form/edit mode across base-map switches; "Borrar geometría" clears only temporal geometry; cancel clears and deactivates draw mode; rejected save keeps form+geometry and shows the Spanish error in the register view; edit loads geometry as editable preview; geometry-type change asks for confirmation when vertices would be lost
- [ ] T060 Update academic docs: `docs/use-cases.md` (UC-02, UC-08), `docs/design.md`, `docs/manual-de-instalacion-y-uso.md`, `docs/traceability.md`
- [ ] T061 Run `./mvnw test` and `./mvnw verify`; rebuild the jar, restart the app, and perform manual browser verification of the acceptance scenarios (OSM visible with attribution, fallback, selector, coordinates, drawing, state preservation)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - US1 and US2 (both P1) can proceed in parallel once Foundation is ready
  - US3 (P2) after US1 (it reuses its authorization/UI)
- **Polish (Final Phase)**: Depends on all desired user stories being complete
- **UI/UX Polish (Phase 7)**: Post-implementation; depends on all phases above being complete; presentation-only
- **Convergence review fixes (Phase 8)**: Post-implementation; depends on Phase 7; correctness/documentation, no new features
- **Map enhancement (Phase 9)**: Post-implementation; depends on Phase 8; presentation-only (frontend base maps, fallback, coordinates, click-based drawing)

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational - no dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational - uses geometry + repositories from US1; independently testable
- **User Story 3 (P2)**: Depends on US1 (reuses CRUD + authorization) and seed users

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Models before services
- Services before endpoints
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, US1 and US2 can start in parallel (if capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Domain unit tests for geometry validity rules in src/test/java/edu/udistrital/sig/domain/"
Task: "Domain unit tests for GeographicEntity invariants in src/test/java/edu/udistrital/sig/domain/"
# Application tests for entity use cases run after the two domain test sets pass
```

```bash
# Launch independent implementation files together:
Task: "Implement EntityService in src/main/java/edu/udistrital/sig/application/service/"
Task: "Create CRUD frontend in src/main/resources/static/"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational - Foundation ready
2. Add User Story 1 - Test independently - Deploy/Demo (MVP!)
3. Add User Story 2 - Test independently - Deploy/Demo
4. Add User Story 3 - Test independently - Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
3. Developer C: User Story 3 after US1 authorization stabilizes

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group (Conventional Commits, descriptive title + body)
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
