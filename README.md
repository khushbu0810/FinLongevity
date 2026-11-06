# FinLongevity – Microservices Setup

## Application Details

| Application | Port | URL | Purpose |
|---|---:|---|---|
| Discovery Server | 8010 | http://localhost:8010 | Eureka dashboard and service registry |
| Config Server | 8012 | http://localhost:8012/identity-service/default | Serves Identity Service configuration |
| Identity Service | 8080 | http://localhost:8080/health | Initial service health endpoint |
| API Gateway | 8082 | http://localhost:8082/actuator/health | API entry point and health endpoint |

## Start-up Order

Start the applications in the following order:

1. **Discovery Server** — http://localhost:8010
2. **Config Server** — http://localhost:8012
3. **Identity Service** — http://localhost:8080
4. **API Gateway** — http://localhost:8082

## Identity-service
In this service we added database-backed customer registration, BCrypt password hashing, login, JWT access-token generation, and a protected current-user endpoint.

## Endpoints

Direct Identity Service (port 8080):

- `POST http://localhost:8080/auth/register`
- `POST http://localhost:8080/auth/login`
- `GET http://localhost:8080/users/me` (requires Bearer token)

Through API Gateway (port 8082):

- `POST http://localhost:8082/api/identity/auth/register`
- `POST http://localhost:8082/api/identity/auth/login`
- `GET http://localhost:8082/api/identity/users/me` (requires Bearer token)

## Register request

```json
{
  "fullName": "Demo Customer",
  "email": "demo.customer@example.com",
  "password": "StrongPass123"
}
```

Expected: HTTP 201 Created, with user ID, name, email, role and creation time. The password is never returned.

## Login request

```json
{
  "email": "demo.customer@example.com",
  "password": "StrongPass123"
}
```

Expected: HTTP 200 with `accessToken`, `tokenType`, `expiresIn`, and safe user details.
