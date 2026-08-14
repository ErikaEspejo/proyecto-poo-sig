# Sistema de Información Geográfica (SIG) Académico

Sistema web (Java 21, Spring Boot 3) para consultar, visualizar y administrar entidades
geográficas (puntos, líneas y polígonos) sobre un mapa con dos capas base seleccionables:
OpenStreetMap en línea (opcional, con fallback automático) y mapa vectorial local de Colombia
(offline). Persistencia en archivos JSON locales, sin bases de datos ni servicios externos.

## Entregables

| Entregable | Ubicación |
|------------|-----------|
| Documento explicando el diseño del sistema | `docs/design.md` |
| Especificaciones | `specs/001-geographic-information-system/` (`spec.md`, `contracts/api.md`, `plan.md`, `tasks.md`, `quickstart.md`) |
| Código fuente del proyecto | `src/` |
| Manual básico de instalación y uso | `docs/manual-de-instalacion-y-uso.md` |

Los entregables académicos (documento de diseño, manual, diagramas UML, casos de uso y reporte de
trazabilidad) se encuentran en la carpeta `docs/`.

## Otros entregables y alcance del sistema

- Registrar y administrar diferentes tipos de entidades geográficas (puntos, líneas y polígonos).
- Asociar coordenadas geográficas (WGS84) a cada entidad, capturadas por clic sobre el mapa.
- Consultar entidades por ubicación (proximidad), categoría, texto o atributos, combinables con
  semántica AND.
- Visualizar las entidades sobre un mapa con dos capas base (OpenStreetMap en línea y mapa local
  offline).
- Implementar operaciones de creación, consulta, actualización y eliminación (CRUD).
- Manejar diferentes tipos de usuarios y permisos (Administrador y Consulta).
- Aplicar los principios de programación orientada a objetos en el diseño del sistema (ver
  `docs/traceability.md`).
- Elaborar el modelo UML del sistema, incluyendo diagrama de casos de uso y diagrama de clases
  (`docs/uml/`).
- Almacenar y gestionar la información del sistema de forma persistente. Nota: según la
  especificación aprobada, la persistencia se implementa con archivos JSON locales
  (`data/`), no con una base de datos relacional.

## Inicio rápido

```sh
java -version            # requiere JDK 21
.\mvnw.cmd spring-boot:run    # Windows
./mvnw spring-boot:run        # Linux/macOS
```

Abrir http://localhost:8080. Cuentas precargadas: `admin`/`admin123` (Administrador) y
`consulta`/`consulta123` (Consulta).

## Pruebas

```sh
.\mvnw.cmd verify
```

Repositorio: https://github.com/ErikaEspejo/proyecto-poo-sig
