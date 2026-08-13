# Manual Básico de Instalación y Uso — SIG Académico

Versión: 1.0.0
Fecha: 2026-08-13

Sistema de Información Geográfica académico (Java 21, Spring Boot 3). Aplicación web autónoma:
mapa base local de Colombia, entidades geográficas (puntos, líneas y polígonos), consultas
combinadas, autenticación por roles y persistencia en archivos JSON locales.

---

## 1. Requisitos previos

| Requisito | Versión |
|-----------|---------|
| Java (JDK) | 21 o superior (`java -version`) |
| Maven | 3.9 o superior (o usar el *Maven Wrapper* incluido) |
| Navegador | Cualquiera moderno (Chrome, Edge, Firefox) |
| Puerta de red | 8080 libre |
| Internet | No requerido para la aplicación (Leaflet y mapa base son locales) |

Verificar:

```sh
java -version
mvn -version
```

> En Windows, el comando de Maven puede ser `mvn` o `mvn.cmd`, según la instalación.

## 2. Descarga y estructura

El código está en el repositorio público:

```sh
git clone https://github.com/ErikaEspejo/proyecto-poo-sig.git
cd proyecto-poo-sig
```

Estructura principal:

```
pom.xml                       Configuración de Maven (Spring Boot 3, Java 21)
mvnw / mvnw.cmd               Maven Wrapper
src/main/java/...             Código fuente (dominio, aplicación, infraestructura)
src/main/resources/static/    Frontend (index.html, js, css, leaflet local)
src/main/resources/data/      Datos semilla (categorías, usuarios, entidades, mapa base)
src/test/java/...             Pruebas (JUnit 5)
docs/                         Documentación (diseño, UML, casos de uso, trazabilidad)
specs/                        Especificaciones y contrato de API
data/                         Datos de ejecución (se crea en el primer arranque)
```

## 3. Instalación y ejecución

### Opción A — Ejecutar directamente (modo desarrollo)

```sh
./mvnw spring-boot:run        # Linux/macOS
mvnw.cmd spring-boot:run      # Windows PowerShell
```

### Opción B — Empaquetar y ejecutar el JAR

```sh
./mvnw clean package
java -jar target/sig-1.0.0.jar
```

En Windows:

```powershell
.\mvnw.cmd clean package
java -jar target\sig-1.0.0.jar
```

### Opción C — Maven instalado (sin wrapper)

```sh
mvn spring-boot:run
# o
mvn clean package && java -jar target/sig-1.0.0.jar
```

### Verificación

El arranque termina con una línea similar a:

```
Tomcat started on port 8080 (http)
Started SigApplication in X.XXX seconds
```

Abrir el navegador en **http://localhost:8080**.

### Datos de ejecución

En el **primer arranque**, la aplicación crea la carpeta `data/` junto al JAR y copia los datos
semilla (`categories.json`, `users.json`, `entities.json` y `colombia-boundaries.geojson`). A partir
de entonces los cambios realizados desde la interfaz se guardan ahí y persisten entre reinicios.

> El directorio de datos se puede cambiar con la propiedad `sig.data.directory`:
> `java -jar target/sig-1.0.0.jar --sig.data.directory=./mi-data`.

## 4. Cuentas precargadas

| Usuario  | Contraseña  | Rol              | Permisos |
|----------|-------------|------------------|----------|
| `admin`  | `admin123`  | Administrador    | Consultar y administrar entidades |
| `consulta` | `consulta123` | Usuario de consulta | Solo lectura |

## 5. Uso de la aplicación

### 5.1 Iniciar sesión

1. En la pantalla inicial ingresar usuario y contraseña y pulsar **Iniciar sesión**.
2. La sesión queda activa y se recuerda en el navegador al recargar la página.
3. **Cerrar sesión** con el botón correspondiente en el encabezado.

### 5.2 Explorar el mapa

- El mapa base muestra los departamentos de Colombia (local, sin internet).
- Cada entidad se dibuja según su tipo: **punto** (marcador), **línea** o **polígono** (área).
- Al hacer clic en una entidad se abre un detalle con nombre, descripción, categoría, naturaleza y
  atributos.

### 5.3 Consultar entidades

En el panel de búsqueda se pueden combinar los criterios (todos opcionales, al menos uno):

