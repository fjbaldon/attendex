# The AttendEx Domain Model

This document provides the definitive conceptual model for the AttendEx domain. It is the primary reference for understanding our business processes, rules, and the boundaries between different parts of our system. It should be read alongside the `UBIQUITOUS_LANGUAGE.md` file, which defines the specific terms used herein.

## 1. Core Domain & Strategic Goal

The **core domain** of AttendEx is the **reliable and insightful tracking of event attendance**.

Our strategic goal is to provide a seamless system that empowers `Organizers` to manage complex event logistics effortlessly, while enabling `Scanners` to capture attendance data with speed and accuracy, even in challenging offline environments. The `Platform` achieves this by turning raw attendance data (`Entries`) into valuable `Analytics` and `Reports`.

All other parts of the system, such as `Steward` administration, are supportive subdomains that exist to serve this core mission.

## 2. Bounded Contexts

To manage complexity, the AttendEx `Platform` is divided into several **Bounded Contexts**. Each context has its own dedicated model, responsibilities, and consistency rules. They communicate with each other through well-defined public APIs (synchronous calls) and Domain Events (asynchronous notifications).

### Context Map

This map shows the relationships and information flow between the contexts.

  <!-- You can create a simple diagram for this -->

*   **Administration (Upstream):** Governs the lifecycle of `Organizations`. Managed by `Stewards`.
*   **Organization (Core Context):** The central hub where `Events` and `Rosters` are defined. Managed by `Organizers`.
*   **Capture (Downstream):** Consumes `Event` and `Roster` data from the Organization context. Its sole focus is capturing `Entries` on-site.
*   **Analytics & Reporting (Downstream):** A supporting context that listens for events from other contexts (primarily `Capture`) to build its own optimized read models.

---

## 3. Detailed Context Models

### 3.1. Administration Context

*   **Description:** This context is concerned with the management of the entire `Platform`. It is the world of the `Steward`.
*   **Core Responsibility:** Onboarding, managing, and offboarding `Organization` tenants.
*   **Key Concepts:** `Steward`, `Subscription`, `Lifecycle`, `Audit`.

*   **Aggregates:**
    *   **`Steward` (Aggregate Root):**
        *   **Description:** Represents a platform administrator with superuser privileges.
        *   **Business Rules (Invariants):**
            *   A `Steward`'s email must be globally unique across the `Platform`.
            *   The last `Steward` on the `Platform` cannot be deleted.

    *   **`Organization` (As viewed by Admin):**
        *   **Description:** From the `Steward`'s perspective, an `Organization` is a manageable entity with a `Subscription` and `Lifecycle`. The `Steward` does not interact with the `Organization`'s internal `Events` or `Attendees`.
        *   **Business Rules (Invariants):**
            *   An `Organization`'s `Lifecycle` must be one of the valid states: `ACTIVE`, `INACTIVE`, `SUSPENDED`.
            *   An `Organization`'s `Subscription` must be one of the valid types: `TRIAL`, `ANNUAL`, `LIFETIME`.
            *   A `Subscription` of type `TRIAL` or `ANNUAL` must have an expiration date. A `LIFETIME` subscription must not.

### 3.2. Organization Context

*   **Description:** This is the primary Bounded Context where `Organizers` create and manage all aspects of their events.
*   **Core Responsibility:** Defining *what* an event is, *who* can attend, and *how* attendance should be measured.
*   **Key Concepts:** `Organizer`, `Scanner`, `Event`, `Session`, `Roster`, `Attendee`, `Identity`, `Attribute`, `Grace`, `Intent`.

*   **Aggregates:**
    *   **`Organization` (Aggregate Root):**
        *   **Description:** The root entity for a single tenant. It acts as the consistency boundary for all people (`Organizers`, `Scanners`, `Attendees`) and definitions (`Attributes`) belonging to that tenant.
        *   **Business Rules (Invariants):**
            *   An `Organization`'s name must be unique across the `Platform`.
            *   An `Organizer`'s or `Scanner`'s email must be unique within their `Organization`.
            *   An `Attendee`'s `Identity` must be unique within their `Organization`. If a format pattern is defined, the `Identity` must match it.
            *   An `Attribute`'s name must be unique within the `Organization`.
            *   The last `Organizer` cannot be removed from an `Organization`.
            *   An `Attribute` cannot be deleted if it is in use by any `Attendee`.

    *   **`Event` (Aggregate Root):**
        *   **Description:** Represents a complete, schedulable event. It is the consistency boundary for its own schedule (`Sessions`) and participants (`Roster`).
        *   **Business Rules (Invariants):**
            *   An `Event`'s end date cannot be before its start date.
            *   A `Session`'s target time must fall within the `Event`'s start and end dates.
            *   The `Grace` period (before and after) cannot be a negative number of minutes.
            *   A `Session`'s `Intent` must be either `Arrival` or `Departure`.
            *   An `Attendee` can only be added to the `Roster` once per `Event`.
            *   An `Attendee` cannot be removed from the `Roster` if they already have an `Entry` recorded for the `Event` (to preserve historical data integrity).

### 3.3. Capture Context

*   **Description:** This context is exclusively concerned with the on-site experience of the `Scanner` using the mobile app. Its models are optimized for speed, simplicity, and offline-first operation.
*   **Core Responsibility:** To create `Entries` by performing a `Scan`.
*   **Key Concepts:** `Capture`, `Scan`, `Entry`, `Sync`, `Punctuality`.

*   **Aggregates:**
    *   **`Entry` (Aggregate Root):**
        *   **Description:** Represents the factual, immutable record of a single `Scan` at a specific moment in time. This is the primary transactional entity of this context.
        *   **Business Rules (Invariants):**
            *   An `Entry` must be associated with a valid `Session`, `Attendee`, and `Scanner`.
            *   The `Punctuality` of an `Entry` (`PUNCTUAL`, `EARLY`, `LATE`) is determined at the moment of creation based on the `Session`'s `Grace` period.
            *   An `Attendee` cannot have more than one `Entry` of the same `Intent` (e.g., two `Arrivals`) for the same `Session`.

### 3.4. Analytics & Reporting Context

*   **Description:** A supporting context that provides business intelligence. It is a read-optimized context that is populated by listening to Domain Events from other contexts.
*   **Core Responsibility:** To consume events and build fast, pre-aggregated `Overviews`, `Analytics`, and `Reports`.
*   **Key Concepts:** `Overview`, `Report`, `Analytics`.

*   **Aggregates & Read Models:**
    *   This context is composed primarily of **read models** (projections), not transactional aggregates. These are materialized views of the system's state, optimized for querying.
    *   **Example Read Models:**
        *   **`EventSummary`:** A model that stores pre-calculated stats for an `Event`, such as total registered vs. total checked-in, and the overall attendance rate. It is updated by listening to `RosterUpdatedEvent` and `EntryCreatedEvent`.
        *   **`AttributeBreakdown`:** A model that stores the count of `Entries` grouped by `Event`, `Attribute` name, and `Attribute` value. It is updated by `EntryCreatedEvent`.
        *   **`LiveActivityCounter`:** A simple model, possibly in-memory or in a fast key-value store, that tracks the number of `Entries` created in the last hour.

