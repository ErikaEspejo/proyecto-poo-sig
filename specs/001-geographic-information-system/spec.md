# Feature Specification: Geographic Information System

**Feature Branch**: `001-geographic-information-system`

**Created**: 2026-08-13

**Status**: Implemented

**Input**: User description: "Build an academic Geographic Information System (GIS) that allows registering, storing, querying, updating, deleting and visualizing information associated with different geographic locations, demonstrating a meaningful application of Object-Oriented Programming."

**Provenance**: FR-001 through FR-029 were defined during the initial specification phase, before implementation. FR-030 through FR-036 were incorporated after implementation: FR-030 to FR-035 document the UI polish session (2026-08-13), and FR-036 documents the query-combination semantics decided during the convergence review. FR-037 through FR-045 document the map-enhancement session (2026-08-13): OpenStreetMap as an optional online base map, the explicit fallback policy, the base-map selector, the cursor-coordinate control, and click-based geometry definition; FR-016 was amended in the same session to make the local vector map the required offline base map instead of the only base map. The convergence review (2026-08-13) revalidated the implemented behavior against this specification.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Register and manage geographic entities (Priority: P1)

An authorized user registers a geographic entity (e.g., a point of interest, a road, a neighborhood, an institution, a commercial establishment, or a zone) with its descriptive information and its spatial representation (point, line, or polygon). The user can later view, modify, and delete those entities, and the changes remain available across application sessions.

**Why this priority**: Entity management (CRUD) is the core value of the system. Without it, nothing can be queried or visualized.

**Independent Test**: An authorized user registers an entity with a valid spatial representation, restarts the application, and confirms the entity is still present and editable.

**Acceptance Scenarios**:

1. **Given** an authorized user, **When** they register a new entity with valid descriptive and spatial data, **Then** the entity is stored and immediately available for query and visualization.
2. **Given** an existing entity, **When** an authorized user modifies its information, **Then** the updated information replaces the previous one and is preserved across sessions.
3. **Given** an existing entity, **When** an authorized user deletes it, **Then** the entity no longer appears in queries or on the map.
4. **Given** an entity with invalid spatial data (e.g., malformed coordinates, unclosed polygon ring), **When** a user tries to register or update it, **Then** the system rejects the operation and explains the validity rule violated in Spanish.
5. **Given** a user without write permissions, **When** they attempt to register, modify, or delete an entity, **Then** the operation is denied even if attempted directly rather than through the visible interface.
6. **Given** an update or delete request for an entity that does not exist, **When** the user submits it, **Then** the system rejects the operation, leaves stored data unchanged, and reports that the entity was not found.

### User Story 2 - Query entities and view them on a map (Priority: P1)

A user browses the registered geographic entities through the map and through queries. Queries can be filtered by location (a coordinate and a radius), by category, and by descriptive attributes. The results are shown both as a readable list and positioned on the map.

**Why this priority**: Visualization and query are how users derive value from the stored data and are the second half of the core value.

**Independent Test**: A user runs a query by category, confirms only matching entities are returned, and confirms the results are highlighted on the map.

**Acceptance Scenarios**:

1. **Given** stored entities with different categories, **When** a user queries by a category, **Then** only entities of that category are returned.
2. **Given** stored entities, **When** a user queries by descriptive attributes, **Then** only entities matching those attributes are returned.
3. **Given** stored entities, **When** a user queries by location with a coordinate and a radius, **Then** only entities within that radius are returned.
4. **Given** a query with no matches, **When** the user executes it, **Then** the system returns an empty result set and clearly informs the user that no matching entities were found, without treating it as an error.
5. **Given** stored entities with point, line, and polygon representations, **When** the user views the map, **Then** each entity is displayed according to its spatial representation (markers for points, lines for LineStrings, areas for polygons).
6. **Given** an entity displayed on the map, **When** the user selects it, **Then** the system shows the entity's relevant descriptive information.

### User Story 3 - Use the system as a consultation-only user (Priority: P2)

A consultation-only user can browse the map, run queries, and view entity details, but cannot register, modify, or delete entities.

