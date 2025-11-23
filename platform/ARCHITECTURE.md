# AttendEx System Architecture

## 1. System Overview

AttendEx is a comprehensive event attendance platform built on a **Modular Monolith** architecture. It solves the challenge of tracking attendance in environments with unreliable internet connectivity by utilizing an **Offline-First** mobile client and a **Hybrid Scanning Engine** (OCR + QR).

### High-Level Components

*   **Web Dashboard (Next.js):** For Organizers to manage events, rosters, and view analytics.
*   **Mobile Scanner (Android Native):** For Scanners to capture attendance using camera-based OCR or QR scanning.
*   **Platform API (Spring Boot):** The central nervous system handling business logic, data consistency, and synchronization.
*   **Database (PostgreSQL):** The persistent storage layer.

```mermaid
graph LR
    User[User / Organizer] -->|HTTPS| Web[Web Dashboard]
    Scanner[Scanner Person] -->|HTTPS / Offline| Mobile[Mobile App]
    
    Web -->|REST API| API[Platform API]
    Mobile -->|REST API| API
    
    API -->|JDBC| DB[(PostgreSQL)]
```

---

## 2. Backend Design: Spring Modulith

The backend is structured as a **Modular Monolith**. Unlike a traditional "Layered" architecture (Controller -> Service -> Repository), AttendEx is sliced by **Domain Business Function**.

This ensures that the "Attendance Capture" logic is decoupled from "Organization Management," making the system easier to test and maintain.

### Module Dependency Graph

```mermaid
graph TD
    %% Nodes
    subgraph "Administration Context"
        Admin[Admin Module]
    end

    subgraph "Organization Context (Core)"
        Org[Organization Module]
        Event[Event Module]
        Attendee[Attendee Module]
    end

    subgraph "Capture Context (Downstream)"
        Capture[Capture Module]
    end

    subgraph "Analysis Context (Downstream)"
        Analytics[Analytics Module]
        Report[Report Module]
    end

    subgraph "Shared Kernel"
        Identity[Identity Module]
        Notification[Notification Module]
    end

    %% Relationships (Facade Calls)
    Org -->|Uses| Identity
    Event -->|Uses| Org
    Event -->|Uses| Attendee
    Capture -->|Uses| Event
    Capture -->|Uses| Org
    Capture -->|Uses| Attendee
    Analytics -->|Uses| Event
    Analytics -->|Uses| Capture
    Report -->|Uses| Analytics
    Report -->|Uses| Event
    
    %% Event-Driven Relationships (Dotted)
    Org -.->|Publishes: OrgRegistered| Notification
    Capture -.->|Publishes: EntryCreated| Analytics
    Event -.->|Publishes: EventCreated| Notification
```

**Key Architectural Rules (Enforced by ArchUnit):**
1.  **No Cyclic Dependencies:** Modules cannot depend on each other circularly.
2.  **Event-Driven Integration:** The `Capture` module does not write directly to `Analytics`. Instead, it publishes an `EntryCreatedEvent`, which `Analytics` listens to.

---

## 3. Mobile Architecture: Offline-First Sync

The Android application utilizes **Room Database** as the single source of truth for the UI. Network operations effectively "back up" the local database to the server.

### Synchronization Flow (The "Idempotency" Logic)

To prevent duplicate scans when the internet flickers, every scan generates a unique `scanUuid` locally. The server uses this UUID to deduplicate entries.

```mermaid
sequenceDiagram
    participant App as Mobile App (Capture)
    participant Room as Local DB (Room)
    participant API as Platform API
    participant DB as Server DB (Postgres)

    Note over App, Room: Offline Phase
    App->>Room: Scan Attendee (Insert Entry + UUID)
    Room-->>App: Success (SyncStatus.PENDING)
    
    Note over App, API: Online Phase (Sync)
    loop Every 15s or Manual Trigger
        App->>Room: getPendingEntriesBatch(50)
        Room-->>App: List<EntryEntity>
        
        alt Has Pending Entries
            App->>API: POST /api/v1/capture/sync (Batch)
            activate API
            API->>DB: Check Idempotency (scanUuid)
            
            alt UUID Exists
                API-->>App: 200 OK (Ignored Duplicate)
            else New Record
                API->>DB: Insert Entry
                API-->>App: 200 OK (Saved)
            end
            deactivate API

            App->>Room: markAsSynced(uuids)
            Room-->>App: Update SyncStatus.SYNCED
        end
    end
```

---

## 4. Hybrid Scanning Engine

The scanning feature is designed to handle legacy ID cards (Text) and modern badges (QR). The system can also fallback to Manual Entry if the camera fails or the ID is damaged.

```mermaid
stateDiagram-v2
    [*] --> CameraPreview
    
    state "CameraPreview" as Cam {
        [*] --> OCR_Mode
        OCR_Mode --> QR_Mode : User Toggles
        QR_Mode --> OCR_Mode : User Toggles
        
        state "Text Recognition (ML Kit)" as OCR_Mode
        state "Barcode Scanning (ML Kit)" as QR_Mode
    }

    Cam --> Analyzer : Feeds ImageProxy
    Analyzer --> ViewModel : onTextFound(string)
    
    state "ViewModel Processing" as VM {
        ViewModel --> LocalDB : findAttendee(identity)
        
        state if_found <<choice>>
        LocalDB --> if_found
        
        if_found --> Success : Attendee Exists
        if_found --> NotFound : Attendee Missing
    }

    Success --> TTS : Speak Name
    Success --> UI : Show Overlay (Green)
    
    NotFound --> UI : Show Error (Red)
    
    state "Manual Entry Mode" as Manual
    Cam --> Manual : User Clicks Keyboard Icon
    Manual --> Success : User Selects Name from List
    Manual --> Cam : User Cancels
```

---

## 5. Security & Data Integrity

The system implements strict **Role-Based Access Control (RBAC)** and allows immediate revocation of access for compromised devices.

*   **Authentication:** Stateless JWT (JSON Web Tokens).
*   **Authorization:**
    *   `ROLE_ORGANIZER`: Full administrative access (Web Dashboard).
    *   `ROLE_SCANNER`: Limited write-access (Mobile App).
*   **Data Integrity:** All offline scans are tagged with a UUID (Idempotency Key). The server rejects duplicates if a scanner tries to sync the same record twice.

### Scanner Access Lifecycle

This diagram illustrates the "Kill Switch" feature. If a scanner device is lost or stolen, the Organizer can **Suspend** the account. This immediately forces the mobile app to stop syncing, protecting the database.

```mermaid
stateDiagram-v2
    [*] --> Active : Created by Organizer
    
    state "Active (Authorized)" as Active
    note right of Active
        Login Successful
        Sync Allowed (200 OK)
    end note
    
    state "Suspended (Unauthorized)" as Suspended
    note right of Suspended
        Login Fails
        Sync Blocked (401)
    end note
    
    Active --> Suspended : Organizer clicks 'Suspend'
    Suspended --> Active : Organizer clicks 'Activate'
    
    Active --> [*] : Deleted
    Suspended --> [*] : Deleted
```