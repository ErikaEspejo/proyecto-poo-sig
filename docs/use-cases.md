# Use Cases — Geographic Information System

Documentation of the implemented behavior (matches `spec.md` user stories and `contracts/api.md`). Actors: **Consultation user** (read-only) and **Administrator** (read + write).

## UC-01 Authenticate

- **Actors**: Consultation user, Administrator
- **Trigger**: User opens the application or the session expired
- **Flow**:
  1. User enters username and password (`POST /api/auth/login`).
  2. The system validates credentials against preloaded local users (`users.json`, SHA-256 hash).
  3. On success, the system issues a token and returns the role.
  4. On failure, the system rejects with a Spanish message ("Credenciales inválidas.").
- **Result**: Session active; UI and server enforce the user's role permissions.

## UC-02 Browse entities and map

- **Actors**: Consultation user, Administrator
- **Flow**:
  1. The system loads the entity list and both base maps: OpenStreetMap tiles (online, initial layer) and the local vector map (Colombia departments).
  2. The user views each entity on the map: Point as marker, LineString as line, Polygon as area.
  3. The user clicks an entity to see its descriptive information.
  4. While moving the cursor, the map shows live WGS84 coordinates in a control (Latitud/Longitud, 6 decimals; neutral state when the cursor leaves the map).
- **Result**: Full read-only visualization with online imagery and live coordinates (FR-015, FR-016, FR-037, FR-041).

## UC-09 Switch base map

- **Actors**: Consultation user, Administrator
- **Trigger**: User selects a base layer, or OpenStreetMap tiles fail
- **Flow**:
  1. The layer selector (top-left, `L.control.layers`) offers "OpenStreetMap" (online) and "Mapa local" (offline vector).
  2. The user switches layers; center and zoom are preserved.
  3. If 3 consecutive OpenStreetMap tile errors occur, the system automatically switches to "Mapa local" and shows a non-blocking notice ("No fue posible cargar OpenStreetMap. Se activó el mapa local.").
  4. Re-selecting OpenStreetMap manually resets the failure counter and retries.
- **Result**: The map always renders; online imagery is optional and never blocks the core functionality (FR-016, FR-037..FR-040).

## UC-10 Define geometry by clicking (Administrator)

- **Actors**: Administrator
- **Trigger**: User registers/edits an entity with the Register/Edit tab open and geometry not locked
- **Flow**:
  1. The user chooses the geometry type and clicks the map to add points (Point: a single click defines/replaces it; LineString/Polygon: clicks accumulate vertices with a live preview).
  2. "Terminar línea/polígono" commits the geometry (≥2 points for a line; ≥3 distinct points for a polygon, ring closed automatically); the geometry is then locked.
  3. "Borrar geometría" clears the temporary geometry and re-enables clicking; changing the geometry type with pending geometry asks for confirmation.
  4. Outside this mode (search tab, consultation role, or locked geometry) clicks never add geometry, so selection, popups and tooltips keep working.
- **Result**: Geometry is captured by clicking with validation feedback in the form; a rejected save keeps the form and geometry intact (FR-042..FR-045).

## UC-03 Register an entity (Administrator)

- **Actors**: Administrator
- **Trigger**: User wants to add a geographic entity
- **Flow**:
  1. User fills name, nature, category, optional attributes and geometry (Point/LineString/Polygon) on the map.
  2. The system validates data and geometry (WGS84 ranges, ring closure, mandatory fields).
  3. On success, `POST /api/entities` returns `201` and the entity persists atomically.
- **Alternatives**: Invalid data → `400` with Spanish message; non-administrator → `403`; missing/invalid token → `401`.
- **Result**: New entity visible in list and map and persisted across restarts (FR-001, FR-012).

## UC-04 Update an entity (Administrator)

- **Actors**: Administrator
- **Flow**:
  1. User selects an entity and modifies its data/geometry.
  2. `PUT /api/entities/{id}` replaces the entity after full validation.
- **Alternatives**: Nonexistent id → `404`, stored data unchanged; invalid data → `400`.
- **Result**: Entity updated and persisted (FR-010).

## UC-05 Delete an entity (Administrator)

- **Actors**: Administrator
- **Flow**: User confirms deletion; `DELETE /api/entities/{id}` removes the entity.
- **Alternatives**: Nonexistent id → `404`; non-administrator → `403`.
- **Result**: Entity disappears from list and map (FR-011).

## UC-06 Query entities

- **Actors**: Consultation user, Administrator
- **Trigger**: User filters entities
- **Flow** (`GET /api/entities/query`, at least one criterion):
  - By category (id or name).
  - By free text on name/description.
  - By free text on attribute values.
  - By proximity: latitude + longitude + radius in km (minimum-distance criterion; polygon containment = 0).
  - Several criteria may be combined with AND semantics (each restricts the result set further; FR-036).
- **Result**: Matching entities returned and highlighted on the map; empty results show a "no results" message, not an error (FR-007, FR-008, FR-009, FR-019).

## UC-07 List predefined categories

- **Actors**: Consultation user, Administrator
- **Flow**: `GET /api/categories` returns predefined categories (id + Spanish name).
- **Result**: Categories used in filters and entity registration (FR-013, FR-014).

## UC-08 Load base map

- **Actors**: Consultation user, Administrator
- **Flow**: `GET /api/map/basemap` returns the bundled simplified Colombia GeoJSON; the frontend renders it as the local vector layer registered in the selector.
- **Result**: The local vector map renders offline and is the automatic fallback when OpenStreetMap cannot load (FR-016, FR-038).

## Priority / Coverage

| Use case | User story | Priority |
|----------|-----------|----------|
| UC-01, UC-08 | US3 | P2 |
| UC-02, UC-09 | US2 / US4 | P1 |
| UC-03, UC-04, UC-05, UC-10 | US1 / US4 | P1 |
| UC-06, UC-07 | US2 | P1 |