**Why this priority**: Role separation protects data integrity and is an explicit requirement, but it can be layered after the core flows work.

**Independent Test**: A consultation-only user completes a query and is denied a write operation.

**Acceptance Scenarios**:

1. **Given** a consultation-only user, **When** they query and view entities, **Then** all read operations succeed.
2. **Given** a consultation-only user, **When** they attempt to register, modify, or delete an entity, **Then** the system denies the operation.
3. **Given** a user opening the application, **When** they authenticate with their preloaded local credentials, **Then** they access the system with the permissions of their role (consultation or administrator).

### User Story 4 - Map base layers, cursor coordinates, and click-based geometry (Priority: P2)

A user browses entities choosing between OpenStreetMap (online, detailed) and the bundled local vector map (offline). The system automatically falls back to the local map when OpenStreetMap cannot load, without blocking any other functionality. The map shows the cursor coordinates in real time. Administrators define or re-define entity geometry (Point, LineString, Polygon) by clicking on the map while the register/edit view and the geometry-definition mode are active.

**Why this priority**: The local map, querying, and CRUD already satisfy the core flows; the online base map, coordinate display, and click-based drawing are optional enhancements that do not change the domain, use cases, CRUD, permissions, or JSON persistence.

**Independent Test**: With a connection, OpenStreetMap appears with its attribution and can be selected or switched to "Mapa local". After three consecutive tile load errors (e.g., simulating a network loss), the system switches to "Mapa local" and shows the notice "No fue posible cargar OpenStreetMap. Se activó el mapa local.", preserving center, zoom, entities, results, selection, and any geometry being drawn. A consultation user never enters draw mode.

**Acceptance Scenarios**:

1. **Given** a connection to the internet, **When** the application loads, **Then** OpenStreetMap is the active base map with its attribution visible.
2. **Given** OpenStreetMap active, **When** the user selects "Mapa local" in the selector, **Then** the local vector map is shown without rebuilding the map, resetting the zoom, or moving the center, and the entity layers remain visible.
3. **Given** the local map active and no connection, **When** the user queries, selects, registers, or edits entities, **Then** all operations work normally.
4. **Given** one or two isolated tile load errors on OpenStreetMap, **When** a later tile loads correctly, **Then** no fallback is triggered.
5. **Given** three consecutive tile load errors on OpenStreetMap, **When** they occur during the current load attempt, **Then** the system activates "Mapa local" exclusively, shows the Spanish notice, and preserves center, zoom, entities, results, selection, and in-progress geometry.
6. **Given** the fallback activated, **When** the user does nothing, **Then** the system does not return to OpenStreetMap automatically during the session.
7. **Given** the fallback activated, **When** the user selects OpenStreetMap manually, **Then** the error counter resets and a new load attempt starts under the same fallback policy.
8. **Given** the cursor over the map, **When** it moves, **Then** the floating control shows latitude and longitude with six decimals (e.g., "Latitud: 4.609710 | Longitud: -74.081750").
9. **Given** the cursor leaves the map, **When** the mouse exits the map area, **Then** the control shows the neutral state "Latitud: -- | Longitud: --".
10. **Given** a consultation user, **When** they click on the map while the register view is open, **Then** no vertices are added.
11. **Given** an administrator in the register view defining a Point, **When** they click the map, **Then** the point is defined at that coordinate and a second click replaces it before saving.
12. **Given** an administrator defining a LineString, **When** they click several times and press "Terminar línea/polígono", **Then** the line is committed with the captured order; with fewer than two coordinates, finishing is prevented with a clear Spanish message.
13. **Given** an administrator defining a Polygon, **When** they click at least three distinct points and press "Terminar línea/polígono", **Then** the ring is closed automatically exactly once; with fewer than three distinct points, finishing is prevented with a clear Spanish message.
14. **Given** geometry being drawn, **When** the user switches the base map, **Then** the vertices, preview, geometry type, form, and edit mode are preserved.
15. **Given** a rejected save, **When** the backend returns a validation error, **Then** the form data and geometry are preserved and the Spanish error is shown; when the save succeeds, the temporary drawing state is cleared.
16. **Given** a change of geometry type, **When** vertices already exist, **Then** the user is asked for confirmation before they are lost.
17. **Given** entity geometry on the map, **When** the base map changes, **Then** the entity layers remain visible.
18. **Given** click-based drawing, **When** the form is canceled, **Then** the preview and temporary vertices are cleared, draw mode is deactivated, and the map returns to normal behavior.
19. **Given** the cursor-coordinate control, **When** the user clicks the map, **Then** no vertex is added unless draw mode is active.
20. **Given** OpenStreetMap unavailable, **When** the user continues working, **Then** query, visualization, selection, registration, editing, and CRUD all keep working on the local map.

