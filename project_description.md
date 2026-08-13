# Proyecto: Sistema de Información Geográfica (SIG) Orientado a Objetos

## 1. Contexto


El proyecto consiste en desarrollar un Sistema de Información Geográfica (SIG) aplicando los principios fundamentales de la Programación Orientada a Objetos (POO).

El propósito del sistema es permitir el registro, almacenamiento, consulta, actualización y visualización de información asociada a diferentes ubicaciones geográficas.

El sistema deberá gestionar diferentes tipos de entidades geográficas, asociando a cada una información descriptiva y su respectiva ubicación o representación espacial.

    
*   almacenar la información asociada a las entidades;
    
*   consultar entidades existentes;
    
*   actualizar la información de las entidades;
    
*   eliminar entidades;
    
*   asociar información espacial a cada entidad;
    
*   consultar entidades por ubicación;
    
*   consultar entidades por categoría;
    
*   consultar entidades por atributos;
    
*   visualizar las entidades sobre un mapa;
    
*   manejar diferentes tipos de usuarios;
    
*   restringir operaciones según los permisos del usuario.
    

Las operaciones de creación, actualización y eliminación deben estar disponibles únicamente para los usuarios que cuenten con los permisos correspondientes.

## 4. Entidades geográficas

El dominio contempla inicialmente entidades tales como:

*   puntos de interés;
    
*   vías;
    
*   barrios;
    
*   instituciones;
    
*   establecimientos comerciales;
    
*   zonas de interés.
    

Cada entidad geográfica debe contener información descriptiva y una ubicación o representación espacial.

Los diferentes tipos de entidades podrán tener atributos y comportamientos particulares según su naturaleza.

La lista anterior representa conceptos del dominio y no implica necesariamente que cada uno deba convertirse directamente en una clase independiente.

## 5. Información geográfica

Cada entidad debe estar asociada a información espacial que permita representar adecuadamente su localización o extensión geográfica.

No todas las entidades deben asumirse como un único par de coordenadas.

El sistema debe permitir representar, cuando corresponda, geometrías equivalentes a:

*   puntos;
    
*   líneas;
    
*   polígonos.
    

Esto debe permitir representar adecuadamente elementos de diferente naturaleza, como puntos de interés, vías, barrios o zonas.

Las coordenadas y geometrías deben cumplir reglas de validez coherentes con su naturaleza geográfica.

## 6. Consultas

El sistema debe permitir consultar entidades utilizando diferentes criterios.

Como mínimo:

*   ubicación;
    
*   categoría;
    
*   atributos descriptivos.
    

La consulta por ubicación debe permitir recuperar entidades a partir de criterios espaciales definidos dentro del alcance del proyecto.

El comportamiento espacial exacto de estas consultas deberá mantenerse acorde con las necesidades de un proyecto académico introductorio de SIG y POO, evitando incorporar capacidades de análisis espacial avanzado que no sean necesarias.

## 7. Categorías y atributos

Las entidades geográficas podrán clasificarse mediante categorías.

Las categorías deben poder utilizarse como criterio de consulta.

Las entidades deberán contener información descriptiva común y podrán contener atributos particulares cuando su naturaleza lo requiera.

## 8. Gestión de entidades

El sistema debe implementar operaciones CRUD sobre las entidades geográficas.

### Creación

Los usuarios autorizados podrán registrar nuevas entidades con su información descriptiva y espacial.

### Consulta

Los usuarios podrán consultar las entidades almacenadas y visualizar su información.

### Actualización

Los usuarios autorizados podrán modificar la información de entidades existentes.

### Eliminación

Los usuarios autorizados podrán eliminar entidades existentes.

Los cambios realizados deben conservarse entre ejecuciones de la aplicación.

## 9. Visualización cartográfica

El sistema debe permitir visualizar las entidades geográficas sobre un mapa.

La visualización debe permitir identificar espacialmente las entidades registradas y los resultados obtenidos mediante las consultas.

Las diferentes representaciones espaciales utilizadas por las entidades deberán poder visualizarse adecuadamente.

La interfaz cartográfica debe formar parte de la misma aplicación.

