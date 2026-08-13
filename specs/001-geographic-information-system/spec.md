# Feature Specification: Geographic Information System

**Feature Branch**: `001-geographic-information-system`

**Created**: 2026-08-13

**Status**: Implemented

**Input**: User description: "Build an academic Geographic Information System (GIS) that allows registering, storing, querying, updating, deleting and visualizing information associated with different geographic locations, demonstrating a meaningful application of Object-Oriented Programming."

**Provenance**: FR-001 through FR-029 were defined during the initial specification phase, before implementation. FR-030 through FR-036 were incorporated after implementation: FR-030 to FR-035 document the UI polish session (2026-08-13), and FR-036 documents the query-combination semantics decided during the convergence review. The convergence review (2026-08-13) revalidated the implemented behavior against this specification.

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

### Edge Cases

- What happens when the user tries to register an entity with missing mandatory descriptive data? The operation is rejected with a clear validation message in Spanish.
- What happens when spatial data is malformed, uses an unsupported geometry type, or has coordinates out of valid geographic range? The data is rejected before persistence and the dataset is left uncorrupted.
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
- **FR-016**: The system MUST render the map using a local vector base map bundled with the application, using simplified administrative boundaries as geographic context, working without internet access and without external tile services or APIs.
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
- The base map is a simplified local vector map bundled with the application; it provides geographic context only and needs no internet.
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
