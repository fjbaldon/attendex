# AttendEx Platform: The Ubiquitous Language

## 1. Introduction

This document is the definitive, shared dictionary for the AttendEx domain. It is the single source of truth for all terminology used in the project. The language defined here **must** be used consistently in all team conversations, user stories, documentation, and, most importantly, in the code itself (class names, methods, variables, and database schemas).

This language is the foundation of our Domain-Driven Design (DDD) approach. Mastering it is essential for every member of the team.

## 2. Core Principles of Our Language

*   **One Word, One Meaning:** Each term has a single, unambiguous definition. If a term seems to mean two different things, it must be split into two distinct terms.
*   **Singular Naming:** All domain entities and the database tables that store them are named in the singular (e.g., `Event`, `TABLE organization_event`). This creates a direct mapping from concept to code to data.
*   **Past Tense for Events:** Domain Events, which announce that something has happened, are always named in the past tense (e.g., `EventCreated`, `EntrySynced`).
*   **A Living Document:** This document is not static. As our understanding of the domain deepens, we will refine this language together. All changes must be deliberate and agreed upon by the team.

## 3. Core Terminology

The language is organized by the Bounded Context where the term is most relevant.

### Shared Concepts (Cross-Context)

These terms are foundational and used across multiple contexts.

| Term | Replaces (Legacy) | Definition |
| :--- | :--- | :--- |
| **User** | | Any individual who can authenticate with the system. A generic concept for a `Steward`, `Organizer`, or `Scanner`. |
| **Principal**| | The specific, currently authenticated `User` for a given request or session. A security concept. |

### Platform & Administration Context

Terms related to the overall management of the AttendEx `Platform`, used primarily by a `Steward`.

| Term | Replaces (Legacy) | Definition |
| :--- | :--- | :--- |
| **Platform** | | The entire AttendEx system, comprising all `Organizations`. |
| **Steward** | `SystemAdmin` | A `User` who administers the `Platform`, managing `Organizations`, `Subscriptions`, and other `Stewards`. |
| **Lifecycle** | `OrganizationStatus` | The business state of an `Organization`'s account. Possible values are `ACTIVE`, `INACTIVE`, `SUSPENDED`. |
| **Subscription**| | The commercial plan an `Organization` is on. Possible values are `TRIAL`, `ANNUAL`, `LIFETIME`. |
| **Audit** | `AuditLog` | An immutable record of a significant action that occurred on the `Platform`. |

### Organization Context

The core language for `Organizers` managing their tenant account, events, and people.

| Term | Replaces (Legacy) | Definition                                                                                                                      |
| :--- | :--- |:--------------------------------------------------------------------------------------------------------------------------------|
| **Organization**| | The top-level customer account; the root for all tenant-specific data.                                                          |
| **Organizer** | | A `User` who manages an `Organization` via the `web` application.                                                               |
| **Scanner** | | A `User` who performs a `Scan` at an `Event` using the `Capture`. Managed within the `Organization`.                            |
| **Attendee** | | An individual who can be registered for an `Event`.                                                                             |
| **Identity** | `uniqueIdentifier` | A unique Value Object that identifies an `Attendee` within an `Organization`. This is not just a string; it's a domain concept. |
| **Attribute** | `CustomField`, `CustomFieldDefinition` | A defined, optional characteristic of an `Attendee` (e.g., "Department").                                                       |
| **Event** | | A scheduled occasion with a start and end date, which contains one or more `Sessions`.                                          |
| **Session** | `EventTimeSlot` | A specific, schedulable activity within an `Event` that has a target time for attendance (e.g., "Keynote").                     |
| **Roster** | `EventAttendees` | The definitive list of `Attendees` registered for a specific `Event`.                                                           |
| **Grace** | `OnTimeWindowMinutesBefore`, `onTimeWindowMinutesAfter` | The configurable time window (in minutes) around a `Session`'s target time that determines `Punctuality`.                       |
| **Intent** | `TimeSlotType`, `SessionType` | The business purpose of a `Session`, dictating the kind of `Entry` it generates. Possible values are `Arrival` or `Departure`.  |
| **Import** | | The use case for bulk-creating `Attendees` from a file.                                                                         |

### Capture Context

The language specific to the on-site `Scanner` experience via the mobile application.

| Term            | Replaces (Legacy) | Definition                                                                                                                    |
|:----------------| :--- |:------------------------------------------------------------------------------------------------------------------------------|
| **Capture**     | `scanner` (application name) | The on-site context of the mobile application used by a `Scanner`.                                                            |
| **Scan**        | | The **action** of reading an `Attendee`'s `Identity` to create an `Entry`. `Scan` is the verb.                                |
| **Entry**       | `AttendanceRecord` | The **result** of a `Scan`. A single, timestamped record of attendance. `Entry` is the noun.                                  |
| **Sync**        | | The action of transferring `Entries` from an offline `Capture` to the central `Platform`.                                     |
| **Arrival**     | `CHECK_IN`, `check-in` | An `Entry` that records an `Attendee` entering a `Session`. Fulfills the `Arrival` `Intent` of a `Session`.                   |
| **Departure**   | `CHECK_OUT`, `check-out` | An `Entry` that records an `Attendee` leaving a `Session`. Fulfills the `Departure` `Intent` of a `Session`.                  |
| **Punctuality** | `AttendanceStatus`, `Entry Status` | The classification of an `Entry` relative to its `Session`'s `Grace` period. Possible values are `PUNCTUAL`, `EARLY`, `LATE`. |

### Analytics & Reporting Context

Terms related to deriving insights from the collected data.

| Term | Replaces (Legacy) | Definition |
| :--- | :--- | :--- |
| **Report** | | A static, formal, and often exportable summary of data (e.g., a PDF list of all `Entries`). |
| **Analytics** | | The interactive exploration and visualization of domain data. |
| **Overview** | `Dashboard` | A high-level summary view of a specific context (e.g., an `Organizer`'s `Overview`, a `Steward`'s `Overview`). |
