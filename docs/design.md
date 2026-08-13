# Documento de Diseño del Sistema — SIG Académico

Versión: 1.0.0
Fecha: 2026-08-13
Repositorio: https://github.com/ErikaEspejo/proyecto-poo-sig

Este documento describe el diseño del **Sistema de Información Geográfica (SIG)** académico
implementado en Java 21. El sistema permite consultar, visualizar y administrar entidades
geográficas (puntos, líneas y polígonos) sobre un mapa base local de Colombia, sin depender de
servicios externos.

Documentos relacionados:

- Especificaciones: `specs/001-geographic-information-system/` (`spec.md`, `contracts/api.md`)
- Casos de uso: `docs/use-cases.md`
- UML (PlantUML): `docs/uml/classes.puml` (clases), `docs/uml/use-cases.puml` (casos de uso)
- Reporte de trazabilidad OOP: `docs/traceability.md`
- Manual de instalación y uso: `docs/manual-de-instalacion-y-uso.md`

---

## 1. Objetivos

1. Proveer una aplicación web autónoma para visualizar entidades geográficas sobre un mapa.
2. Permitir consultas por categoría, texto, atributos y proximidad, combinables entre sí.
3. Permitir a los administradores registrar, actualizar y eliminar entidades.
4. Mantener el dominio independiente de la infraestructura técnica (Spring, Jackson, HTTP, Leaflet).
5. Aplicar de forma significativa los principios de la programación orientada a objetos (encapsulamiento,
   abstracción, herencia y polimorfismo) y SOLID.
6. Persistir los datos en archivos JSON locales, sin bases de datos ni servicios externos.

## 2. Alcance

El sistema cubre los requisitos funcionales FR-001 a FR-036 (ver `spec.md`). Está fuera de alcance:

- Mapas de teselas en línea (OpenStreetMap, etc.); se usa un mapa base vectorial local simplificado.
- Bases de datos relacionales o Spring Data.
- Autenticación por terceros; se usa autenticación local con tokens en memoria.
- Servicios externos de geocodificación.

## 3. Arquitectura general

Se usa una **arquitectura en capas** con un enfoque de **Dependencia Invertida**: el dominio no conoce
nada de la infraestructura; la infraestructura depende del dominio.

```
┌──────────────────────────────────────────────────────────────┐
│  Capa web (infrastructure.web)                                │
│  Controladores HTTP · Interceptor de autenticación ·          │
│  Manejo global de errores · Frontend (Leaflet)                │
└───────────────────────────┬──────────────────────────────────┘
                            │ usa
┌───────────────────────────▼──────────────────────────────────┐
│  Capa de aplicación (application.service)                     │
│  Casos de uso: EntityService · QueryService ·                 │
│  CategoryService · AuthService · TokenManager ·               │
│  Puerto PasswordHasher                                        │
└───────────────────────────┬──────────────────────────────────┘
                            │ usa (solo interfaces de dominio)
┌───────────────────────────▼──────────────────────────────────┐
│  Capa de dominio (domain)                                     │
│  model: GeographicEntity, Geometry (Point/LineString/Polygon),│
│         GeoMath, Coordinate, Category, Role, User, enums      │
│  exception: excepciones tipadas                               │
│  repository: interfaces de persistencia                       │
└───────────────────────────┬──────────────────────────────────┘
                            │ implementa
┌───────────────────────────▼──────────────────────────────────┐
│  Infraestructura técnica (infrastructure)                     │
│  persistence: JsonDataStore + repositorios JSON + seed        │
│  codec: EntityJsonCodec, GeometryJsonCodec (GeoJSON)          │
│  security: Sha256PasswordHasher                               │
└──────────────────────────────────────────────────────────────┘
```

**Reglas de dependencia** (constitución, principio II y arquitectura aprobada):

- `domain/` no importa Spring, Jackson, HTTP, JSON ni Leaflet.
- `application/` depende solo de interfaces de `domain/` y del puerto `PasswordHasher`.
- `infrastructure/` depende de `domain/` y `application/`, nunca al revés.
- La persistencia está detrás de las interfaces `EntityRepository`, `CategoryRepository` y
  `UserRepository`.

