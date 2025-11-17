# AttendEx Platform: Architecture Guide

## 1. Overview

This document defines the architecture of the AttendEx Platform. It is the **single source of truth** for the system's structure, dependencies, and communication patterns. Adherence to these principles is mandatory to maintain the long-term health and scalability of the codebase.

The platform is architected as a **Domain-Driven, Package-Private Modulith with Public Facades**.

### 1.1. Core Architectural Principles

1.  **Modulithic Architecture:** The platform is a single, deployable Spring Boot application, internally organized into distinct modules.

2.  **Domain-Driven Design (DDD):** Module boundaries correspond to the **Bounded Contexts** defined in the root `DOMAIN.md`. All code must strictly adhere to the project's `UBIQUITOUS_LANGUAGE.md`.

3.  **The Public Facade Pattern:** Each module exposes a **single `public class ...Facade`** as its only entry point for commands and queries. This Facade is the module's public API.

4.  **Explicit API Data Structures:** A module's public contract also includes `public` **DTOs** (in a `dto` sub-package) and `public` **Domain Events** (in an `events` sub-package).

5.  **Encapsulation by Default (Compiler-Enforced):** All other classes within a module are **`package-private`**. This includes controllers, domain models (Aggregates), repositories, and persistence entities. This makes it a **compile-time error** for one module to access the internal implementation of another.

## 2. Module Structure: The Anatomy of a Module

The package structure is the physical manifestation of our architecture.

```java
com.github.fjbaldon.attendex.platform
└── admin/                    // The 'admin' Module (Bounded Context)
    ├── dto/                  // PUBLIC: Data Transfer Objects
    │   └── public record StewardDto(...) {}
    │
    ├── events/               // PUBLIC: Domain Events
    │   └── public record StewardCreatedEvent(...) {}
    │
    ├── public class AdminFacade {}   // PUBLIC: The single entry point
    │
    ├── class Steward {}             // package-private Domain Aggregate
    ├── interface StewardRepository {} // package-private Repository Interface
    └── class AdminController {}     // package-private API Controller
```

## 3. Communication Patterns

### 3.1. Synchronous Communication (via Facades)

This pattern is used when a module requires an immediate response from another. This is achieved by injecting another module's `public` Facade.

**Example:** The `organization` module needs to check a `Steward`'s permissions.

```java
// In: ...platform.organization.OrganizationFacade
import com.github.fjbaldon.attendex.platform.admin.AdminFacade; // Legal: Injects a public Facade

public class OrganizationFacade {
    private final AdminFacade adminFacade; // Legal dependency

    // This would be a compile error, as Steward is package-private to `admin`.
    // private final StewardRepository stewardRepository; // ILLEGAL!

    public void someOrganizationAction() {
        boolean canPerform = adminFacade.hasPermission(...)
        // ...
    }
}
```

### 3.2. Asynchronous Communication (via Events)

This is the preferred method for cross-module collaboration, ensuring loose coupling. It is achieved using Spring's `ApplicationEventPublisher`.

**Example:** The `admin` module creates a `Steward` and the `analytics` module needs to be notified.

1.  **Define the Public Event (`admin` module):**
    A `public record` is created in the `events` sub-package.

    ```java
    // In: platform/admin/events/StewardCreatedEvent.java
    public record StewardCreatedEvent(Long stewardId, String email) {}
    ```

2.  **Publish the Event (`admin` module):**
    The `package-private` service logic (which might be called by the Facade) publishes the event.

    ```java
// In: ...platform.admin.AdminFacade
public class AdminFacade {
    private final ApplicationEventPublisher eventPublisher;
    // ...
    public void createSteward(...) {
        Steward newSteward = ...;
        stewardRepository.save(newSteward);
        eventPublisher.publishEvent(new StewardCreatedEvent(...))
    }
}
```

3.  **Listen for the Event (`analytics` module):**
    A `package-private` component in the `analytics` module listens for the public event.

    ```java
    // In: ...platform.analytics.AdminEventListener.java
    import com.github.fjbaldon.attendex.platform.admin.events.StewardCreatedEvent;

    @Component
    class AdminEventListener {
        @EventListener
        public void onStewardCreated(StewardCreatedEvent event) {
            // React to the event...
        }
    }
    ```

## 4. Database & Persistence Strategy

The database is a single schema, logically partitioned to reflect module boundaries.

1.  **Table Naming Convention:** All tables **must** be named using the singular formula: `module_entity` (e.g., `organization_event`, `capture_entry`).
2.  **No Cross-Module JOINs:** A query must never join a table from its own module to a table from another module.
3.  **Intra-Module Foreign Keys Only:** Database-level foreign keys may only exist between tables belonging to the same module.
4.  **Merged Domain/Persistence Model:** To reduce boilerplate, domain models are directly annotated as JPA `@Entity` classes. These entity classes are always `package-private`.

## 5. Architectural Enforcement

Architectural integrity is maintained through automated checks and discipline.

1.  **Java Access Modifiers (Compiler-Enforced):** By defaulting implementation classes to `package-private`, the Java compiler provides the primary line of defense against illegal dependencies.
2.  **ArchUnit (Automated Guardrails):** The `ArchitectureTest.java` suite runs on every build, automatically verifying key rules such as:
    *   Only Facades, DTOs, and Events are `public`.
    *   Controllers, Entities, and Repositories are `package-private`.
    *   Controllers do not directly access Repositories or Entities.
    *   Database tables follow the `module_entity` naming convention.