### Edge Cases

- What happens when the user tries to register an entity with missing mandatory descriptive data? The operation is rejected with a clear validation message in Spanish.- What happens when spatial data is malformed, uses an unsupported geometry type, or has coordinates out of valid geographic range? The data is rejected before persistence and the dataset is left uncorrupted.
- What happens when the user queries with a location criterion that matches no entity? The system returns an empty result set and informs the user; it is not an error.
- What happens when the user tries to modify or delete an entity that no longer exists? The operation is rejected, stored data is unchanged, and the user is told the entity was not found.
- What happens when two entities share the same name but differ in location or type? Both coexist because each has a unique identifier independent from its name.
- What happens when a polygon is not closed or has too few coordinates? The system rejects the geometry as invalid.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST allow authorized users to register geographic entities of the supported natures (point of interest, road, neighborhood, institution, commercial establishment, and zone).
- **FR-002**: Each entity MUST include descriptive information and a spatial representation.
- **FR-003**: The system MUST support only Point, LineString, and Polygon spatial representations.
- **FR-004**: The system MUST validate coordinates and geometries before accepting them, using the WGS84 spatial reference system: latitude between -90 and 90, longitude between -180 and 180; a Point MUST have one valid coordinate; a LineString MUST have at least two valid coordinates; a Polygon MUST have enough coordinates to form an area, MUST be closed, and MUST have its first and last coordinate represent the same location.
- **FR-005**: The system MUST reject data with unsupported geometry types or uninterpretable geometry before persistence, avoiding corruption of the local dataset.
- **FR-006**: Each entity MUST have a unique identifier independent from its name; names, categories, and types may repeat as long as identifiers differ.
- **FR-007**: Authorized users MUST be able to query existing entities by category.
- **FR-008**: Authorized users MUST be able to query existing entities by simple matching on descriptive information: the entity name, description, and descriptive attributes.
- **FR-009**: Authorized users MUST be able to query existing entities by location, providing a latitude, a longitude, and a search radius in kilometers; the system MUST return the entities within that radius, using a simple, documented criterion for line and polygon entities.
- **FR-010**: Authorized users MUST be able to update the information of existing entities.
- **FR-011**: Authorized users MUST be able to delete existing entities.
- **FR-012**: The system MUST preserve all created, updated, and deleted data across application sessions using local files.
- **FR-013**: The system MUST classify entities into predefined categories and allow categories to be used as a query criterion.
- **FR-014**: Entity types and categories are predefined; users MUST NOT be able to create, modify, or delete them in the first version.
- **FR-015**: The system MUST display registered entities and query results on a map: points as markers, LineStrings as lines, and polygons as areas.
- **FR-016**: The system MUST render a base map usable offline: a local vector base map bundled with the application (simplified administrative boundaries as geographic context) MUST work without internet access and without external tile services, MUST remain available as an alternative base map, and MUST act as the automatic fallback when the online base map cannot load (FR-040). OpenStreetMap MAY be offered as an additional online base map (FR-037), but its unavailability MUST NOT prevent any other functionality.
- **FR-017**: Selecting an entity on the map MUST allow the user to identify it and view its relevant descriptive information.
- **FR-018**: Query results MUST be presented both as a readable list and on the map.
- **FR-019**: A query with no matches MUST clearly inform the user that no matching entities were found, and MUST NOT be treated as an error.
- **FR-020**: The system MUST support at least two user types: consultation user and administrator.
- **FR-021**: The system MUST provide a simple local authentication mechanism with preloaded users (at least one consultation user and one administrator) stored locally with their role and authentication information.
- **FR-022**: Consultation users MUST be able to query, search, and view entities on the map, but MUST NOT create, update, or delete entities.
- **FR-023**: Administrators MUST be able to do everything consultation users can, plus register, update, and delete entities.
- **FR-024**: The system MUST deny write operations to users without the corresponding permission, including attempts made directly rather than through the visible interface.
- **FR-025**: The system MUST reject creation or update operations when mandatory data is missing or invalid: a unique identifier assigned or generated by the system, sufficient descriptive information, a valid category, and a valid spatial representation.
- **FR-026**: Validation errors MUST provide a clear user-facing explanation in Spanish.
- **FR-027**: The application MAY initialize writable local data from bundled seed files on first execution, without modifying files embedded inside the packaged application.
- **FR-028**: The application MAY include a reasonable preloaded academic dataset that demonstrates the geometry types, categories, queries, map visualization, and CRUD operations.
- **FR-029**: All user-facing interface text and messages MUST be presented in Spanish.
- **FR-030**: The system MUST present a login screen as the entry point, where users authenticate with their preloaded credentials before accessing the main interface.
- **FR-031**: The main interface MUST organize the work area with a sidebar that shows the application identity, the current user and their role, a logout control, and navigation tabs between the search (query) view and the entity management (registration) view; the registration tab MUST only be available to administrators.
- **FR-032**: The search view MUST present the query filters and the results as a list of cards; each card MUST show an icon according to the entity nature, the entity name, and its nature and category displayed with Spanish labels (e.g., "Punto de interés · Turismo"), without exposing internal identifiers to the user.
- **FR-033**: A query with no matches MUST present a dedicated empty-state message in the results area ("No se encontraron entidades"), clearly distinct from an error (FR-019).
- **FR-034**: Selecting an entity in the results list MUST highlight it in the list and on the map; hovering an entity on the map MUST show its name as a tooltip (a top tooltip for markers and a sticky tooltip for lines and areas).
- **FR-035**: The map MUST include a legend indicating how points, lines, and areas are represented, and a floating counter showing how many entities are currently visible.
- **FR-036**: When several query criteria are provided together, the system MUST combine them with AND semantics: each provided criterion restricts the result set further, so the result MUST satisfy all provided criteria; the response MUST report the full set of applied criteria.
- **FR-037**: The system MUST offer OpenStreetMap as an optional online base map using the standard tile layer `https://tile.openstreetmap.org/{z}/{x}/{y}.png` with a maximum zoom of up to 19 and the corresponding OpenStreetMap attribution, rendered by the locally bundled Leaflet; no other external service, tile server, or API MAY be used. OpenStreetMap MUST be the initial base layer when its tiles can load.
- **FR-038**: The system MUST keep the bundled local vector base map as an alternative base map and as the automatic fallback when OpenStreetMap cannot load; the local GeoJSON MUST NOT be removed or unnecessarily modified. OpenStreetMap is an optional online improvement; the local map is the only guaranteed offline geographic base map. The unavailability of OpenStreetMap MUST NOT prevent querying entities, visualizing results, selecting entities, registering or editing geometries, or executing role-permitted CRUD.
- **FR-039**: The system MUST provide a base map selector (Leaflet `L.control.layers`) with the two mutually exclusive options "OpenStreetMap" and "Mapa local" (Spanish). Only one base layer MUST be active at a time. Changing the base map MUST NOT rebuild the map, reset the zoom, move the center, clear entities, results, or selection, close popups unnecessarily, or lose geometry being drawn or edited; entity layers MUST remain visible; the automatic fallback selection MUST be reflected in the selector.
- **FR-040**: The system MUST detect OpenStreetMap load failures through the `tileerror` event, MUST NOT trigger fallback on an isolated single tile failure, MUST keep a counter of consecutive tile load errors, MUST activate "Mapa local" when 3 consecutive `tileerror` events occur during the current load attempt, and MUST reset the error counter when a tile loads correctly (`tileload`). On fallback the system MUST deactivate OpenStreetMap, activate exclusively "Mapa local", preserve center, zoom, entities, results, selection, and any geometry being drawn or edited, and show a non-blocking notice in Spanish: "No fue posible cargar OpenStreetMap. Se activó el mapa local." The system MUST NOT return to OpenStreetMap automatically after the fallback during the same session; the user MAY select OpenStreetMap manually again, which resets the error counter and starts a new load attempt under the same policy. A single isolated error after a successful load MUST NOT trigger the fallback.
- **FR-041**: The system MUST display a floating control with the cursor position in real time using the map `mousemove` event, showing latitude and longitude with six decimals in Spanish (e.g., "Latitud: 4.609710 | Longitud: -74.081750"); when the cursor leaves the map it MUST show a neutral state (e.g., "Latitud: -- | Longitud: --"). The control MUST be readable on both base maps, MUST NOT block clicks, MUST NOT interfere with the selector, legend, counter, popups, tooltips, or zoom controls, MUST be available for both roles, and MUST NOT activate draw mode.
- **FR-042**: The system MUST allow an administrator to define Point, LineString, and Polygon geometry by clicking the map: a Point is defined by the first click and replaced by subsequent clicks until saved; a LineString captures vertices and the segments that connect them in capture order and requires at least two coordinates; a Polygon requires at least three distinct points and the application MUST close the ring automatically by repeating the first coordinate exactly once; the button "Terminar línea/polígono" MUST finish lines and polygons, refusing to finish when the minimum point count is not met with a clear Spanish message; "Borrar geometría" MUST clear only the geometry and preview currently being edited; all GeoJSON positions MUST use `[longitude, latitude]` order.
- **FR-043**: The system MUST activate draw mode only when all of the following hold simultaneously: the authenticated user is an administrator, the register or edit view is open, and the form is in geometry-definition mode. In any other case, clicks MUST keep the normal map behavior (entity selection, popups, tooltips) and MUST NOT add vertices accidentally. A consultation user MUST NOT be able to activate draw mode. Hiding interface controls does not replace server-side authorization (FR-024).
- **FR-044**: The system MUST preserve vertices, preview, geometry type, form, and edit mode when switching between OpenStreetMap and the local map. Canceling the form MUST clear the preview and temporary vertices, deactivate draw mode, and restore normal map behavior. A successful save MUST clear the temporary drawing state; a rejected save MUST preserve the form data and geometry, show the error in Spanish, and allow correction and retry. Editing an existing entity MUST load its geometry into the form with an editable preview. Changing the geometry type MUST request confirmation when vertices already exist and the change would lose them.
- **FR-045**: The following remain explicitly out of scope: geocoding, address search through external services, routing, navigation, bulk tile download, and offline storage of OpenStreetMap tiles. The system MUST NOT declare that OpenStreetMap tiles work offline; the local vector base map remains the only guaranteed offline geographic base map.

