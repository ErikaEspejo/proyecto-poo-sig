# UML Class Diagram — Geographic Information System

Actual domain and application design (matches `src/main/java/edu/udistrital/sig/`).

```mermaid
classDiagram
    class Coordinate {
        <<record>>
        +latitude : double
        +longitude : double
        +ofLonLat(double, double) Coordinate
    }

    class Geometry {
        <<abstract>>
        +type() String
        +minDistanceToKm(Coordinate) double
    }

    class Point {
        +coordinate() Coordinate
    }

    class LineString {
        +coordinates() List~Coordinate~
    }

    class Polygon {
        +ring() List~Coordinate~
    }

    class GeoMath {
        <<static utility>>
        +EARTH_RADIUS_KM : double
        +haversineKm(Coordinate, Coordinate) double
        +pointToSegmentDistanceKm(Coordinate, Coordinate, Coordinate) double
        +pointInRing(Coordinate, List~Coordinate~) boolean
    }

    class GeographicEntity {
        -id : String
        -name : String
        -description : String
        -nature : EntityNature
        -category : Category
        -attributes : Map~String, String~
        -geometry : Geometry
        +create(...) GeographicEntity
        +updatedWith(...) GeographicEntity
        +id() String
        +name() String
        +description() String
        +nature() EntityNature
        +category() Category
        +attributes() Map~String, String~
        +geometry() Geometry
    }

    class EntityNature {
        <<enum>>
        POINT_OF_INTEREST
        ROAD
        NEIGHBORHOOD
        INSTITUTION
        COMMERCIAL_ESTABLISHMENT
        ZONE_OF_INTEREST
    }

    class Category {
        <<record>>
        +id : String
        +name : String
    }

    class Role {
        <<enum>>
        CONSULTATION
        ADMINISTRATOR
    }

    class User {
        <<record>>
        +username : String
        +passwordHash : String
        +role : Role
    }

    class EntityRepository {
        <<interface>>
        +findAll() List~GeographicEntity~
        +findById(String) Optional~GeographicEntity~
        +save(GeographicEntity) GeographicEntity
        +delete(String) void
    }

    class CategoryRepository {
        <<interface>>
        +findAll() List~Category~
        +findById(String) Optional~Category~
    }

    class UserRepository {
        <<interface>>
        +findByUsername(String) Optional~User~
    }

    class JsonDataStore {
        -dataDirectory : Path
        +read(String) JsonNode
        +readArray(String) ArrayNode
        +write(String, JsonNode) void
        +exists(String) boolean
        +path(String) Path
    }

    class EntityRepositoryJson {
        -FILE_NAME : String
    }

    class CategoryRepositoryJson {
        -FILE_NAME : String
    }

    class UserRepositoryJson {
        -FILE_NAME : String
    }

    class EntityJsonCodec {
        <<static utility>>
        +fromJson(JsonNode, Category) GeographicEntity
        +toJson(GeographicEntity) ObjectNode
    }

    class GeometryJsonCodec {
        <<static utility>>
        +fromJson(JsonNode) Geometry
        +toJson(Geometry) ObjectNode
    }

    class EntityService {
        +findAll() List~GeographicEntity~
        +findById(String) Optional~GeographicEntity~
        +create(GeographicEntity, Role) GeographicEntity
        +update(String, GeographicEntity, Role) GeographicEntity
        +delete(String, Role) void
    }

    class QueryService {
        +query(category, attribute, text, lat, lon, radiusKm) QueryResult
    }

    class QueryService..QueryResult {
        <<record>>
        +entities : List~GeographicEntity~
        +matchedBy : String
    }

    class CategoryService {
        +findAll() List~Category~
    }

    class AuthService {
        +login(String, String) String
        +resolve(String) User
    }

    class TokenManager {
        -tokens : Map~String, User~
        +issue(User) String
        +resolve(String) Optional~User~
    }

    class PasswordHasher {
        <<interface>>
        +hash(String) String
    }

    class Sha256PasswordHasher {
        +hash(String) String
    }

    class AuthController {
        +login(LoginRequest) ResponseEntity~ObjectNode~
    }

    class EntityController {
        +list() ObjectNode
        +get(String) ObjectNode
        +create(JsonNode, HttpServletRequest) ResponseEntity~ObjectNode~
        +update(String, JsonNode, HttpServletRequest) ObjectNode
        +delete(String, HttpServletRequest) ResponseEntity~Void~
        +query(...) ObjectNode
    }

    class CategoryController {
        +list() ObjectNode
    }

    class BaseMapController {
        +basemap() ResponseEntity~JsonNode~
    }

    class AuthInterceptor {
        +preHandle(...) boolean
    }

    class GlobalExceptionHandler {
        <<@RestControllerAdvice>>
    }

    class EntityRequestMapper {
        <<static utility>>
        +toDomain(JsonNode, String, CategoryRepository) GeographicEntity
    }

    Geometry <|-- Point : is-a
    Geometry <|-- LineString : is-a
    Geometry <|-- Polygon : is-a

    Point ..> GeoMath : uses
    LineString ..> GeoMath : uses
    Polygon ..> GeoMath : uses

    GeographicEntity *-- Geometry : composition (required)
    GeographicEntity --> Category : reference (aggregation)
    GeographicEntity --> EntityNature : value
    User --> Role : value

    EntityRepository <|.. EntityRepositoryJson : implements
    CategoryRepository <|.. CategoryRepositoryJson : implements
    UserRepository <|.. UserRepositoryJson : implements

    EntityRepositoryJson ..> EntityJsonCodec : uses
    EntityRepositoryJson ..> GeometryJsonCodec : uses
    CategoryRepositoryJson --> JsonDataStore : uses
    UserRepositoryJson --> JsonDataStore : uses
    EntityRepositoryJson --> JsonDataStore : uses
    EntityRepositoryJson --> CategoryRepository : resolves category

    EntityService --> EntityRepository : depends (interface)
    QueryService --> EntityRepository : depends (interface)
    CategoryService --> CategoryRepository : depends (interface)
    AuthService --> UserRepository : depends (interface)
    AuthService --> TokenManager : depends
    AuthService ..> PasswordHasher : depends (interface)
    PasswordHasher <|.. Sha256PasswordHasher : implements
    Sha256PasswordHasher ..> MessageDigest : uses

    EntityController --> EntityService
    EntityController --> QueryService
    EntityController --> CategoryRepository
    AuthController --> AuthService
    CategoryController --> CategoryService
    BaseMapController ..> ResourceLoader : reads bundled base map
    EntityController ..> EntityRequestMapper : parses requests
    EntityController ..> EntityJsonCodec : serializes responses

    AuthInterceptor --> AuthService : authenticates + authorizes
```

## Notes

- **Domain independence**: `domain/` classes (Coordinate, Geometry and subtypes, GeographicEntity, Category, User, enums, repository interfaces, exceptions) have no dependency on Spring, Jackson, HTTP, JSON or Leaflet (constitution principle II).
- **Serialization boundary**: Jackson is confined to `infrastructure/` (`JsonDataStore`, `EntityJsonCodec`, `GeometryJsonCodec`, controllers). Geometry is exchanged as GeoJSON at the HTTP and persistence edges.
- **Persistence boundary**: JSON files are only accessed through `JsonDataStore` behind repository interfaces.
- **Auth**: `AuthInterceptor` enforces token authentication and administrator-only writes server-side (FR-024); the frontend additionally hides write controls for consultation users.