- **Categoría**: selección del catálogo (Turismo, Vía, Barrio, Institución, Comercio, Zona).
- **Texto**: coincide con el nombre o la descripción (sin distinguir mayúsculas).
- **Atributo**: texto libre sobre los valores de los atributos de la entidad.
- **Proximidad**: latitud, longitud y radio en km (p. ej. un punto cercano, una vía a menos de X km,
  o un polígono que contenga el punto).

Los criterios se combinan con **AND**: cada criterio restringe aún más el resultado. Los resultados
se resaltan en el mapa, se listan a la derecha y el contador se actualiza. Si no hay coincidencias se
muestra "sin resultados" (no es un error).

### 5.4 Administrar entidades (solo Administrador)

El panel de administración está disponible únicamente para el rol **Administrador** (la interfaz lo
oculta para usuarios de consulta y el servidor además lo impide con `403`).

- **Registrar**: completar nombre, descripción, naturaleza, categoría y atributos opcionales; dibujar
  la geometría sobre el mapa (punto, línea o polígono) y guardar.
- **Actualizar**: seleccionar una entidad, modificar sus datos/geometría y guardar.
- **Eliminar**: seleccionar una entidad y confirmar su eliminación.

Los mensajes de error se muestran en español (p. ej. geometría inválida, campos obligatorios,
credenciales inválidas, sin permisos).

## 6. API REST (referencia rápida)

| Método | Ruta | Autenticación | Descripción |
|--------|------|---------------|-------------|
| `POST` | `/api/auth/login` | Pública | `{ "username": "...", "password": "..." }` → `{ "token", "username", "role" }` |
| `GET` | `/api/map/basemap` | Pública | Mapa base local (GeoJSON de Colombia) |
| `GET` | `/api/categories` | Token | Catálogo de categorías |
| `GET` | `/api/entities` | Token | Lista de entidades |
| `GET` | `/api/entities/{id}` | Token | Detalle de una entidad |
| `GET` | `/api/entities/query?category=&text=&attribute=&latitude=&longitude=&radiusKm=` | Token | Consulta combinada |
| `POST` | `/api/entities` | Token + Admin | Registrar entidad (GeoJSON) |
| `PUT` | `/api/entities/{id}` | Token + Admin | Actualizar entidad |
| `DELETE` | `/api/entities/{id}` | Token + Admin | Eliminar entidad |

Los endpoints protegidos usan el encabezado:

```
Authorization: Bearer <token>
```

Ejemplo con PowerShell:

```powershell
$login = Invoke-RestMethod -Uri http://localhost:8080/api/auth/login -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
$headers = @{ Authorization = "Bearer $($login.token)" }
Invoke-RestMethod -Uri "http://localhost:8080/api/entities/query?text=parque" -Headers $headers
```

## 7. Pruebas

```sh
./mvnw test        # ejecuta las 81 pruebas
./mvnw verify      # compila, prueba y verifica el paquete
```

En Windows: `.\mvnw.cmd test`.

## 8. Solución de problemas

| Problema | Causa probable | Solución |
|----------|----------------|----------|
| `java: not found` / `JAVA_HOME` no definido | Java 21 no está en el PATH | Instalar JDK 21 y configurar `JAVA_HOME` |
| `Port 8080 was already in use` | Otra aplicación ocupa el puerto | Cerrar la otra aplicación o cambiar el puerto: `java -jar target/sig-1.0.0.jar --server.port=9090` |
| `mvn: command not found` | Maven no está instalado | Usar el wrapper: `./mvnw.cmd spring-boot:run` |
| Mensaje de error de escritura en `data/` | Sin permisos de escritura en el directorio | Ejecutar con permisos o cambiar el directorio con `--sig.data.directory` |
| Cambios que no se guardan | El JAR se ejecuta desde otra carpeta | Los datos se guardan en `data/` relativo al directorio de ejecución; usar la misma carpeta o fijar `sig.data.directory` |
| Pantalla en blanco / favicon 404 | No es un error; la app sigue funcionando | Recargar `http://localhost:8080` |

## 9. Documentación académica

- Diseño del sistema: `docs/design.md`
- Diagramas UML (PlantUML): `docs/uml/classes.puml` y `docs/uml/use-cases.puml`
- Casos de uso: `docs/use-cases.md`
- Trazabilidad OOP: `docs/traceability.md`
- Especificaciones: `specs/001-geographic-information-system/`