### Key Entities *(include if feature involves data)*

- **Geographic Entity**: A registered object with a unique identifier, descriptive information, a spatial representation, and a category. Supported natures include point of interest, road, neighborhood, institution, commercial establishment, and zone. Some entity types may have attributes and behaviors particular to their nature.
- **Spatial Representation**: The geometry associated with an entity, expressed as a Point, LineString, or Polygon with defined validity rules.
- **Category**: A predefined classification used to group entities and as a query criterion.
- **User**: A person interacting with the system, of type consultation or administrator, stored locally with role and authentication information.
- **Role / Permission**: The set of operations a user type may perform; consultation is read-only, administrator adds create, update, and delete.
- **Query**: A request filtering entities by location (coordinate and radius), category, or descriptive attributes, producing a result set.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: An authorized user can register an entity with valid data in under 2 minutes.
- **SC-002**: 100% of registered, updated, and deleted entities are recovered after an application restart.
- **SC-003**: All entities matching a query criterion are returned and no non-matching entity appears in the results.
- **SC-004**: 100% of write attempts by users without the corresponding permission are denied, including direct access attempts.
- **SC-005**: 100% of registered entities can be located and identified on the map without internet access.
- **SC-006**: 100% of invalid spatial data attempts are rejected before being stored.
- **SC-007**: 100% of update and delete attempts for nonexistent entities are rejected and leave stored data unchanged.
- **SC-008**: 100% of user-facing messages and interface elements are presented in Spanish.