## 4. Modelo de dominio

### 4.1 Entidad geográfica

`GeographicEntity` es el agregado central. Es una clase inmutable con un solo punto de creación
(fábrica estática `create`) y un solo punto de mutación (`updatedWith`); ambos validan el objeto
completo (FR-002, FR-010). Sus invariantes:

- `id` no vacío; `name` no vacío; `description` no vacía.
- `nature` y `category` obligatorias.
- `attributes` es una copia inmutable (`Map.copyOf`).
- `geometry` obligatoria.

Compone una `Geometry` (relación de composición: la geometría no existe sin la entidad). Referencia
a una `Category` del catálogo predefinido (agregación: la categoría sobrevive a la entidad).

### 4.2 Geometrías

`Geometry` es una clase abstracta con contrato `type()` y `minDistanceToKm(Coordinate)`. Tres
subtipos, cada uno con invariantes propias (FR-004, FR-005):

- `Point`: una coordenada; rango WGS84 (`latitude` ∈ [-90, 90], `longitude` ∈ [-180, 180]).
- `LineString`: lista de 2 o más coordenadas, todas en rango, sin repeticiones consecutivas.
- `Polygon`: anillo cerrado (primera = última) con 4 o más vértices, todos en rango y distintos.

`GeoMath` concentra la matemática geodésica reutilizada por todas las geometrías:

- `haversineKm`: distancia entre dos coordenadas sobre la esfera.
- `pointToSegmentDistanceKm`: distancia de un punto a un segmento (Haversine en pasos pequeños).
- `pointInRing`: inclusión de punto en polígono (ray casting).

El polimorfismo de `minDistanceToKm` implementa la semántica de distancia por tipo:

- `Point`: distancia Haversine al punto consultado.
- `LineString`: distancia mínima del consultado a cualquier segmento.
- `Polygon`: 0 si el punto está dentro del anillo; en caso contrario, distancia mínima al anillo.

### 4.3 Categorías y usuarios

- `Category` (`record`): `id` + `name` (catálogo en español, FR-013).
- `Role` (`enum`): `CONSULTATION` y `ADMINISTRATOR`; `canModifyEntities()` devuelve `true` solo para
  `ADMINISTRATOR`. La autorización se evalúa en el caso de uso, no en el controlador.
- `User` (`record`): `username`, `passwordHash` (SHA-256) y `role` (seed local en `users.json`).

### 4.4 Excepciones tipadas

`InvalidGeometryException`, `InvalidEntityException`, `EntityNotFoundException`,
`InvalidCredentialsException`, `AuthenticationRequiredException`, `UnauthorizedOperationException`
y `PersistenceException` (infraestructura). Se mapean a códigos HTTP y mensajes en español
(FR-017, FR-018, FR-020).

## 5. Capa de aplicación (casos de uso)

Cada servicio orquesta un caso de uso y no contiene reglas de presentación ni de persistencia.

### 5.1 `EntityService`

Operaciones CRUD sobre entidades. `create`, `update` y `delete` reciben el `Role` del usuario que
invoca y lanzan `UnauthorizedOperationException` si `!Role.canModifyEntities()` (FR-024). La
autorización vive aquí para que tampoco se pueda escribir saltándose la capa web.

### 5.2 `QueryService`

Implementa la consulta combinada (`GET /api/entities/query`, FR-007, FR-008, FR-009, FR-019,
FR-036). Los criterios se **combinan con semántica AND**: cada criterio presente restringe aún más
el conjunto de resultados. La cadena `matchedBy` (p. ej. `CATEGORY,TEXT`) indica qué criterios
aportaron resultados, para que la interfaz pueda mostrar "sin resultados" en lugar de error.
Criterios:

- `category`: coincide con `Category.id` o `Category.name`.
- `attribute`: texto libre sobre los valores de `attributes` (ignore case, substring).
- `text`: texto libre sobre `name` y `description` (ignore case, substring).
- `proximity`: latitud + longitud + radio en km; usa `minDistanceToKm` (polimorfismo), de modo que
  la contención en polígono cuenta como distancia 0.

