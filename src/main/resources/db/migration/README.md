# Migraciones Flyway

## Proposito
Esta carpeta contiene las migraciones SQL que definen y evolucionan el esquema PostgreSQL de Domus.

## Convencion
```text
V{numero}__descripcion_en_snake_case.sql
```

Ejemplos actuales:
- `V1__create_auth_and_users_schema.sql`
- `V2__create_visits_module.sql`
- `V3__create_packages_module.sql`
- `V10__add_rbac_permissions.sql`
- `V11__create_audit_logs.sql`
- `V13__create_properties_module.sql`

## Reglas de mantenimiento
- No modificar migraciones ya aplicadas en ambientes compartidos.
- Crear una nueva migracion para cada cambio de esquema.
- Mantener nombres claros y orientados al dominio.
- Validar con `mvnw verify` o levantando la aplicacion contra una base limpia.