## Assumptions

- The application targets an academic course; data volumes are small (tens to low hundreds of entities) and usage is single-classroom.
- Identity management is intentionally simple: preloaded users per type, no user registration, and no external identity provider.
- Query by location is a proximity search with a coordinate and a radius; the matching criterion for line and polygon entities is simple, documented in the technical plan, and appropriate to the academic scope.
- Spatial queries and validation are limited to basic academic criteria; advanced spatial analysis (joins, overlays, buffers, intersections, network analysis, paths, CRS transformations, geostatistics, raster) is out of scope.
- Entity types and categories are predefined; dynamic management is out of scope.
- Descriptive attribute queries use simple matching.
- The user-facing interface is presented in Spanish; code identifiers, specifications, and documentation remain in English.
- The base map is a simplified local vector map bundled with the application; it provides geographic context only and needs no internet. OpenStreetMap is offered as an optional online base map (enhancement); the local map is the only guaranteed offline base map.
- No user registration, external authentication, or cloud services are required.
- The design demonstrates Object-Oriented Programming meaningfully (encapsulation, abstraction, inheritance and polymorphism where justified, relationships, reuse) without artificial hierarchies; composition is preferred when more natural.

## Clarifications

### Session 2026-08-13

- Q: Map base layer source? → A: Local vector base map (simplified GeoJSON of Colombia with administrative boundaries) bundled with the app, rendered with Leaflet, works offline, no external tiles or APIs.
- Q: Query by location semantics? → A: Proximity search with latitude, longitude, and radius; simple documented criterion for lines/polygons; advanced spatial analysis out of scope.
- Q: User interface language? → A: Spanish for all user-facing text and messages; code identifiers and docs remain in English.
- Q: Users and authentication? → A: Local preloaded users (one consultation, one administrator) with a simple local authentication mechanism; no registration or external identity provider.
- Q: Permissions? → A: Consultation is read-only; administrator adds create, update, delete; unauthorized writes denied even on direct access.
- Q: Geographic geometry types? → A: Only Point, LineString, and Polygon; no Multi*/GeometryCollection/raster/3D.
- Q: Geographic validation rules? → A: Basic rules only: coordinate ranges, point count, line count, polygon closure; no advanced topology.
- Q: Entity identity? → A: Unique identifier independent from name; names may repeat.
- Q: Entity types and categories? → A: Predefined and fixed for the first version; no dynamic management.
- Q: Descriptive attribute queries? → A: Simple matching; no full-text, fuzzy, or external search.
- Q: Persistence behavior? → A: Local JSON files; seed files may initialize writable data on first run; never modify bundled files at runtime.
- Q: Initial data? → A: Reasonable preloaded academic dataset demonstrating geometry types, categories, queries, map, and CRUD.
- Q: Map visualization behavior? → A: Points as markers, lines as lines, polygons as areas; selecting an entity shows its descriptive information.
- Q: Query result behavior? → A: Results as a list and on the map; empty results clearly informed and not treated as an error. Multiple criteria provided together are combined with AND semantics (each criterion restricts the result further); the response reports the applied criteria (FR-036).
- Q: Missing or invalid data? → A: Creation/update rejected when mandatory data is missing or invalid; clear Spanish validation messages.
- Q: Updating and deleting nonexistent entities? → A: Rejected, stored data unchanged, clear not-found report.
- Q: Unsupported geometry data? → A: Rejected before persistence, dataset left uncorrupted, clear validation error.
- Q: Scope of spatial functionality? → A: No advanced GIS analysis in the first version.
- Q: Expected application scale? → A: Tens to a few hundred entities; no large-scale optimization.
- Q: Object-Oriented Programming scope? → A: Meaningful encapsulation, abstraction, justified inheritance/polymorphism, relationships, reuse; no artificial hierarchies; composition preferred.