## 10. Usuarios y permisos

El sistema debe manejar diferentes tipos de usuarios y permisos.

Como mínimo se deben contemplar:

### Usuario de consulta

Puede:

*   consultar entidades;
    
*   realizar búsquedas;
    
*   visualizar entidades sobre el mapa.
    

### Usuario administrador

Puede:

*   consultar entidades;
    
*   realizar búsquedas;
    
*   visualizar entidades;
    
*   registrar entidades;
    
*   actualizar entidades;
    
*   eliminar entidades.
    

No se requiere un sistema complejo de gestión de identidad.

El manejo de usuarios y permisos debe mantenerse acorde con el alcance académico del proyecto.

## 11. Persistencia

La información debe conservarse entre diferentes ejecuciones de la aplicación.

No se utilizará una base de datos.

La persistencia se realizará mediante archivos JSON locales.

El proyecto podrá contener datos iniciales precargados mediante archivos JSON.

Las operaciones CRUD deben reflejarse en el almacenamiento local para que las modificaciones realizadas puedan recuperarse posteriormente.

## 12. Requisitos académicos de Programación Orientada a Objetos

El diseño e implementación del sistema deben permitir demostrar de manera significativa los siguientes conceptos:

*   clases y objetos;
    
*   encapsulamiento;
    
*   abstracción;
    
*   herencia;
    
*   polimorfismo;
    
*   relaciones entre objetos;
    
*   reutilización de código.
    

Estos conceptos deben aplicarse de acuerdo con las necesidades reales del dominio.

No deben introducirse relaciones, jerarquías o abstracciones artificiales únicamente con el propósito de demostrar un concepto de POO.

El sistema debe permitir explicar y justificar cómo los principios de POO contribuyen al modelado y solución del problema.

## 13. Separación de responsabilidades

El sistema debe mantener una separación clara entre:

*   lógica del negocio;
    
*   gestión de datos;
    
*   interacción con el usuario;
    
*   visualización geográfica.
    

El diseño debe facilitar la comprensión, mantenimiento y evolución del sistema.

## 14. Restricciones tecnológicas

El proyecto se desarrollará como una única aplicación autocontenida.

### Lenguaje

*   Java 21.
    

### Framework

*   Spring Boot.
    
*   Spring Web.
    

### Gestión del proyecto

*   Maven.
    

### Persistencia

*   archivos JSON locales;
    
*   Jackson para serialización y deserialización.
    

No se utilizarán:

*   bases de datos;
    
*   JPA;
    
*   Hibernate;
    
*   Spring Data.
    

### Interfaz de usuario

La interfaz formará parte de la misma aplicación.

Se utilizarán:

*   HTML;
    
*   CSS;
    
*   JavaScript vanilla;
    
*   Leaflet para visualización cartográfica.
    

No se utilizará un framework frontend adicional.

### Información geográfica

GeoJSON podrá utilizarse como formato de representación o intercambio de información geográfica cuando resulte apropiado.

### Testing

Se utilizarán:

*   JUnit 5;
    
*   Spring Boot Test;
    
*   Mockito cuando resulte necesario.
    

## 15. Aplicación autocontenida

La aplicación debe poder ejecutarse localmente como una única aplicación.

El funcionamiento principal del sistema no debe depender de:

*   bases de datos externas;
    
*   servicios cloud;
    
*   APIs externas;
    
*   servicios externos de autenticación;
    
*   microservicios;
    
*   infraestructura distribuida.
    

Los datos administrados por el SIG deberán almacenarse localmente.

Las librerías necesarias podrán incluirse como dependencias o recursos del proyecto.

Los recursos necesarios para la interfaz deberán formar parte de la aplicación.

## 16. Alcance

La primera versión debe priorizar el cumplimiento completo de los requerimientos académicos y funcionales definidos.

El proyecto debe mantener una complejidad adecuada para una asignatura de Programación Orientada a Objetos.

El objetivo es construir un SIG funcional que permita demostrar correctamente el diseño orientado a objetos, no reproducir todas las capacidades de un software GIS profesional.