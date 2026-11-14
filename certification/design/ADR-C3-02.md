# ADR-C3-02: Protect sales data with multi-AZ PostgreSQL

## Status

Proposed

## Context

The OptiView sales domain manages work orders, invoices, and payment information. Its database is an important part of the platform because transaction records must remain consistent and recoverable.

A database failure could interrupt sales operations and affect access to financial records.

As explained in Section 4 of the architecture design note, the proposed disaster recovery objectives are an RTO of 30 minutes and an RPO of 5 minutes.

A recovery strategy is necessary to reduce the impact of infrastructure failures while maintaining the current database-per-service architecture.

## Decision

I propose migrating `opti-sales-db` to Huawei Cloud RDS for PostgreSQL with a primary-standby configuration distributed across two availability zones. The primary instance would process normal transactions, while the standby instance would provide a recovery target during infrastructure failures. The solution would include backup policies, replication monitoring, recovery procedures, and periodic failover tests. The proposed RTO and RPO would be treated as operational objectives that must be validated through recovery testing. The sales database would remain independent from the authentication, customers, and products databases.

## Alternatives considered

- PostgreSQL on a single ECS instance: This option could reduce initial infrastructure costs, but the absence of a standby instance would increase the risk of prolonged downtime following a server failure.
- Backups without a standby database: Periodic backups provide an important recovery mechanism, but they may require longer restoration times and could lose transactions created after the latest available backup.

## Consequences

- Positive: A managed primary-standby configuration could improve availability, simplify some database administration tasks, and provide better recovery options for critical sales information.
- Negative: The architecture would increase infrastructure costs and require replication monitoring, backup testing, and careful management of database changes.

The decision would not remove the need to coordinate database recovery with Redis saga state and inventory reservations maintained by other services.
