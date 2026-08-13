# Research: Geographic Information System

Phase 0 output for `specs/001-geographic-information-system`. Each section records a technical decision, its rationale, and alternatives considered.

## 1. Offline Leaflet integration

**Decision**: Bundle Leaflet's CSS and JS distribution files as local static assets under `src/main/resources/static/leaflet/`, served by Spring Boot. No webjar or npm dependency.

**Rationale**: Keeps the application fully offline and self-contained (constitution Technical Constraints; FR-016). Avoids an extra build-time dependency, consistent with the dependency policy (every dependency must have a concrete purpose; prefer existing resources).

**Alternatives considered**:
- WebJar `org.webjars.npm:leaflet`: adds a dependency and resource-serving indirection with no functional benefit for an offline single-app deployment.
- CDN: rejected because it requires internet access at runtime.

## 2. Local vector base map

**Decision**: Bundle a simplified GeoJSON map of Colombia (`colombia-boundaries.geojson`) with administrative (department) boundaries, placed under `src/main/resources/data/` and loaded at runtime by Leaflet as a local `L.geoJSON` layer. It serves as geographic context only: no labels beyond administrative names, no street detail.

**Rationale**: FR-016 requires a local vector base map with administrative boundaries, offline, without external tiles. A simplified GeoJSON keeps file size small while providing enough context to locate entities.

**Alternatives considered**:
- Raster tiles (local tile set): larger artifacts, more complex setup, unnecessary detail for the academic scope.
- No base map (blank canvas): would make entities hard to locate; clarification selected administrative boundaries as context.

**Note**: The GeoJSON file must be small enough to be a reasonable repo resource; coarse simplification is acceptable (spec: "does not need the level of detail of a commercial web map"). Simplification/licensing of the boundary source is addressed during implementation.

## 3. Atomic JSON persistence

**Decision**: Writes use a write-to-temporary-file-then-atomic-rename strategy: serialize the full dataset to a temp file in the same directory, then `Files.move(tmp, target, ATOMIC_MOVE)`. Reads load from the canonical path only.

**Rationale**: Constitution requires failed writes to never leave local JSON corrupted or partially written. Atomic rename guarantees the target file is either the old or the new complete version.

**Alternatives considered**:
- In-place `ObjectMapper.writeValue(file, ...)`: can leave a truncated/corrupt file on failure; rejected.
- Single-file journaling/versioned files: more complexity than needed for an academic dataset.

## 4. Proximity criterion for LineString and Polygon

**Decision**: A line or polygon entity matches a proximity query if the **minimum distance from the query coordinate to the geometry** is within the search radius. For polygons, a coordinate inside the polygon has distance 0 (always a match). Distances are computed with the Haversine formula between the query coordinate and the closest point on the geometry (for polygons, check containment first; otherwise distance to each segment).

**Rationale**: FR-009 requires a simple, documented criterion. Minimum-distance-to-geometry is simple, deterministic, and matches user intuition ("is this near the road/zone?"). It is appropriate for the academic scope.

**Alternatives considered**:
- Centroid distance only: cheap but wrong for long roads or large zones.
- Full buffer/intersection geometry: advanced spatial analysis, explicitly out of scope.
- Bounding-box approximation: cheap but produces false positives; documented criterion should be more faithful.

## 5. Geographic math and geometry handling

**Decision**: Implement a small, self-contained geometry module in the domain (coordinate validity, geometry validity per FR-004, Haversine distance, point-to-segment distance, point-in-polygon via ray casting). No GIS geometry library dependency.

**Rationale**: The required rules are basic (FR-004, clarification 7). A hand-written module is small, testable, keeps the domain infrastructure-free (constitution principle II), and avoids a heavyweight dependency. Behavior-oriented tests cover validity and distance cases.

**Alternatives considered**:
- JTS (`org.locationtech.jts`): powerful but far beyond scope and adds a dependency for functionality not required by the spec.
- WGS84-to-planar projection library: CRS transformations are explicitly out of scope.

## 6. Authentication mechanism

**Decision**: Simple local credential check: users are preloaded in `users.json` with a username, a password hash, and a role. Login exchanges credentials for a token held by the frontend; controllers enforce role-based authorization on every write operation (server-side), so direct access is denied too (FR-024).

**Rationale**: Clarification 4 requires a simple local mechanism with no external identity provider and no registration. Enforcing authorization on the server (not only in the UI) satisfies FR-024.

**Alternatives considered**:
- Full OAuth2/Spring Security session flows: overkill for the academic scope; Spring Security adds complexity not required by the spec.
- Client-side-only checks: would fail FR-024 (direct access must be rejected).

## 7. Frontend architecture

**Decision**: A single `index.html` with a single vanilla JS file (`js/app.js`) and one shared stylesheet (`css/style.css`), no framework and no build step. The UI is composed of two views toggled by the application state: a login view (brand panel + authentication form) and an app view (sidebar with identity, user/role and logout, navigation tabs, query filters, results list, entity management form, and the Leaflet map). Map features are rendered with custom flat `divIcon` markers (not Leaflet's default icon), styled popups and tooltips, plus overlays for a legend and a visible-entity counter.

**Rationale**: Constitution constrains the frontend to vanilla HTML/CSS/JS + Leaflet with no framework. A single JS file avoids a build step and keeps the app self-contained; a shared CSS theme with design tokens keeps the login and application views visually consistent (FR-029). Custom flat markers, popups, and the legend/counter are presentation-only and do not change domain or API behavior.

**Alternatives considered**:
- Any SPA framework: rejected by the constitution.
- Leaflet default markers/icons: rejected for visual consistency with the custom theme (heavy shadows, inconsistent look).
- Multiple JS modules/ES modules: adds load-order complexity without benefit for an app of this size.

## 8. Entity natures vs categories

**Decision**: Entity **nature** is a domain concept (point of interest, road, neighborhood, institution, commercial establishment, zone) and **category** is a classification attribute used as a query criterion. The design does NOT create a class per nature; nature and category are data values, while behavior that genuinely varies is expressed through the geometry types.

**Rationale**: Clarification 9 forbids forcing a class-per-type design. Geometry is the real source of polymorphic behavior (validation + rendering + distance), so the domain models geometry polymorphism, not per-nature classes.

**Alternatives considered**:
- Subclass per entity nature: artificial hierarchy; rejected.
- Nature as an enum and category as a predefined list: adopted.
