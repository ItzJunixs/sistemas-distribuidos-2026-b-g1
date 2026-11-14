# ADR-C3-01: Deploy OptiView microservices on Huawei Cloud CCE

## Status

Proposed

## Context

OptiView consists of multiple independent services responsible for authentication, customers, products, sales, workflow orchestration, and scheduled tasks.

The current system uses Docker Compose configurations for container execution. Although this structure supports service separation, it does not by itself establish automatic scaling, multi-zone availability, or automated recovery of failed application instances.

As discussed in Section 2 of the architecture design note, reliability and performance efficiency are important considerations for this architecture. Section 3 also identifies the products and sales APIs as candidates for horizontal scaling.

The system needs an infrastructure design that can support independent service deployment without changing existing business responsibilities.

## Decision

I propose using Huawei Cloud Container Engine (CCE) to deploy the OptiView microservices. Application replicas would be distributed across two availability zones, with Elastic Load Balance (ELB) managing incoming traffic. Each domain service would retain its own responsibilities and database, and the workflow would continue using Redis for saga state persistence. Horizontal Pod Autoscaler policies would be introduced for selected services, including the products and sales APIs. The migration would also require adapting the existing NGINX service discovery configuration and shared product-image storage.

## Alternatives considered

- ECS virtual machines with Docker Compose: This would require fewer initial changes, but configuring multi-zone availability, automatic scaling, and container recovery would involve more manual infrastructure management.
- A single consolidated application: A monolithic deployment could simplify infrastructure administration, but it would reduce independent deployment and scaling capabilities and weaken the existing separation between business domains.

## Consequences

- Positive: CCE could improve application availability, support independent scaling, and provide standardized container deployment and recovery mechanisms.
- Negative: The solution would introduce additional operational complexity, Kubernetes configuration requirements, cloud costs, and the need to manage networking, secrets, and persistent storage carefully.

The decision would require validation through deployment tests, monitoring, and failure simulations before being accepted for production.
