# Cloud Native Kubernetes

A simplified e-commerce platform built as **3 independent Java/Spring Boot microservices**, containerized and ready to run on **Kubernetes** — with both plain YAML manifests and a **Helm chart**. Portfolio project focused on demonstrating cloud-native architecture, Clean Code, DDD and infrastructure best practices.

## Architecture

```
                              +-------------------+
                    /api/products  Ingress (nginx)  /api/inventory, /api/orders
                              +---------+---------+
                                        |
        +-------------------+----------+----------+-------------------+
        |                   |                     |                   |
+---------------+   +------------------+   +---------------+   +-----------+
| product-service|   | inventory-service|   | order-service |   |  Redis    |
|   :8081        |<--|     :8082        |   |    :8083      |   | (cache)   |
+-------+--------+   +--------+---------+   +-------+-------+   +-----------+
        |                     |                     |
        |                     |  REST (RestClient)  |
        |                     +---------------------+
        |                                            |
        +--------------------- REST -----------------+
        |
+---------------------------------------------------------+
|                     PostgreSQL                           |
|   productdb   |   inventorydb   |   orderdb (3 schemas)   |
+---------------------------------------------------------+
```

- **product-service** (port 8081): product catalog. Read-through cache with **Redis**.
- **inventory-service** (port 8082): per-product stock control. Validates product existence by calling `product-service`.
- **order-service** (port 8083): order creation and lifecycle. Orchestrates `product-service` (current price) and `inventory-service` (stock reservation).

Each service follows a layered structure inspired by **DDD**:

```
com.marcosperboni.cloudnative.<service>
├── domain          # entities, business rules, ports (interfaces)
├── application     # use cases, DTOs, mapping
├── infrastructure  # JPA, REST clients, exception handling
└── interfaces.web  # REST controllers
```

## Tech stack

| Layer              | Technology                                    |
|--------------------|------------------------------------------------|
| Language           | Java 25                                        |
| Framework          | Spring Boot 4.0.8 (Web, Data JPA, Validation, Cache, Actuator) |
| Database           | PostgreSQL 16 (1 database per service, managed with Flyway) |
| Cache              | Redis 7                                        |
| Service-to-service | REST (Spring `RestClient`)                     |
| Testing            | JUnit 5, Mockito, Spring `@WebMvcTest`/MockMvc |
| Containerization   | Podman + multi-stage Containerfile             |
| Local orchestration| Podman Compose                                 |
| Kubernetes         | Deployment, Service, ConfigMap, Secret, Ingress, HPA, rolling update |
| K8s packaging      | Helm chart                                      |
| CI/CD              | GitHub Actions (build, tests, image publishing to GHCR) |

## Running locally with Podman

Prerequisites: [Podman](https://podman.io/) installed (`podman compose` working).

```bash
# from the repository root
podman compose up --build
```

This brings up: PostgreSQL (with the 3 databases created automatically), Redis and the 3 microservices.

| Service            | Local URL                            |
|---------------------|--------------------------------------|
| product-service      | http://localhost:18081/api/products |
| inventory-service     | http://localhost:18082/api/inventory |
| order-service         | http://localhost:18083/api/orders   |

> The external ports (18081-18083, 5433, 6380) were chosen to avoid clashing with other services already running on the local machine. Inside the compose network, the services still talk to each other on their "native" ports (8081/8082/8083, 5432, 6379), which are the same ports used in the Kubernetes manifests.

To tear everything down (including volumes):

```bash
podman compose down -v
```

### API usage examples

```bash
# create a product
curl -X POST http://localhost:18081/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Mechanical Keyboard","description":"RGB, brown switches","price":350.00,"stockQuantity":50}'

# create an inventory record for the product above (replace productId)
curl -X POST http://localhost:18082/api/inventory \
  -H "Content-Type: application/json" \
  -d '{"productId":"<product-uuid>","quantityAvailable":50,"warehouseLocation":"CD-SP-01"}'

# create an order
curl -X POST http://localhost:18083/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Marcos Perboni","items":[{"productId":"<product-uuid>","quantity":2}]}'
```

Every endpoint validates the request body (Bean Validation) and returns `400` with per-field details on invalid input, `404` for resources that don't exist, `409` for business-rule conflicts (e.g. insufficient stock) and `502` when a downstream service is unavailable.

## Running the tests

```bash
./mvnw test
```

Each module has unit tests (service layer, with Mockito) and web-slice tests (`@WebMvcTest` + MockMvc) covering the success, validation and error paths of every `GET`, `POST`, `PUT` and `DELETE` endpoint.

## Deploying to Kubernetes

### Option 1 — plain YAML manifests

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/redis/
kubectl apply -f k8s/product-service/
kubectl apply -f k8s/inventory-service/
kubectl apply -f k8s/order-service/
kubectl apply -f k8s/ingress.yaml
```

### Option 2 — Helm chart

```bash
helm install cloud-native-kubernetes helm/cloud-native-kubernetes \
  --namespace cloud-native-kubernetes --create-namespace
```

To upgrade (automatic rolling update via `strategy: RollingUpdate`):

```bash
helm upgrade cloud-native-kubernetes helm/cloud-native-kubernetes \
  --set image.tag=<new-tag>
```

Each Deployment defines a `readinessProbe`/`livenessProbe` via Spring Actuator (`/actuator/health/readiness` and `/actuator/health/liveness`), `resources.requests/limits`, and a `HorizontalPodAutoscaler` (70% CPU / 80% memory, 2 to 6 replicas).

> The `Secret` resources versioned in this repository (`k8s/**/secret.yaml`, `values.yaml`) use example credentials (`postgres`/`postgres`) for portfolio/demo purposes only. In a real environment, use a secret manager (Vault, AWS Secrets Manager, Sealed Secrets) and never commit real credentials.

## CI/CD

The `.github/workflows/ci-cd.yml` workflow runs on every `push`/`pull request` to `master`:

1. Builds and runs the unit tests for each microservice (job matrix).
2. On push to `master`, builds and publishes each service's image to the GitHub Container Registry (GHCR).

## Repository structure

```
cloud-native-kubernetes/
├── product-service/       # catalog microservice (Java/Spring Boot)
├── inventory-service/      # inventory microservice
├── order-service/          # order microservice
├── k8s/                     # plain Kubernetes manifests
├── helm/cloud-native-kubernetes/  # Helm chart
├── infra/postgres/          # multi-database init script
├── podman-compose.yml        # local orchestration
├── .github/workflows/         # CI/CD pipeline
└── pom.xml                     # parent POM (multi-module Maven)
```
