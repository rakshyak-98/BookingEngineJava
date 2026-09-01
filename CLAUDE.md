# Booking Engine Backend — Working Agreement

## Design decisions

**Before adding a class, interface, or abstraction, consult [docs/design-patterns.md](docs/design-patterns.md).**

It contains the binding decision procedure for low-level design in this repo, derived from *Dive Into Design Patterns* (Shvets, Refactoring.Guru). Specifically:

- **§1** — the five-gate decision procedure. Apply in order; stop at the first gate that resolves the question.
- **§4** — ratified pattern decisions (State for booking lifecycle, Strategy for pricing, Chain of Responsibility for validation, Observer via Spring events, Facade for payments, Adapter for suppliers).
- **§5** — patterns **explicitly rejected** because Spring already implements them. Do not hand-roll Singleton, Proxy, Factory, or Decorator-for-cross-cutting-concerns.
- **§6** — known defects in the current scaffold, ordered by severity.
- **§7** — PR review checklist.

Two rules carry most of the weight:

1. **Gate 0 — does the framework already do it?** Spring beans are singletons; `@Cacheable`/`@Transactional` are proxies; the container is a factory; `ApplicationEventPublisher` is Observer. Using the GoF version instead is a defect, not a design choice.
2. **Gate 1 — is anything actually varying?** An interface with one implementation and no second one planned is ceremony. Write the direct code and revisit when the second case is real.

## Stack

Spring Boot 3.2 · Java 17 · Maven · MySQL/PostgreSQL (JPA) · MongoDB · Redis · AWS SDK v2 · JUnit 5 + Mockito · Docker/K8s (EKS)

## Conventions

- Entities in `entity/`, request/response models in `dto/`, never expose entities from controllers.
- Services are interface + `impl/` pair. Constructor injection via Lombok `@RequiredArgsConstructor` — no field injection.
- Repositories split by store: `repository/sql/` (JPA) and `repository/mongodb/`.
- All exceptions surface through `GlobalExceptionHandler`; add a typed exception rather than throwing raw `RuntimeException`.
- Domain events publish **IDs, not JPA entities**, and listen with `@TransactionalEventListener(phase = AFTER_COMMIT)`.
- DTO↔entity mapping belongs in MapStruct mappers, not hand-written methods in services.

## Build

```bash
mvn clean package          # build + test
mvn test                   # tests only
docker-compose up -d       # MySQL, MongoDB, Postgres, Redis
```

Active profiles: `dev` (default), `test` (H2), `prod`.
