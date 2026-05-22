# Deployment de Domus Backend

## Resumen tecnico

- Stack: Java 21, Spring Boot 3.5.12, Maven Wrapper
- Persistencia: PostgreSQL + Spring Data JPA + Flyway
- Seguridad: Spring Security + JWT propio + RBAC por permisos
- Observabilidad base: Actuator, logs de request, correlation id, auditoria de negocio parcial
- Arranque local sin variables: usa defaults seguros para desarrollo
- Perfil recomendado para despliegue: `prod`

## Estructura y modulos

El backend esta organizado por modulos de dominio y capas convencionales:

- `auth`: login y emision de JWT
- `user`: usuarios, roles y permisos
- `residents`
- `units`
- `visits`
- `packages`
- `parking`
- `storages`
- `messaging`
- `notifications`
- `audit`
- `concierge`
- `health`
- `common`: seguridad, errores y respuestas base
- `config`: seguridad y OpenAPI

Patron principal por modulo:

- `controller`
- `service`
- `repository`
- `entity`
- `dto`
- `mapper`
- `support` para specifications

## Endpoints criticos

- `POST /api/v1/auth/login`
- `GET /api/v1/users/me`
- `GET /api/v1/health`
- `GET /actuator/health`
- `GET /api/v1/concierge/dashboard`
- CRUD operacionales:
  - `/api/v1/visits`
  - `/api/v1/packages`
  - `/api/v1/residents`
  - `/api/v1/units`
  - `/api/v1/parking`
  - `/api/v1/storages`
  - `/api/v1/messages`
  - `/api/v1/conversations`
  - `/api/v1/notifications`
  - `/api/v1/audit-logs`

## Seguridad actual

- Autenticacion por JWT Bearer con refresh tokens revocables
- Filtro JWT stateless
- RBAC con permisos finos por `@PreAuthorize`
- CORS configurable por variable de entorno
- Errores de seguridad devueltos en JSON uniforme
- Correlation ID por request en header `X-Correlation-Id`

Roles detectados:

- `ADMIN`
- `CONSERJERIA`
- `RESIDENTE`

Permisos principales:

- `visits.read`, `visits.create`, `visits.update`
- `packages.read`, `packages.create`, `packages.update`
- `residents.read`, `residents.manage`
- `units.read`, `units.manage`
- `parking.read`, `parking.manage`
- `storages.read`, `storages.manage`
- `messaging.read`, `messaging.create`
- `notifications.read`
- `concierge.dashboard.read`
- `users.read`, `users.manage`
- `roles.read`, `permissions.read`
- `admin.dashboard.read`
- `audit.read`

## Variables de entorno necesarias

Minimas para desarrollo:

- `SPRING_PROFILES_ACTIVE=dev`
- `SERVER_PORT=8080`
- `DB_HOST=localhost`
- `DB_PORT=5432`
- `DB_NAME=domus_dev`
- `DB_USERNAME=domus_dev`
- `DB_PASSWORD=domus_dev`
- `JWT_SECRET=<base64 fuerte recomendado>`
- `JWT_EXPIRATION_SECONDS=28800`
- `REFRESH_TOKEN_EXPIRATION_DAYS=14`
- `APP_CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:8100,http://127.0.0.1:8100`

Adicionales recomendadas:

- `DB_POOL_MAX_SIZE=10`
- `DB_POOL_MIN_IDLE=2`
- `DB_POOL_CONNECTION_TIMEOUT_MS=30000`
- `DB_POOL_VALIDATION_TIMEOUT_MS=5000`
- `APP_SECURITY_EXPOSE_DOCS=true|false`
- `MANAGEMENT_EXPOSED_ENDPOINTS=health,info`
- `LOG_LEVEL_ROOT=INFO`
- `LOG_LEVEL_APP=INFO`

Reglas por ambiente:

- `dev`
  - usa defaults locales para PostgreSQL
  - habilita Swagger
  - expone `health` con detalle
- `prod`
  - exige `DB_HOST`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET`
  - deshabilita docs por defecto
  - reduce Actuator a `health`
  - oculta detalles del estado de salud

## Como correr en local

### Opcion 1: Maven + PostgreSQL local

1. Crear base y usuario:

```sql
CREATE USER domus_dev WITH PASSWORD 'domus_dev';
CREATE DATABASE domus_dev OWNER domus_dev;
```

2. Copiar variables desde `.env.example` a tu entorno o archivo `.env`.

3. Ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

4. Verificar:

- API: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`
- Health app: `http://localhost:8080/api/v1/health`
- Swagger dev: `http://localhost:8080/swagger-ui.html`

### Opcion 2: Docker Compose para desarrollo

```powershell
docker compose up --build
```

Esto levanta:

- PostgreSQL 16
- backend `domus-server`

## Base de datos y migraciones