### 5.3 `CategoryService` y `AuthService`

- `CategoryService.findAll()`: lista el catálogo (FR-013, FR-014).
- `AuthService.login` valida credenciales (contrastando con `PasswordHasher`) y emite un token vía
  `TokenManager`; `resolve` recupera el usuario a partir del token (FR-022, FR-023).

### 5.4 `TokenManager`

Almacén en memoria token → `User`. Los tokens son cadenas opacas (UUID) sin información de usuario;
son intransferibles e infalsificables dentro del ciclo de vida del proceso.

## 6. Persistencia

`JsonDataStore` encapsula el acceso a los archivos JSON en `data/`:

- Escritura **atómica**: se escribe a un archivo temporal y se mueve con `Files.move` (fallo seguro).
- `read`/`readArray`/`write` tipados con Jackson `JsonNode`/`ArrayNode`.

`EntityRepositoryJson`, `CategoryRepositoryJson` y `UserRepositoryJson` implementan las interfaces
de dominio. `EntityJsonCodec`/`GeometryJsonCodec` serializan entidades y geometrías en formato
GeoJSON, y son reutilizados por persistencia y presentación (una única fuente de verdad del formato).

`SeedDataInitializer` (implementa `ApplicationRunner`) copia los archivos semilla de
`src/main/resources/data/` al directorio `data/` la primera vez que arranca la aplicación.

## 7. Autenticación y autorización

- `POST /api/auth/login` valida credenciales locales; devuelve `{ token, username, role }`.
- `AuthInterceptor` (registrado en `WebConfig`) verifica el encabezado `Authorization: Bearer <token>`
  en las rutas `/api/entities/**` y `/api/categories/**`; `AuthController` y `BaseMapController`
  quedan públicos.
- El interceptor **condiciona** (no polimórfico) el acceso por método HTTP: solo `ADMINISTRATOR`
  puede ejecutar `POST/PUT/DELETE` sobre entidades; un `CONSULTATION` recibe `403`. El caso de uso
  `EntityService` refuerza la misma regla por si un llamador no web intenta escribir.
- `GlobalExceptionHandler` traduce cada excepción de dominio a un código HTTP y un mensaje en español
  sin exponer detalles técnicos (que quedan en el log).

## 8. Frontend

- `static/index.html` + `static/js/app.js` + `static/css/style.css` (español).
- Leaflet distribuido localmente en `static/leaflet/` (sin CDN; funciona sin internet).
- El mapa base es el GeoJSON simplificado de departamentos de Colombia servido por
  `BaseMapController` desde `data/colombia-boundaries.geojson` (FR-016).
- Las entidades se dibujan según su tipo: `Point` como marcador, `LineString` como línea y `Polygon`
  como área (FR-015).
- El panel de consulta permite combinar categoría, texto, atributo y proximidad; los resultados se
  resaltan en el mapa y se actualiza un contador (FR-019).
- Los controles de escritura se muestran solo al rol `ADMINISTRADOR`; el servidor además los protege.

## 9. API REST (resumen)

| Método | Ruta | Descripción | Autenticación |
|--------|------|-------------|---------------|
| POST | `/api/auth/login` | Iniciar sesión | Pública |
| GET | `/api/map/basemap` | Mapa base local | Pública |
| GET | `/api/categories` | Catálogo de categorías | Token |
| GET | `/api/entities` | Listar entidades | Token |
| GET | `/api/entities/{id}` | Detalle de entidad | Token |
| GET | `/api/entities/query` | Consulta combinada (AND) | Token |
| POST | `/api/entities` | Registrar entidad | Token + Administrador |
| PUT | `/api/entities/{id}` | Actualizar entidad | Token + Administrador |
| DELETE | `/api/entities/{id}` | Eliminar entidad | Token + Administrador |

Contrato completo en `specs/001-geographic-information-system/contracts/api.md`.

## 10. Aplicación de la POO y SOLID

