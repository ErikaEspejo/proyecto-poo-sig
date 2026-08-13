# Quickstart: Geographic Information System

Phase 1 validation guide. Refer to [contracts/api.md](./contracts/api.md) and [data-model.md](./data-model.md) for details; this document only explains how to prove the feature works end-to-end.

## Prerequisites

- Java 21 JDK.
- Maven wrapper (`mvnw`) or Maven 3.9+.
- No internet required at runtime; build dependencies are resolved from the configured Maven repository.

## Setup

```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`. Seed data (users, categories, entities, base map) is initialized on first execution into local writable JSON files (FR-027).

## Preloaded accounts

| User | Role | Use to test |
|------|------|-------------|
| `admin` | ADMINISTRATOR | write operations (create/update/delete) |
| `consulta` | CONSULTATION | read-only operations and denied writes |

*Exact credentials are defined in seed data `src/main/resources/data/users.json`.*

## Validation scenarios

### 1. Login and roles
1. Open `http://localhost:8080`; the login screen appears (brand panel + authentication form).
2. Login as `admin`; the main interface loads with the sidebar (identity, user/role, logout), the navigation tabs (Búsqueda / Registrar), and the map with the local base map and seed entities (no internet). The "Registrar" tab is available.
3. Login as `consulta`; the "Registrar" tab is hidden and result cards show no edit/delete actions; verify that a direct API write attempt returns `403 Forbidden` (FR-024).

### 2. Register an entity (FR-001)
1. As `admin`, go to the "Registrar" tab, fill in the form (name, nature, category, geometry type), click on the map to define the geometry, and save.
2. Restart the application and confirm the entity is still present (FR-012).

### 3. Invalid data rejected (FR-004, FR-025)
1. Attempt to create an entity with a latitude of 100 → rejected with a clear Spanish message.
2. Attempt to create a polygon with an unclosed ring → rejected.
3. Attempt to create an entity without a category → rejected.

### 4. Queries (FR-007, FR-008, FR-009)
1. In the "Búsqueda" tab, use the filters (text, attribute, category) and the "Búsqueda por ubicación" group (latitude, longitude, radius) to query by category → only matching entities returned and highlighted on the map; the results show a counter and each card shows a nature icon, the name, and Spanish nature/category labels (e.g., "Punto de interés · Turismo").
2. Query by attribute text → only matching entities returned.
3. Query by proximity (lat/lon/radius) → entities within the radius returned; verify a line/polygon matches via the documented minimum-distance criterion.
4. Run a query with no matches → empty result set with the empty-state message "No se encontraron entidades" (not an error; FR-019).

### 5. Update and delete (FR-010, FR-011)
1. As `admin`, edit an entity from its result card (opens the form in the "Registrar" tab) and confirm the change persists after restart.
2. Delete an entity from its result card and confirm it disappears from list and map.
3. Attempt to update/delete a nonexistent id → `404 Not Found`, stored data unchanged.

### 6. Map visualization (FR-015, FR-016)
1. Confirm points render as flat markers, LineStrings as lines, polygons as areas; the map legend shows how each type is represented.
2. Hover an entity on the map → its name appears as a tooltip; click it → its descriptive information is shown in a popup.
3. Select an entity in the results list → it is highlighted in the list and on the map; the floating counter shows the number of visible entities.
4. Disconnect from the network and confirm the map still renders (offline base map).

## Verification

Run the full test suite before considering the feature complete:

```bash
./mvnw test
./mvnw verify
```

Expected: all behavior-oriented tests pass, including domain validation rules, invalid cases, boundary cases, and permission checks.
