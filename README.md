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
