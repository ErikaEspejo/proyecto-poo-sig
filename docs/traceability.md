# OOP Traceability Report — Geographic Information System

Final report mapping OOP concepts and the constitution v2.0.0 principles to concrete implementation points (`src/main/java/edu/udistrital/sig/`).

## Encapsulation

- `GeographicEntity` (domain/model/GeographicEntity.java) keeps its state private (`id`, `name`, `nature`, `category`, `attributes`, `geometry`) and exposes only read accessors. Mutation is possible only through the static factory `create` and `updatedWith`, both of which run `validate()`; `attributes` is defensively copied into an immutable `Map.copyOf` (verified by `GeographicEntityTest.attributesAreImmutableCopies`).
- `Point`, `LineString`, `Polygon` enforce their invariants in the constructor (coordinate ranges, minimum point counts, ring closure, distinct points) so no invalid geometry can exist (FR-004, FR-005).
- `JsonDataStore` encapsulates the JSON file access and the atomic-write strategy (temp file + `Files.move`); repositories only see `JsonNode`/domain objects and never touch the filesystem directly.
- `TokenManager` encapsulates the in-memory token store; tokens are opaque strings and cannot be forged from outside.

## Abstraction

- `Geometry` (abstract) defines the contract `type()` and `minDistanceToKm(Coordinate)`; consumers (`QueryService`, `EntityJsonCodec`) program against the abstraction, not concrete types.
- Repository interfaces (`EntityRepository`, `CategoryRepository`, `UserRepository`) abstract persistence so services depend on interfaces (DIP). JSON implementations are interchangeable for testing (see `InMemoryEntityRepository`).

## Inheritance (genuine is-a)

- `Point`, `LineString`, `Polygon` extend `Geometry` — a genuine is-a relationship with genuinely varying behavior (validity rules and distance semantics differ per type). No speculative inheritance is used elsewhere.

## Polymorphism

- `Geometry.minDistanceToKm` is the primary example: the same call returns Haversine distance for a `Point`, minimum segment distance for a `LineString`, and 0 (inside, ray casting) or ring distance for a `Polygon` — without any `instanceof` in `QueryService`.
- Role-based authorization is conditional enforcement, not polymorphism: `AuthInterceptor` centrally gates HTTP write requests per role and HTTP method (FR-024), and the entity use case (`EntityService`) independently requires `Role.canModifyEntities()` so writes are also denied to direct application-layer callers. Both layers check the role; neither dispatches on it polymorphically.
- JSON handling: `EntityJsonCodec`/`GeometryJsonCodec` treat any `Geometry` uniformly through `type()` + GeoJSON mapping.

## Relationships

- **Composition**: `GeographicEntity` owns a `Geometry` (required field, created with the entity, no shared lifecycle).
- **Aggregation/reference**: `GeographicEntity` references a `Category` (predefined catalog; category outlives the entity).
- **Dependency inversion**: services depend on repository interfaces; controllers depend on services; infrastructure depends on the domain — never the reverse.

## Reuse (DRY applied to meaningful duplication)

- `GeoMath` centralizes geodesic math (Haversine, point-to-segment, ray casting) reused by all geometry types.
- `EntityJsonCodec`/`GeometryJsonCodec` are reused by both persistence (`EntityRepositoryJson`) and presentation (`EntityController`) so there is a single source of truth for the entity JSON shape.
- `AuthService.resolve` is shared by the login response and the interceptor.

## SOLID (pragmatic)

- **SRP**: each class has one responsibility (geometry validity, geodesic math, persistence atomicity, query orchestration, authentication, HTTP mapping, error mapping).
- **OCP** (partial, by design): adding a new geometry type requires extending `Geometry` and adding one branch in `GeometryJsonCodec` (a new `instanceof`/switch arm); existing consumers (`QueryService`, `EntityJsonCodec`) do not change. For the fixed three-type scope this is a simple and acceptable point of change.
- **LSP**: all geometry subtypes honor the `Geometry` contract (`minDistanceToKm` semantics documented and tested).
- **ISP**: repository interfaces are small and tailored (three separate interfaces, not one god repository).
- **DIP**: `EntityService`/`QueryService`/`AuthService` depend on interfaces; `AuthService` depends on the `PasswordHasher` port defined in `application` with an infrastructure SHA-256 implementation (`Sha256PasswordHasher`); `JsonDataStore` is injected into repositories.

## Constitution principle mapping

| Principle | Evidence |
|-----------|----------|
| II Rich, infrastructure-independent domain | `domain/` has no Spring/Jackson/HTTP/JSON/Leaflet imports |
| III Encapsulation and invariants | constructors + `validate()`; immutable records (`Coordinate`, `Category`, `User`) |
| IV Domain concepts over primitives | `Coordinate`, `Category`, enums instead of raw primitives |
| V Explicit and meaningful errors | typed exceptions (`InvalidGeometryException`, `InvalidEntityException`, `EntityNotFoundException`, `InvalidCredentialsException`, `AuthenticationRequiredException`, `UnauthorizedOperationException`) mapped to Spanish user messages; technical details logged |
| VII Behavior-oriented testing | 81 behavior tests: domain rules, invalid/boundary cases, permission checks, AND-combined query criteria; no coverage metric chasing |
| VIII Clear responsibility boundaries | controllers (HTTP) / repositories (JSON persistence) / services (orchestration) / domain (rules) |
| IX Academic traceability | this report + PlantUML models `docs/uml/classes.puml` and `docs/uml/use-cases.puml` + `docs/use-cases.md` |
| Persistence integrity | atomic writes; `JsonDataStoreIntegrityTest` proves a failed write leaves prior data intact |

## Dependency policy review

- Dependencies: `spring-boot-starter-web` (Spring MVC + embedded Tomcat + Jackson) and `spring-boot-starter-test` (JUnit 5). No databases, JPA, Hibernate, or Spring Data (spec constraint). Mockito is on the test classpath but no test mocks unnecessary collaborators — tests use real objects or small in-memory fakes (`InMemoryEntityRepository`, `InMemoryUserRepository`).
- UI language: Spanish; code identifiers, specs, docs, and commits: English.
