# UML Use Case Diagram — Geographic Information System

Actors and use cases of the implemented system (matches `docs/use-cases.md`, UC-01..UC-08, and the user stories in `spec.md`).

The complete use case diagram is maintained as a PlantUML model in [`use-cases.puml`](use-cases.puml).

## Actors

- **Usuario** — generic user; generalization of the two concrete actors.
- **Usuario de consulta (Consulta)** — read-only actor (browse, query, view details, list categories, load base map, authenticate).
- **Administrador** — full actor (everything a consultation user can do, plus register, update and delete entities).

## Use cases

| # | Use case | Actors |
|---|----------|--------|
| UC-01 | Autenticarse | Usuario |
| UC-02 | Explorar entidades en el mapa / ver detalle | Consulta, Administrador |
| UC-03 | Registrar una entidad | Administrador |
| UC-04 | Actualizar una entidad | Administrador |
| UC-05 | Eliminar una entidad | Administrador |
| UC-06 | Consultar entidades | Consulta, Administrador |
| UC-07 | Listar categorías | Consulta, Administrador |
| UC-08 | Cargar mapa base local | Consulta, Administrador |

## Relationships

- **include**: UC-01 (authentication) is included by all flows that require an active session; UC-03 (query) is included by UC-02 (results shown on the map); UC-06 (base map) is included by UC-02 (requires the local base map); UC-04 (view detail) is included by UC-03 (selection over results).
- **extend**: UC-07 (register), UC-08 (update) and UC-09 (delete) extend UC-03 (query) because they first select an existing entity / validate with a prior query.

## Rendering

Render with the PlantUML JAR:

```sh
java -jar plantuml.jar -o <output-dir> docs/uml/use-cases.puml
```

or use any PlantUML renderer (VS Code extension, PlantText, Kroki, …).

## Source

See [`use-cases.puml`](use-cases.puml) for the diagram source (labels in Spanish, matching the academic deliverables).
