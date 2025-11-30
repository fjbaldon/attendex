# AttendEx Domain Model & Ubiquitous Language

## 1. Core Domain
The **core domain** of AttendEx is the **reliable and insightful tracking of event attendance**.

Our strategic goal is to provide a seamless system where **Organizers** manage logistics and **Scanners** capture data (even offline), which the **Platform** transforms into **Analytics**.

## 2. Bounded Contexts & Terminology

### 2.1. Administration Context
*Governs the lifecycle of the Platform tenants.*

| Term | Definition |
| :--- | :--- |
| **Platform** | The entire AttendEx system, comprising all Organizations. |
| **Steward** | A superuser who administers the Platform, managing Organizations and Subscriptions. |
| **Lifecycle** | The state of an Organization's account (`ACTIVE`, `INACTIVE`, `SUSPENDED`). |
| **Subscription** | The commercial plan (`TRIAL`, `ANNUAL`, `LIFETIME`). |

**Key Rules:**
*   A `Steward`'s email must be globally unique.
*   An `Organization` cannot be deleted if it has active `Events`.

### 2.2. Organization Context (Core)
*The central hub where events and people are defined.*

| Term | Definition |
| :--- | :--- |
| **Organization** | The top-level tenant account. |
| **Organizer** | A user who manages an Organization via the web dashboard. |
| **Scanner** | A user/device account authorized to perform scans at an Event. |
| **Attendee** | An individual registered for an Event. |
| **Identity** | A unique value object (e.g., Student ID) identifying an Attendee. |
| **Event** | A scheduled occasion with a start/end date containing Sessions. |
| **Session** | A specific activity within an Event (e.g., "Keynote") with a target time. |
| **Roster** | The definitive list of Attendees registered for an Event. |
| **Grace** | The time window (minutes) around a Session that determines Punctuality. |
| **Intent** | The purpose of a Session: `Arrival` (Entry) or `Departure` (Exit). |

**Key Rules:**
*   An `Attendee`'s `Identity` must be unique within the Organization.
*   A `Session` must fall within the `Event` start/end dates.

### 2.3. Capture Context (Mobile/Downstream)
*Handles on-site data collection, optimized for offline use.*

| Term | Definition |
| :--- | :--- |
| **Capture** | The mobile application used by a Scanner. |
| **Scan** | The **action** of reading an Identity to create an Entry. |
| **Entry** | The **result** of a Scan. A timestamped record of attendance. |
| **Activity** | The chronological stream of all `Entries` (Arrivals, Departures, Unscheduled). |
| **Sync** | The process of transferring Entries from offline storage to the Platform. |
| **Punctuality** | The classification of an Entry: `PUNCTUAL`, `EARLY`, `LATE`, or `UNSCHEDULED`. |

**Key Rules:**
*   **Idempotency:** Every scan generates a unique `scanUuid` locally. The server uses this to prevent duplicate entries during Sync.
*   **Offline-First:** The local database is the source of truth for the UI; the network is for backup.

### 2.4. Analytics Context
*Derives insights from raw data.*

| Term | Definition |
| :--- | :--- |
| **Report** | A static, exportable summary (e.g., PDF Log). |
| **Analytics** | Interactive visualization of data. |
| **Overview** | A high-level summary view (Dashboard). |