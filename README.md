# Healix ERP — Backend Service

Production-grade, multi-tenant pharmaceutical supply chain ERP SaaS for India.

## Tech Stack
* **Java**: 21 (LTS)
* **Spring Boot**: 3.3.x
* **Spring Modulith**: 1.2.x
* **PostgreSQL**: 16 (RLS Partitioned)
* **Redis**: 7.x (Cache namespace per tenant)
* **Flyway**: 10.x (DB migrations)

## Directory Structure
* `healix-core`: Common classes, exceptions, and core tenant provider APIs.
* `healix-security`: JWT security filter and stateless auth configuration.
* `healix-persistence`: RLS routing DataSource and JPA database mappings.
* `healix-rules`: JSONB-based rule engine.
* `healix-workflows`: Transition state-machine mapping.
* `healix-notifications`: Notification adapters (SMS, WA, Email).
* `healix-modules`: Modulith domain features (Catalog, Inventory, Billing).
* `healix-app`: Bootstrap execution module and docker configs.

## Local Development
Run infrastructure databases locally:
```bash
docker compose up -d
```

Verify application builds and passes Modulith boundary verification tests:
```bash
mvn clean verify
```
