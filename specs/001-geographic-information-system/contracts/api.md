# API Contracts: Geographic Information System

Phase 1 output. The application exposes a JSON REST API consumed by the vanilla JS frontend, plus static resources for the UI and base map. Geometry is exchanged in GeoJSON.

**Base path**: `/api`
**Content type**: `application/json` (UTF-8)
**Auth**: token returned by login, sent as `Authorization: Bearer <token>`

## Endpoints

### POST /api/auth/login

Authenticate a preloaded user (FR-021).

**Request body**:
```json
{ "username": "admin", "password": "admin123" }
```

**Responses**:
- `200 OK`
```json
{ "token": "<token>", "role": "ADMINISTRATOR" }
```
- `401 Unauthorized` when credentials are invalid:
```json
{ "error": "Credenciales inválidas." }
```

### GET /api/entities

List all entities.

**Responses**:
- `200 OK`
```json
{
  "entities": [
    {
      "id": "uuid",
      "name": "Torre Colpatria",
      "description": "Punto de referencia",
      "nature": "POINT_OF_INTEREST",
      "category": "TURISMO",
      "attributes": {},
      "geometry": { "type": "Point", "coordinates": [-74.071, 4.612] }
    }
  ]
}
```

### GET /api/entities/{id}

Get one entity. Returns `404 Not Found` with a Spanish message if it does not exist.

### POST /api/entities

Create an entity (FR-001). Administrator only.

**Request body**: entity object as above, without `id` (system assigns it).

**Responses**:
- `201 Created` with the stored entity.
- `400 Bad Request` with a Spanish validation message for missing/invalid data (FR-025, FR-026).
- `403 Forbidden` for non-administrators (FR-024).

### PUT /api/entities/{id}

Update an entity (FR-010). Administrator only. Replaces the entity after full validation.

**Responses**:
- `200 OK` with the stored entity.
- `400 Bad Request` Spanish validation message.
- `403 Forbidden` for non-administrators.
- `404 Not Found` if the entity does not exist; stored data unchanged.

### DELETE /api/entities/{id}

Delete an entity (FR-011). Administrator only.

**Responses**:
- `204 No Content`.
- `403 Forbidden` for non-administrators.
- `404 Not Found` if the entity does not exist; stored data unchanged.

### GET /api/entities/query

Query entities (FR-007, FR-008, FR-009, FR-036). All parameters optional; at least one filter must be present. When several filters are provided they are combined with AND semantics (each filter restricts the result set further), and `matchedBy` lists all applied criteria in a fixed order: `CATEGORY`, `ATTRIBUTE`, `TEXT`, `PROXIMITY` (comma-separated).

**Query parameters**:
| Param | Meaning |
|-------|---------|
| `category` | Category id or name |
| `attribute` | Free-text simple match on descriptive attributes |
| `text` | Free-text simple match on name/description |
| `lat`, `lon`, `radiusKm` | Proximity query (all three required together); radius in kilometers |

**Responses**:
- `200 OK`
```json
{
  "entities": [ ... ],
  "matchedBy": "PROXIMITY"
}
```
When combining filters, e.g. category + text:
```json
{
  "entities": [ ... ],
  "matchedBy": "CATEGORY,TEXT"
}
```
- `400 Bad Request` if no filter or an incomplete proximity filter is provided.
- Empty `entities` array + `200 OK` when no matches (not an error; FR-019).

### GET /api/categories

List predefined categories (FR-013).

**Responses**:
- `200 OK`
```json
{ "categories": [ { "id": "TURISMO", "name": "Turismo" } ] }
```

## Static resources

| Path | Purpose |
|------|---------|
| `/` | Frontend entry point (`index.html`) |
| `/css/`, `/js/`, `/leaflet/` | Vanilla JS app and bundled offline Leaflet assets |
| `/data/colombia-boundaries.geojson` | Local vector base map (offline) |

## Error contract

All errors use a consistent envelope:
```json
{ "error": "<mensaje en español>" }
```
Technical details are logged server-side and never exposed to the user (constitution principle V).

## Proximity criterion

A line/polygon entity matches when the minimum distance from the query coordinate to the geometry is within `radiusKm`; a point inside a polygon has distance 0. Distances use Haversine (see `research.md` section 4).