- Motor soportado para runtime: PostgreSQL
- Estrategia de schema: `hibernate.ddl-auto=validate`
- Migraciones: Flyway en `src/main/resources/db/migration`
- Validacion de migraciones: `spring.flyway.validate-on-migrate=true`

Orden actual:

1. `V1__create_auth_and_users_schema.sql`
2. `V2__create_visits_module.sql`
3. `V3__create_packages_module.sql`
4. `V4__create_residents_module.sql`
5. `V5__create_units_module.sql`
6. `V6__create_parking_module.sql`
7. `V7__create_storages_module.sql`
8. `V8__create_messaging_module.sql`
9. `V9__create_notifications_module.sql`
10. `V10__add_rbac_permissions.sql`
11. `V11__create_audit_logs.sql`

Practica recomendada:

- nunca editar migraciones ya ejecutadas en ambientes compartidos
- agregar nuevas versiones `V12+`
- validar migraciones en CI contra una base efimera PostgreSQL
- respaldar la base antes de aplicar cambios productivos

## Docker

Archivos agregados:

- `Dockerfile`: build multi-stage con Java 21
- `.dockerignore`
- `docker-compose.yml` para desarrollo

Build manual:

```powershell
docker build -t domus-server .
```

Run manual:

```powershell
docker run --rm -p 8080:8080 ^
  -e SPRING_PROFILES_ACTIVE=prod ^
  -e DB_HOST=<host> ^
  -e DB_PORT=5432 ^
  -e DB_NAME=<db> ^
  -e DB_USERNAME=<user> ^
  -e DB_PASSWORD=<password> ^
  -e JWT_SECRET=<base64> ^
  -e APP_CORS_ALLOWED_ORIGINS=https://tu-frontend.com ^
  domus-server
```

## Despliegue recomendado

### Render

- Tipo: Web Service con Docker
- Build: `docker build`
- Run: imagen resultante
- Variables: cargar todas las de `prod`
- Base de datos: PostgreSQL administrado de Render
- Health check: `/actuator/health`

### Railway

- Tipo: servicio Docker
- Adjuntar PostgreSQL administrado
- Definir `SPRING_PROFILES_ACTIVE=prod`
- Limitar `APP_CORS_ALLOWED_ORIGINS` al frontend real
- Health check: `/actuator/health`

### VPS

Recomendado:

- Nginx o Caddy como reverse proxy con TLS
- backend en contenedor o `systemd`
- PostgreSQL administrado o instalado aparte
- secretos fuera del repo
- firewall abierto solo para 80/443 y acceso interno al backend

## Logs y observabilidad basica

Implementado:

- logs por request con metodo, path, status y duracion
- correlation id por request
- errores uniformes con `path` y `correlationId`
- Actuator `health`

Recomendado para el siguiente nivel:

- centralizacion de logs en Loki, Datadog, ELK o similar
- metricas JVM y HTTP
- alertas sobre 5xx, latencia y fallos de login
- dashboard de crecimiento de `audit_logs`

## Manejo seguro de errores

- no se expone stacktrace al cliente
- errores 401/403 salen en JSON uniforme
- errores 5xx se registran en logs internos
- el correlation id permite seguir incidentes entre cliente, app y reverse proxy

## Secretos

- no guardar secretos reales en Git
- usar secretos del proveedor o variables de entorno
- `JWT_SECRET` debe ser fuerte y en Base64
- rotar secretos con procedimiento controlado
- mantener `.env` fuera del versionado

## Backups

Minimo recomendado:

- backup diario automatizado de PostgreSQL
- retencion diaria y semanal
- restore probado mensualmente
- almacenamiento cifrado y fuera del nodo principal

## Riesgos detectados

- `README.md` no refleja el stack real actual y menciona OAuth2
- existe secreto JWT default en `dev`; no debe trasladarse a `prod`
- `docker-compose.yml` usa secreto de desarrollo por conveniencia local
- auditoria aun no cubre todos los modulos operacionales con el mismo nivel
- listados sin paginacion pueden afectar escalabilidad
- hay paquetes legacy `com.domus.demo` que pueden inducir confusion
- no hay pipeline CI/CD versionado en este repo
- permisos sembrados como `users.manage` y `admin.dashboard.read` no tienen uso visible actual

## Checklist previo a produccion

- definir `SPRING_PROFILES_ACTIVE=prod`
- cargar `JWT_SECRET` fuerte y secreto por plataforma
- restringir `APP_CORS_ALLOWED_ORIGINS`
- confirmar que Swagger quede deshabilitado
- apuntar a PostgreSQL administrado
- probar migraciones sobre copia o staging
- validar login y permisos criticos
- monitorear `/actuator/health`
- configurar backups
- activar TLS y reverse proxy
- revisar rotacion de logs y alertas
