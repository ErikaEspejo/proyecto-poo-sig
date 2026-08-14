# Quickstart: Geographic Information System

Phase 1 validation guide. Refer to [contracts/api.md](./contracts/api.md) and [data-model.md](./data-model.md) for details; this document only explains how to prove the feature works end-to-end.

## Prerequisites

- Java 21 JDK.
- Maven wrapper (`mvnw`) or Maven 3.9+.
- No internet required for the core flows (the local vector base map is bundled and works offline); the optional OpenStreetMap base layer requires internet and falls back automatically to the local map (FR-037..FR-040). Build dependencies are resolved from the configured Maven repository.

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
2. Login as `admin`; the main interface loads with the sidebar (identity, user/role, logout), the navigation tabs (Búsqueda / Registrar), and the map (OpenStreetMap when its tiles can load, otherwise the local base map via automatic fallback) with seed entities. The "Registrar" tab is available.
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

### 7. Base maps and fallback (FR-037..FR-040, FR-039)
1. With a connection, the map opens on OpenStreetMap; its attribution is visible at the bottom-right; the selector shows "OpenStreetMap" and "Mapa local".
2. Select "Mapa local" in the selector → the local vector map is shown without rebuilding the map or moving center/zoom; entity layers remain visible.
3. Select "OpenStreetMap" again → OSM tiles reload and the policy restarts.
4. Disable the network while OpenStreetMap is active → after 3 consecutive tile errors the map switches to "Mapa local", showing "No fue posible cargar OpenStreetMap. Se activó el mapa local." and preserving entities, results, selection, and any geometry being drawn; there is no automatic return to OpenStreetMap.
5. Re-enable the network and select "OpenStreetMap" manually → tiles load again and the counter is reset.

### 8. Cursor coordinates (FR-041)
1. Move the mouse over the map → the bottom-left control shows "Latitud: 4.609710 | Longitud: -74.081750" style values with six decimals, updating in real time.
2. Move the cursor off the map → the control shows "Latitud: -- | Longitud: --".
3. Clicking on the map never adds a vertex unless draw mode is active (see scenario 9).

### 9. Click-based geometry (FR-042..FR-044)
1. As `consulta`, open the application; clicks on the map never add vertices and the "Registrar" tab is unavailable.
2. As `admin`, go to the "Registrar" tab:
   - Select "Punto" and click the map → the point is placed; a second click replaces it.
   - Select "Línea" and click twice, then press "Terminar línea/polígono" → the line commits; with a single click the finish is rejected with a Spanish message.
   - Select "Polígono" and click three distinct points, then press "Terminar línea/polígono" → the ring closes automatically exactly once; with fewer than three distinct points the finish is rejected.
3. Switch the base map while drawing → vertices, preview, and form state are preserved.
4. Press "Borrar geometría" → only the temporal geometry is cleared (stored entities are untouched).
5. Send an invalid save (e.g., empty name) → the Spanish error appears and the form and geometry are preserved; after a successful save the drawing state clears.
6. Edit an existing entity → its geometry is loaded as an editable preview; changing the geometry type asks for confirmation when vertices would be lost; canceling clears the preview and restores normal map behavior.

## Verification

Run the full test suite before considering the feature complete:

```bash
./mvnw test
./mvnw verify
```

Expected: all behavior-oriented tests pass, including domain validation rules, invalid cases, boundary cases, and permission checks.