| Concepto | Dónde |
|----------|-------|
| Encapsulamiento | `GeographicEntity`, geometrías, `JsonDataStore`, `TokenManager` |
| Abstracción | `Geometry`, interfaces de repositorio, puerto `PasswordHasher` |
| Herencia (is-a) | `Point`/`LineString`/`Polygon` → `Geometry` |
| Polimorfismo | `minDistanceToKm`; serialización uniforme vía `type()` |
| SRP | controladores/repositorios/servicios/dominio con responsabilidades únicas |
| OCP | nuevo tipo de geometría = extender `Geometry` + una rama en `GeometryJsonCodec` |
| LSP | todos los subtipos cumplen el contrato de `Geometry` (verificado por tests) |
| ISP | tres interfaces de repositorio pequeñas, no un repositorio god |
| DIP | servicios dependen de interfaces; `AuthService` depende del puerto `PasswordHasher` |
| Composición | `GeographicEntity` compone `Geometry` |
| DRY | `GeoMath`, `EntityJsonCodec`/`GeometryJsonCodec` reutilizados |
| Excepciones tipadas | nunca `RuntimeException` genérico en reglas de dominio |

## 11. Decisiones de diseño relevantes

1. **Geometrías como jerarquía real**: la distancia varía genuinamente entre tipos; la herencia aquí
   es legítima (is-a) y el polimorfismo evita `instanceof` en `QueryService`.
2. **Caso de uso como punto de autorización**: la regla de rol está en `EntityService`, no en el
   controlador, para que la seguridad no dependa de la capa web.
3. **Puerto de hashing**: `AuthService` depende de la interfaz `PasswordHasher` (definida en
   `application`), con implementación `Sha256PasswordHasher` en infraestructura (DIP aplicado a
   seguridad; evita depender de Spring Security).
4. **Persistencia atómica**: archivo temporal + `Files.move` garantiza que una escritura fallida no
   corrompa los datos previos (verificado por `JsonDataStoreIntegrityTest`).
5. **Semántica AND en consultas**: cada criterio restringe el resultado; `matchedBy` documenta qué
   criterios matchearon, evitando falsos negativos en la interfaz.
6. **Mapa sin internet**: Leaflet y el mapa base son locales; la aplicación es completamente
   autónoma (cumple US3/FR-016).

## 12. Pruebas

La suite (81 tests, JUnit 5) prioriza:

1. Reglas de dominio: invariantes de geometrías, rangos WGS84, cierre de anillos, distancia y
   contención (unidad).
2. Casos de uso: consultas AND combinadas, autorización por rol, búsqueda por criterios.
3. Integración: persistencia atómica y endpoints HTTP (`@SpringBootTest` + `MockMvc`).

Comandos:

```sh
./mvnw test       # o mvnw.cmd test en Windows
./mvnw verify
```

## 13. Estructura del código

```
src/main/java/edu/udistrital/sig/
├── SigApplication.java
├── domain/
│   ├── model/        Coordinate, Geometry, Point, LineString, Polygon, GeoMath,
│   │                 GeographicEntity, Category, EntityNature, Role, User
│   ├── exception/    InvalidGeometryException, InvalidEntityException, ... 
│   └── repository/   EntityRepository, CategoryRepository, UserRepository
├── application/
│   └── service/      EntityService, QueryService, CategoryService, AuthService,
│                     TokenManager, PasswordHasher
└── infrastructure/
    ├── persistence/  JsonDataStore, EntityRepositoryJson, CategoryRepositoryJson,
    │                 UserRepositoryJson, SeedDataInitializer, PersistenceException
    ├── codec/        EntityJsonCodec, GeometryJsonCodec
    ├── security/     Sha256PasswordHasher
    └── web/          AuthController, EntityController, CategoryController,
                      BaseMapController, AuthInterceptor, WebConfig,
                      GlobalExceptionHandler, EntityRequestMapper

src/main/resources/
├── static/           index.html, js/app.js, css/style.css, leaflet/
└── data/             categories.json, users.json, entities.json, colombia-boundaries.geojson
```

## 14. Diagramas UML

El modelo UML completo está en PlantUML:

- Diagrama de clases: `docs/uml/classes.puml` (render: `java -jar plantuml.jar -o <dir> docs/uml/classes.puml`).
- Diagrama de casos de uso: `docs/uml/use-cases.puml`.
