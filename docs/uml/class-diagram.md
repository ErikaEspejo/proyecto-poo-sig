# UML Class Diagram — Geographic Information System

Actual domain, application and infrastructure design (matches `src/main/java/edu/udistrital/sig/`).

The complete class diagram is maintained as a PlantUML model in [`classes.puml`](classes.puml).
It reflects the real implementation, organized by layer:

- **Dominio (domain)** — `domain.model`, `domain.exception`, `domain.repository`
- **Aplicación (application)** — `application.service`
- **Infraestructura (infrastructure)** — `infrastructure.persistence`, `infrastructure.codec`, `infrastructure.security`, `infrastructure.web`

## Rendering

Render with the PlantUML JAR:

```sh
java -jar plantuml.jar -o <output-dir> docs/uml/classes.puml
```

or use any PlantUML renderer (VS Code extension, PlantText, Kroki, …).

## Source

See [`classes.puml`](classes.puml) for the diagram source (labels in Spanish, matching the academic deliverables).

## Notes

- **Domain independence**: `domain/` classes (Coordinate, Geometry and subtypes, GeographicEntity, Category, User, enums, repository interfaces, exceptions) have no dependency on Spring, Jackson, HTTP, JSON or Leaflet (constitution principle II).
- **Serialization boundary**: Jackson is confined to `infrastructure/` (`JsonDataStore`, `EntityJsonCodec`, `GeometryJsonCodec`, controllers). Geometry is exchanged as GeoJSON at the HTTP and persistence edges.
- **Persistence boundary**: JSON files are only accessed through `JsonDataStore` behind repository interfaces.
- **Auth**: `AuthInterceptor` enforces token authentication and administrator-only writes server-side (FR-024); the frontend additionally hides write controls for consultation users.
