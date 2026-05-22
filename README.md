# Domus Server

## Descripcion
Backend REST para Domus, una plataforma de administracion residencial orientada a condominios y comunidades. Expone servicios para autenticacion, usuarios, propiedades, unidades, residentes, visitas, encomiendas, reservas, estacionamientos, bodegas, mensajeria, notificaciones, auditoria y paneles operativos.

## Repositorios relacionados
- Frontend cliente: [Cristianlr95/domus-app](https://github.com/Cristianlr95/domus-app)

## Problema que resuelve
La operacion diaria de un edificio o condominio suele quedar repartida entre planillas, mensajes informales y registros manuales. Domus centraliza informacion operacional y de seguridad en una API con roles, permisos, trazabilidad y persistencia relacional.

## Funcionalidades principales
- Login con JWT y restauracion de usuario autenticado.
- Control de acceso basado en roles y permisos.
- Gestion de propiedades, unidades, residentes, visitas, encomiendas, reservas, estacionamientos y bodegas.
- Panel de conserjeria con metricas y actividad reciente.
- Mensajeria interna y notificaciones.
- Registro de auditoria para acciones relevantes.
- Migraciones versionadas con Flyway y documentacion OpenAPI.

## Stack tecnico
- Java 21
- Spring Boot 3.5
- Spring Web, Spring Security, Spring Data JPA y Bean Validation
- PostgreSQL 16
- Flyway
- JWT con `jjwt`
- OpenAPI/Swagger con `springdoc-openapi`
- Maven Wrapper
- JUnit 5, Spring Security Test, Testcontainers, H2, jqwik y JaCoCo
- Docker y Docker Compose

## Arquitectura / Estructura
El proyecto sigue un monolito modular por dominio. Cada modulo agrupa controller, service, repository, entity, DTOs, mapper y clases de soporte cuando aplica.

```text
domus-server/
  src/main/java/com/domus/server/
    auth/              # autenticacion y contratos JWT
    common/            # seguridad, excepciones y respuestas comunes
    concierge/         # dashboard operativo
    visits/            # visitas
    packages/          # encomiendas
    residents/         # residentes
    units/             # unidades
    parking/           # estacionamientos
    storages/          # bodegas
    user/              # usuarios, roles y permisos
    audit/             # auditoria
    config/            # seguridad y OpenAPI
  src/main/resources/
    db/migration/      # migraciones Flyway
    application*.properties
```

## Instalacion y ejecucion local
Requisitos:

- Java 21
- Docker Desktop o PostgreSQL local
- Maven Wrapper incluido en el repositorio

### Opcion recomendada con Docker Compose

```bash
docker compose up --build
```

La API queda disponible en `http://localhost:8080`.

### Ejecucion local contra PostgreSQL

```bash
# Windows PowerShell
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="domus_dev"
$env:DB_USERNAME="domus_dev"
$env:DB_PASSWORD="domus_dev"
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run
```

```bash
# Linux/macOS
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=domus_dev
export DB_USERNAME=domus_dev
export DB_PASSWORD=domus_dev
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

Comandos utiles:

```bash
./mvnw test
./mvnw verify
```

En Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

## Endpoints de referencia
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Health check: `http://localhost:8080/actuator/health`

## Estado del proyecto
Proyecto en estado funcional. El backend ya contiene los dominios principales de operacion residencial, autenticacion JWT, control de permisos y migraciones versionadas. La evolucion pendiente se concentra en hardening productivo, observabilidad y autorizacion fina.

Avance funcional estimado: `92%`.

## Funcionalidades implementadas
- Autenticacion JWT.
- Usuarios, roles, permisos y gestion basica de cuentas.
- CRUD y cambios de estado para dominios operativos.
- Inventario de propiedades con filtros, permisos y auditoria.
- Reservas de espacios comunes con validacion de disponibilidad y auditoria.
- Dashboard de conserjeria.
- Dashboard administrativo.
- Mensajeria, notificaciones y auditoria.
- Auditoria operacional para propiedades, reservas, visitas, encomiendas, residentes, unidades, estacionamientos, bodegas, login y mensajeria.
- Validacion de esquema con Flyway.

## Funcionalidades en desarrollo o parciales
- Reglas de autorizacion fina por identidad y dominio.
- Notificaciones avanzadas y preferencias.
- Paginacion/filtros estandarizados en todos los listados.
- Hardening productivo de seguridad, observabilidad y despliegue.

## Proximas mejoras
- Incorporar refresh tokens o estrategia de sesion mas robusta.
- Mejorar politicas de permisos por accion y propietario del recurso.
- Agregar pruebas de integracion por modulo critico.
- Formalizar ambientes `staging` y `production`.
- Documentar flujos principales con diagramas de secuencia.

## Valor profesional del proyecto
Este backend demuestra competencias en diseno de APIs REST con Spring Boot, modelado relacional, seguridad con JWT, migraciones controladas, separacion por dominios, documentacion OpenAPI, pruebas automatizadas y preparacion para despliegue con Docker. Tambien muestra criterio para construir software operativo con trazabilidad, roles y mantenibilidad.

## Que conviene revisar primero
- Seguridad JWT y control de acceso por roles/permisos.
- Modulos operativos: visitas, encomiendas, residentes, unidades y auditoria.
- Migraciones Flyway y estructura modular por dominio.
- Endpoints documentados con Swagger/OpenAPI.