### Session 2026-08-13 (UI polish, after implementation)

- Q: Login experience? → A: Dedicated login screen with a brand panel, styled form, password visibility toggle, and loading state; only after successful authentication is the main interface shown.
- Q: Main interface layout? → A: Sidebar-based work area (identity, user/role, logout, navigation tabs between search and registration) plus the map; registration tab only for administrators.
- Q: How are results presented? → A: As cards with a nature icon, the entity name, and Spanish labels for nature and category; a counter shows the number of results.
- Q: Empty query results? → A: Dedicated empty-state message, not an error (FR-019).
- Q: Map interaction? → A: Tooltips with the name on hover, popup with details on click, legend and floating counter of visible entities; selecting a result highlights it in the list and on the map.
- Q: Nature/category naming in the UI? → A: Spanish display labels; internal identifiers remain unchanged.

### Session 2026-08-13 (Convergence review, after implementation)

- Q: How are multiple query criteria combined? → A: With AND semantics: each provided criterion (category, attribute, text, proximity) restricts the result set further; the API reports the full set of applied criteria (FR-036).
- Q: Where is write authorization enforced? → A: At two levels: the HTTP interceptor rejects non-admin write requests, and the entity use case itself requires an administrator role (domain `Role.canModifyEntities`), so writes are denied even for direct application-layer callers (FR-024).

