# ms-campuslab-bookings

Microservicio de dominio encargado de la gestión de reservas de laboratorios y equipos académicos dentro de la plataforma CampusLab.

## Responsabilidades

- Crear, consultar y actualizar el estado de las reservas.
- Publicar eventos de cambio de estado a RabbitMQ para que `ms-campuslab-notify` notifique al estudiante y al técnico.
- Registrar cada acción relevante en `ms-campuslab-audit` vía llamada REST síncrona.

## Stack técnico

- Java 21 + Spring Boot 3.3
- Spring Data JPA + MySQL
- Spring AMQP (RabbitMQ)
- Spring Security OAuth2 Resource Server (JWT de Azure AD)
- Flyway (migraciones de base de datos)

## Configuración

Variables de entorno principales (`application.yml`):

| Variable | Descripción | Default |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL de conexión a MySQL | `jdbc:mysql://localhost:3306/bookings_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de MySQL | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Password de MySQL | (vacío) |
| `SPRING_RABBITMQ_HOST` | Host de RabbitMQ | `localhost` |
| `AUDIT_SERVICE_URL` | URL base de `ms-campuslab-audit` | `http://localhost:8085` |

Puerto por defecto: **8081**

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/bookings` | Crea una nueva reserva (queda en estado `SOLICITADA`) |
| `GET` | `/api/bookings/{id}` | Obtiene una reserva por ID |
| `PUT` | `/api/bookings/{id}/status` | Actualiza el estado de una reserva |
| `GET` | `/api/bookings?status=&from=&to=` | Busca reservas con filtros opcionales |

### Estados válidos (`BookingStatus`)


### Ejemplo: crear una reserva

```http
POST /api/bookings
Content-Type: application/json

{
  "resourceId": "LAB-01",
  "studentId": "STU-123",
  "from": "2026-09-20T10:00:00",
  "to": "2026-09-20T12:00:00"
}
```

### Ejemplo: cambiar estado

```http
PUT /api/bookings/1/status
Content-Type: application/json

{
  "status": "APROBADA"
}
```

## Flujo interno al cambiar de estado

1. Se actualiza el registro en MySQL.
2. Se publica un evento a RabbitMQ (`cmd.direct` → cola `notify.booking.queue`) para que `notify` envíe la notificación correspondiente.
3. Se llama a `POST /api/audit/events` en `ms-campuslab-audit` para dejar constancia de quién hizo el cambio.

## Cómo correr localmente

```bash
mvn spring-boot:run
```

Requiere MySQL y RabbitMQ corriendo (ver `/infra` para levantarlos con Docker Compose).

## Cómo correr con Docker

```bash
docker build -t ms-campuslab-bookings .
docker run -p 8081:8081 ms-campuslab-bookings
```

O como parte del stack completo desde `/infra`:

```bash
docker compose up --build
```

## Seguridad

Todos los endpoints (excepto `/actuator/health`) requieren un JWT válido emitido por Azure AD en el header: