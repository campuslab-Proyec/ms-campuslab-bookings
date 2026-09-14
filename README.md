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