### Session 2026-08-13 (Map base layers and click-based drawing, after implementation)

- Q: Is the map still exclusively local? → A: No. OpenStreetMap is offered as an optional online base map (standard tiles, up to zoom 19, with attribution). The bundled local vector map remains mandatory for offline use and as the automatic fallback; the user can choose the base map from a selector.
- Q: What happens if OpenStreetMap is unavailable? → A: An explicit fallback policy detects 3 consecutive `tileerror` events during the current load attempt, activates "Mapa local" exclusively, and shows the Spanish notice "No fue posible cargar OpenStreetMap. Se activó el mapa local."; center, zoom, entities, results, selection, and in-progress geometry are preserved. The user can retry OpenStreetMap manually, which restarts the policy. OSM unavailability never blocks the rest of the system.
- Q: Do OpenStreetMap tiles work offline? → A: No; they require the network. The local vector map is the only guaranteed offline geographic base map (FR-045).
- Q: What does the base-map selector provide? → A: `L.control.layers` with the mutually exclusive options "OpenStreetMap" and "Mapa local"; switching preserves the map view and all application state (FR-039).
- Q: How are cursor coordinates shown? → A: A floating control updates on `mousemove` with six decimals ("Latitud: 4.609710 | Longitud: -74.081750") and shows a neutral state ("Latitud: -- | Longitud: --") when the cursor leaves the map; it never activates drawing (FR-041).
- Q: When is click-based drawing active? → A: Only for administrators while the register/edit view is open and the form is in geometry-definition mode; otherwise clicks keep normal map behavior (selection, popups, tooltips) and never add vertices (FR-043).
- Q: How are points, lines, and polygons defined by clicks? → A: A Point is set by the first click and replaced by later clicks; a LineString captures vertices and segments (min 2); a Polygon captures vertices with the ring auto-closed once (min 3 distinct points); "Terminar línea/polígono" finishes lines and polygons; GeoJSON uses `[longitude, latitude]` (FR-042).
- Q: What is preserved when switching base maps? → A: Vertices, preview, geometry type, form, edit mode, entities, results, and selection; the map is not rebuilt and center/zoom are kept (FR-039, FR-044).
- Q: Scope of the enhancement? → A: Only the base map and drawing interaction. Domain, use cases, CRUD, permissions, and JSON persistence are unchanged. Out of scope: geocoding, address search, routing, navigation, bulk tile download, offline storage of OSM tiles (FR-045).
