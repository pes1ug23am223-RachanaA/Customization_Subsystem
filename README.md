# HRMS Customization Subsystem
### Team: Code Crafters | Section D | PES University

---

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Team & Ownership](#2-team--ownership)
3. [Project Structure](#3-project-structure)
4. [Module Overview](#4-module-overview)
5. [Architecture](#5-architecture)
6. [Design Patterns](#6-design-patterns)
7. [SOLID Principles](#7-solid-principles)
8. [GRASP Principles](#8-grasp-principles)
9. [Database Integration](#9-database-integration)
10. [Cross-Subsystem Integration](#10-cross-subsystem-integration)
11. [Module Deep Dives](#11-module-deep-dives)
12. [File Reference](#12-file-reference)
13. [How to Run](#13-how-to-run)
14. [API Reference](#14-api-reference)
15. [Contributors](#15-Contributors)

---

## 1. Project Overview

The **HRMS Customization Subsystem** is one of several subsystems in a larger Human Resource Management System (HRMS) built as part of the OOAD course project. It allows HR administrators to configure and extend the HRMS without touching source code — defining custom forms, approval workflows, navigation flows, lookup values, module settings, and reports.

The subsystem contains **8 modules** spread across 4 functional areas, all served from a single Java backend (`ApiServer.java`) on **port 8080**, sharing a single **SQLite database (`hrms.db`)**.

**Technology Stack:**
- Backend: Plain Java (no framework), SQLite via JDBC
- Frontend: HTML + CSS + Vanilla JavaScript (fetch API)
- Database: SQLite (`hrms.db`) shared across all subsystems
- Server: `com.sun.net.httpserver.HttpServer` (built-in Java)

---

## 2. Team & Ownership

| Module | Owner |
|--------|-------|
| Workflow Engine | Code Crafters |
| Task Flow Builder | Code Crafters |
| Form Designer | Code Crafters |
| Flexfield Manager | Code Crafters |
| EIT Handler | Code Crafters |
| Lookup Customizer | Code Crafters |
| Module Customizer | Code Crafters |
| Report Builder | Code Crafters |
| Employee Onboarding / Offboarding | External Team |
| Performance Management | External Team (PM Team) |
| Database Layer | DB Team (`HRMSDatabaseFacade`) |

---

## 3. Project Structure

```
hrms_v2/
│
├── hrms.db                          ← Shared SQLite database (all subsystems)
│
├── workflow_taskflow/
│   ├── backend/
│   │   ├── ApiServer.java           ← Single entry point, all 8 module handlers
│   │   │
│   │   ├── ── Workflow Engine ──
│   │   ├── IWorkflowRepository.java
│   │   ├── DbWorkflowRepository.java
│   │   ├── WorkflowService.java
│   │   ├── WorkflowServiceImpl.java
│   │   ├── WorkflowDTO.java
│   │   ├── WorkflowStepDTO.java
│   │   ├── WorkflowObserver.java
│   │   ├── ConsoleNotificationObserver.java
│   │   ├── WorkflowException.java
│   │   │
│   │   ├── ── Task Flow Builder ──
│   │   ├── ITaskFlowRepository.java
│   │   ├── DbTaskFlowRepository.java
│   │   ├── TaskFlowService.java
│   │   ├── TaskFlowServiceImpl.java
│   │   ├── TaskFlowDTO.java
│   │   ├── TaskFlowException.java
│   │   │
│   │   ├── ── Shared Infrastructure ──
│   │   ├── CustomizationServiceFactory.java  ← Singleton Factory
│   │   ├── CustomizationFacade.java          ← Facade for external subsystems
│   │   │
│   │   ├── ── Cross-Subsystem Integration ──
│   │   ├── IEmployeeIntegration.java
│   │   ├── IEmployeeDataProvider.java
│   │   ├── RealEmployeeIntegration.java      ← Adapter (Onboarding team)
│   │   ├── RealEmployeeDataProvider.java     ← Adapter (Onboarding team)
│   │   ├── IPerformanceForCustomization.java
│   │   ├── RealPerformanceIntegration.java   ← Adapter (PM team)
│   │   │
│   │   ├── ── Other 6 Modules (flat in this folder) ──
│   │   ├── DbUnifiedFormRepository.java
│   │   ├── DbFlexfieldRepository.java
│   │   ├── DbEITRepository.java
│   │   ├── DbLookupRepository.java
│   │   ├── DbModuleRepository.java
│   │   ├── DbReportRepository.java
│   │   │
│   │   └── sqlite-jdbc.jar, slf4j-api.jar, slf4j-simple.jar
│   │
│   └── frontend/
│       ├── workflow.html
│       ├── taskflow.html
│       └── nav.js
│
├── form_flexfield/
│   ├── backend/
│   │   ├── controller/CustomizationController.java
│   │   ├── dto/          FormDTO, FieldDTO, FlexFieldDTO
│   │   ├── exception/    FormException, FlexfieldException
│   │   ├── facade/       CustomizationFacade.java
│   │   ├── factory/      RepositoryFactory.java
│   │   ├── observer/     Observer, Subject, PreviewObserver
│   │   ├── repository/   IFormRepository, DbFormRepository, MockFormRepository
│   │   │                 IFlexfieldRepository, DbFlexfieldRepository, MockFlexfieldRepository
│   │   └── service/      FormService, FormServiceImpl
│   │                     FlexfieldService, FlexfieldServiceImpl
│   └── frontend/
│       ├── forms.html, flexfields.html, forms.js, style.css, nav.js
│
├── eit_lookup/
│   ├── backend/
│   │   ├── dto/          EITDTO, LookupDTO
│   │   ├── repository/   IEITRepository, DbEITRepository
│   │   │                 ILookupRepository, DbLookupRepository
│   │   └── service/      EITService, EITServiceImpl
│   │                     LookupService, LookupServiceImpl
│   └── frontend/
│       ├── eit.html, lookup.html, nav.js
│
├── module_report/
│   ├── backend/
│   │   ├── controller/   ModuleController, ReportController, ApiResponse
│   │   ├── exception/    CustomizationException, ExceptionFactory, Exceptions
│   │   ├── model/        Module, Report
│   │   ├── repository/   IModuleRepository, DbModuleRepository
│   │   │                 IReportRepository, DbReportRepository
│   │   └── service/      ModuleCustomizer, ReportBuilder
│   └── frontend/
│       ├── module_customizer.html, report_builder.html, nav.js
│
└── integration/
    ├── backend/
    │   ├── IPerformanceForCustomization.java
    │   └── IPerformanceSubsystem.java
    └── frontend/
        ├── customization.html
        ├── employee_onboarding.html
        ├── performance_management.html
        ├── workflow.html
        ├── eit.html
        └── nav.js
```

---

## 4. Module Overview

### 4.1 Workflow Engine
Lets HR admins define named approval chains (e.g. "Leave Approval", "Payroll Processing"). Each workflow has sequential steps, where every step has an assignee and an escalation timer (hours before escalation). Workflows can be activated, deactivated, and deleted. Other subsystems (Leave, Payroll, Recruitment) trigger these workflows via the `CustomizationFacade`.

**Tables:** `workflows`, `workflow_steps`

### 4.2 Task Flow Builder
Lets HR admins define multi-step UI navigation sequences. A "Task Flow" is a named ordered list of screens (windows) — e.g. "New Employee Onboarding" → [Personal Info, Document Upload, Role Assignment, Benefits]. Other subsystems read these flows to know which screens to display and in what order, without hardcoding screen sequences.

**Tables:** `task_flows`, `task_flow_windows`

### 4.3 Form Designer
Lets HR admins create custom forms with fields (text, dropdown, date, checkbox). Each form has a name, status, and a list of typed fields. The Employee Onboarding and Performance teams use these forms during their processes.

**Tables:** `custom_forms`, `custom_fields`

### 4.4 Flexfield Manager
Manages reusable field segments (Key Flexfields and Descriptive Flexfields) that can be attached to forms across the HRMS. Admins define segments (e.g. "Department Code", "Cost Centre") which are then linked to forms via the Form Designer.

**Tables:** `custom_fields` (with `form_id = NULL` for unattached flexfields)

### 4.5 EIT Handler
Manages Extra Information Types — custom data categories that extend employee records. Each EIT belongs to a context (EMPLOYEE, JOB, or POSITION) and can be injected into any form. Used to attach domain-specific extra information to employee profiles.

**Tables:** `entity_interaction_types`

### 4.6 Lookup Customizer
Manages reusable dropdown value sets used across the HRMS. Admins define lookup codes and their valid values (e.g. "EMPLOYMENT_TYPE" → [Full-Time, Part-Time, Contract]). Includes a live employee data search to extract values directly from real employee data into a lookup.

**Tables:** `lookup_values`

### 4.7 Module Customizer
Controls the visibility, access, and configuration of all HRMS modules. Admins can enable/disable modules, set display order, assign role-based access, configure sidebar visibility, and view a session-level change audit trail.

**Tables:** `module_customizations`

### 4.8 Report Builder
Generates HR reports from 8 data sources (employees, performance, appraisals, workflows, forms, fields, EITs, lookups). Supports column selection, row filtering, and CSV/PDF export.

**Tables:** `hr_reports`

---

## 5. Architecture

The subsystem follows a **layered architecture**:

```
┌─────────────────────────────────────────────────┐
│                  Frontend (HTML/JS)              │  ← fetch() calls to localhost:8080
├─────────────────────────────────────────────────┤
│         ApiServer.java — HTTP Handlers           │  ← Routes, parses JSON, delegates
├───────────────────┬─────────────────────────────┤
│  WorkflowService  │    TaskFlowService           │  ← Business logic, validation
│  (interface)      │    (interface)               │
├───────────────────┴─────────────────────────────┤
│         CustomizationServiceFactory              │  ← Singleton — wires everything
├───────────────────┬─────────────────────────────┤
│ IWorkflowRepo     │    ITaskFlowRepo             │  ← Interfaces (DIP)
│ DbWorkflowRepo    │    DbTaskFlowRepo            │  ← Adapters (JDBC ↔ Domain)
├───────────────────┴─────────────────────────────┤
│              hrms.db (SQLite)                    │  ← Shared database
└─────────────────────────────────────────────────┘

External subsystems access via:
CustomizationFacade.getInstance().getAllWorkflows()
CustomizationFacade.getInstance().getTaskFlowWindows(id)
```

All 8 modules are served from **one `ApiServer.java`** on port 8080. Each module has its own inner `Handler` class registered to a URL prefix (e.g. `/api/workflows/*`, `/api/taskflows/*`).

---

## 6. Design Patterns

### 6.1 Singleton — `CustomizationServiceFactory`, `CustomizationFacade`

**What:** Only one instance exists for the lifetime of the application. Enforced with a `private` constructor, a `private static` instance field, and a `synchronized getInstance()` method.

**Where:**
```java
// CustomizationServiceFactory.java
private static CustomizationServiceFactory instance;

public static synchronized CustomizationServiceFactory getInstance() {
    if (instance == null) {
        instance = new CustomizationServiceFactory();
    }
    return instance;
}
```

**Why this over alternatives:**
- A static class can't be swapped or mocked for testing
- Plain `new Service()` scattered across the codebase = multiple DB connections, no central wiring
- Singleton gives one controlled place to swap implementations (e.g., `MockWorkflowRepository` for testing) without changing any other class

The `synchronized` keyword makes it thread-safe — two threads calling `getInstance()` simultaneously won't create two instances.

---

### 6.2 Adapter — `DbWorkflowRepository`, `DbTaskFlowRepository`, `RealEmployeeIntegration`, `RealPerformanceIntegration`

**What:** Adapts an incompatible interface into one that the rest of the application expects. The two incompatible interfaces are:

- **Target** — `IWorkflowRepository`: clean Java domain methods (`getWorkflowById(int id)`, `saveWorkflow(WorkflowDTO dto)`)
- **Adaptee** — JDBC/SQLite: raw `PreparedStatement`, `ResultSet`, `DriverManager.getConnection()`

**Where:**
```java
// DbWorkflowRepository.java — mapWf() is the core translation
private WorkflowDTO mapWf(ResultSet rs) throws SQLException {
    WorkflowDTO w = new WorkflowDTO();
    w.workflowId    = rs.getInt("workflow_id");      // JDBC → Java
    w.workflowName  = rs.getString("workflow_name");
    w.currentStatus = rs.getString("current_status");
    w.assignedTo    = rs.getString("assigned_to");
    return w;
}

// DbTaskFlowRepository.java — boolean ↔ SQLite integer translation
ps.setInt(1, validateOnNext ? 1 : 0);   // Java boolean → SQLite integer
t.validateOnNext = rs.getInt("validate_on_next") == 1;  // SQLite integer → Java boolean
```

For cross-subsystem integration, `RealEmployeeIntegration` adapts the Employee Onboarding team's `DbEmployeeRepository` API to the `IEmployeeIntegration` interface that our Workflow Engine expects. Same pattern for `RealPerformanceIntegration` ↔ PM team.

**Why:** Without the Adapter, every service class would contain `PreparedStatement` and `ResultSet` code — violating SRP and making database changes ripple across the entire codebase. Changing from SQLite to PostgreSQL requires changing only the `Db*Repository` classes.

---

### 6.3 Facade — `CustomizationFacade`

**What:** A single simplified entry point hiding the complexity of all services, repositories, and the database from external subsystems.

**Where:**
```java
// External subsystem usage — they see ONLY this
CustomizationFacade facade = CustomizationFacade.getInstance();
List<WorkflowDTO> workflows = facade.getAllWorkflows();
facade.triggerWorkflowStatus(3, "Active");
List<String> screens = facade.getTaskFlowWindows(5);
```

**Why:** Without the Facade, every external subsystem (Leave, Payroll, Onboarding, UI team) would need to know about `WorkflowServiceImpl`, `CustomizationServiceFactory`, and `IWorkflowRepository`. Any internal refactor would break all external subsystems. The Facade is the stable public contract.

---

### 6.4 Observer — `WorkflowObserver`, `ConsoleNotificationObserver`

**What:** `WorkflowServiceImpl` maintains a list of observers. When workflow status changes, all registered observers are notified automatically.

**Where:**
```java
// WorkflowServiceImpl.java
private final List<WorkflowObserver> observers = new ArrayList<>();

public void registerObserver(WorkflowObserver o)  { observers.add(o); }
public void removeObserver(WorkflowObserver o)    { observers.remove(o); }

private void notifyObservers(int id, String name, String oldStatus, String newStatus) {
    for (WorkflowObserver o : observers) {
        o.onWorkflowStatusChanged(id, name, oldStatus, newStatus);
    }
}
```

**Why this over hardcoding:** To add email notifications, you implement `WorkflowObserver` and call `registerObserver(new EmailNotificationObserver())` in the factory — `WorkflowServiceImpl` is never touched. This is Open/Closed Principle in practice.

**Why only in Workflow, not TaskFlow:** TaskFlow status changes don't trigger downstream notifications — the use case doesn't require it. Adding Observer where it isn't needed would be over-engineering.

---

### 6.5 Data Transfer Object (DTO) — `WorkflowDTO`, `WorkflowStepDTO`, `TaskFlowDTO`

**What:** Plain data carrier classes with no methods, no logic — just public fields. They transfer data between layers without coupling layers to each other's internals.

**Why:** Without DTOs, service classes would either pass raw `ResultSet` objects (leaking JDBC) or expose internal model classes (coupling the API contract to the DB schema). DTOs provide a neutral language all layers can speak.

---

## 7. SOLID Principles

### Single Responsibility Principle (SRP)
Each class has exactly one reason to change:
- `WorkflowServiceImpl` — only workflow business logic
- `DbWorkflowRepository` — only SQLite access for workflows
- `WorkflowDTO` — only carries workflow data
- `ConsoleNotificationObserver` — only prints to console
- `WorkflowException` — only carries error metadata

### Open/Closed Principle (OCP)
- Adding a new observer (e.g. email) → implement `WorkflowObserver`, register in factory. `WorkflowServiceImpl` is closed for modification.
- Adding a new repository backend → implement `IWorkflowRepository`. Service layer never changes.

### Liskov Substitution Principle (LSP)
`DbWorkflowRepository` and any mock implementation are fully substitutable through `IWorkflowRepository`. `WorkflowServiceImpl` works identically regardless of which concrete class it receives.

### Interface Segregation Principle (ISP)
- `WorkflowService` and `TaskFlowService` are separate interfaces — a class needing only task flows doesn't depend on workflow methods
- `WorkflowObserver` is a single-method interface — observers implement only what they need

### Dependency Inversion Principle (DIP)
High-level modules depend on abstractions, not concretions:
```java
// WorkflowServiceImpl depends on the interface, not DbWorkflowRepository
private final IWorkflowRepository repo;  // ← abstraction

// CustomizationServiceFactory does the concrete wiring
IWorkflowRepository workflowRepo = new DbWorkflowRepository("jdbc:sqlite:hrms.db");
WorkflowService ws = new WorkflowServiceImpl(workflowRepo);
```

**Known violation:** `TaskFlowServiceImpl.setStatus()` performs an `instanceof` downcast to `DbTaskFlowRepository` because `setStatus()` was not included in `ITaskFlowRepository` at design time. This is acknowledged design debt — the fix is adding `setStatus()` to the interface.

---

## 8. GRASP Principles

### Information Expert
The class that has the data owns the rules. `WorkflowServiceImpl` knows all workflow data (via `repo`) so it owns all workflow validation: empty name check, duplicate name check, existence check before update, state transition validation.

### Controller (GRASP)
`WorkflowService` and `TaskFlowService` are GRASP Controllers — the first objects beyond the UI layer that handle system events. `ApiServer` handlers delegate immediately without containing any business logic.

### Low Coupling
Services depend on interfaces, not concrete classes. `WorkflowServiceImpl` imports zero JDBC classes — it physically cannot be coupled to the database implementation.

### High Cohesion
Every class has a focused, related set of responsibilities. No class handles unrelated concerns.

### Polymorphism
`IWorkflowRepository` allows runtime substitution of `DbWorkflowRepository` with `MockWorkflowRepository`. All callers are unaffected by which implementation is active.

### Pure Fabrication
`CustomizationServiceFactory` has no counterpart in the HR domain — it's a made-up class that exists purely to give the design good structure (centralised wiring, single creation point). This is Pure Fabrication.

### Indirection
`hrms.db` acts as an indirection point between all 8 subsystems. The Workflow Engine doesn't call the PM team's classes directly — both read from and write to the shared database. `CustomizationFacade` is another indirection layer between external subsystems and internal services.

---

## 9. Database Integration

### Shared Database
All subsystems read/write the same `hrms.db` SQLite file. The absolute path is resolved at startup in `ApiServer.main()`.

### Schema Auto-Creation
Every `Db*Repository` class calls `ensureSchema()` in its constructor. Tables are created with `CREATE TABLE IF NOT EXISTS`, so the first run sets up the schema and subsequent runs are safe.

### How Data Is Saved — Workflow Example

**Step 1:** Frontend sends `POST /api/workflows/create` with JSON body.

**Step 2:** `WorkflowHandler` in `ApiServer.java` parses the body and calls:
```java
int id = workflowService.createWorkflow(name, assignedTo);
```

**Step 3:** `WorkflowServiceImpl.createWorkflow()` validates (non-empty, no duplicate), builds a `WorkflowDTO`, and calls:
```java
int id = repo.saveWorkflow(dto);
```

**Step 4:** `DbWorkflowRepository.saveWorkflow()` runs:
```sql
INSERT INTO workflows(workflow_name, current_status, assigned_to)
VALUES(?, ?, ?)
```
Returns the auto-incremented `workflow_id` via `RETURN_GENERATED_KEYS`.

**Step 5:** ID propagates back through all layers to the frontend as `{"workflowId": N}`.

### SQLite Tables

```sql
-- Workflow Engine
CREATE TABLE workflows (
    workflow_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    workflow_name  TEXT NOT NULL UNIQUE,
    current_status TEXT DEFAULT 'Active',
    assigned_to    TEXT DEFAULT ''
);

CREATE TABLE workflow_steps (
    step_id          INTEGER PRIMARY KEY AUTOINCREMENT,
    workflow_id      INTEGER NOT NULL,
    step_name        TEXT NOT NULL,
    assignee         TEXT DEFAULT '',
    escalation_hours INTEGER DEFAULT 0,
    FOREIGN KEY(workflow_id) REFERENCES workflows(workflow_id)
);

-- Task Flow Builder
CREATE TABLE task_flows (
    task_id          INTEGER PRIMARY KEY AUTOINCREMENT,
    flow_name        TEXT NOT NULL,
    flow_status      TEXT NOT NULL DEFAULT 'Active',
    linked_menu      TEXT DEFAULT '',
    validate_on_next INTEGER DEFAULT 0,
    allow_back_nav   INTEGER DEFAULT 0,
    created_date     DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE task_flow_windows (
    window_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id     INTEGER NOT NULL,
    window_name TEXT NOT NULL,
    FOREIGN KEY(task_id) REFERENCES task_flows(task_id)
);
```

---

## 10. Cross-Subsystem Integration

### How other subsystems consume our modules
Via `CustomizationFacade` — the only public API:
```java
CustomizationFacade.getInstance().getAllWorkflows()
CustomizationFacade.getInstance().triggerWorkflowStatus(id, status)
CustomizationFacade.getInstance().getTaskFlowWindows(taskId)
```

### How our modules consume other subsystems
Via Adapter interfaces:

| Interface | Adapter | Adaptee | Purpose |
|-----------|---------|---------|---------|
| `IEmployeeIntegration` | `RealEmployeeIntegration` | `DbEmployeeRepository` | Validate assignees in workflows |
| `IEmployeeDataProvider` | `RealEmployeeDataProvider` | `DbEmployeeRepository` | Read employee data for display |
| `IPerformanceForCustomization` | `RealPerformanceIntegration` | PM team's DB classes | Read PM data for reports and integration views |

### Shared Database Integration
The PM team and Employee Onboarding team read from `workflows` and `task_flows` tables directly via `hrms.db` — no API call needed. Data written by our modules is immediately available to all subsystems.

---

## 11. Module Deep Dives

### Workflow Engine — End to End

**Use case:** HR admin creates "Leave Approval" workflow with 3 steps.

1. Admin opens `workflow.html`, enters name "Leave Approval", clicks Create
2. `POST /api/workflows/create` → `WorkflowHandler` → `WorkflowServiceImpl.createWorkflow()`
3. Validation: name non-empty ✓, no duplicate ✓
4. `DbWorkflowRepository.saveWorkflow()` → `INSERT INTO workflows` → returns `workflow_id = 3`
5. Admin adds Step 1: "Manager Review", assignee "manager@co.com", escalation 24h
6. `POST /api/workflows/3/steps` → `addStep(3, "Manager Review", "manager@co.com", 24)`
7. `INSERT INTO workflow_steps(workflow_id, step_name, assignee, escalation_hours) VALUES(3, ...)`
8. Admin activates workflow → `updateWorkflowStatus(3, "Active")`
9. Observer fires: `ConsoleNotificationObserver.onWorkflowStatusChanged(3, "Leave Approval", "Inactive", "Active")`
10. Leave subsystem reads: `CustomizationFacade.getInstance().getWorkflowSteps(3)` → executes approval chain

### Task Flow Builder — End to End

**Use case:** HR admin creates "New Employee Onboarding" flow with 3 screens.

1. Admin opens `taskflow.html`, enters "New Employee Onboarding", sets validateOnNext=true, allowBackNav=false
2. `POST /api/taskflows/create` → `TaskFlowServiceImpl.createTaskFlow()`
3. `DbTaskFlowRepository.defineTaskFlow()` → `INSERT INTO task_flows` → returns `task_id = 5`
4. `assignFlowToMenu(5, "HR Admin")` → `UPDATE task_flows SET linked_menu='HR Admin'`
5. `updateFlowSettings(5, true, false)` → `UPDATE task_flows SET validate_on_next=1, allow_back_nav=0`
6. Admin adds windows: "Personal Info", "Document Upload", "Role Assignment"
7. Each: `INSERT INTO task_flow_windows(task_id, window_name)`
8. Employee Onboarding team reads: `CustomizationFacade.getInstance().getTaskFlowWindows(5)`
9. Returns: `["Personal Info", "Document Upload", "Role Assignment"]`
10. Onboarding UI renders each screen in sequence — no hardcoding

---

## 12. File Reference

### Workflow Engine

| File | Pattern/Role | What It Does |
|------|-------------|--------------|
| `IWorkflowRepository.java` | Interface (DIP) | Defines all DB operations: get, save, update, delete, steps |
| `DbWorkflowRepository.java` | Adapter | Translates JDBC/SQLite to `IWorkflowRepository` contract |
| `WorkflowService.java` | Interface (DIP) | Higher-level service contract: CRUD + observer management |
| `WorkflowServiceImpl.java` | Information Expert, Observer Subject | Business logic, validation, observer notification |
| `WorkflowDTO.java` | DTO | Data carrier: `workflowId`, `workflowName`, `currentStatus`, `assignedTo` |
| `WorkflowStepDTO.java` | DTO | Data carrier: `stepId`, `workflowId`, `stepName`, `assignee`, `escalationHours` |
| `WorkflowObserver.java` | Observer interface | `onWorkflowStatusChanged(id, name, oldStatus, newStatus)` |
| `ConsoleNotificationObserver.java` | Concrete Observer | Prints status change to console — swappable for email/audit |
| `WorkflowException.java` | Exception | Typed error with code (e.g. `WORKFLOW_NOT_FOUND`) |

### Task Flow Builder

| File | Pattern/Role | What It Does |
|------|-------------|--------------|
| `ITaskFlowRepository.java` | Interface (DIP) | All DB operations: define, get, update, delete, windows, menu, settings |
| `DbTaskFlowRepository.java` | Adapter | Translates JDBC/SQLite + boolean↔integer to `ITaskFlowRepository` |
| `TaskFlowService.java` | Interface (DIP) | Service contract: CRUD, status, windows, menu, settings |
| `TaskFlowServiceImpl.java` | Information Expert | Business logic, validation, delegates to repo |
| `TaskFlowDTO.java` | DTO | Data carrier: `taskId`, `flowName`, `flowStatus`, `linkedMenu`, `validateOnNext`, `allowBackNav` |
| `TaskFlowException.java` | Exception | Typed error with code (e.g. `TASKFLOW_NOT_FOUND`) |

### Shared Infrastructure

| File | Pattern/Role | What It Does |
|------|-------------|--------------|
| `CustomizationServiceFactory.java` | Singleton + Factory | Creates and wires all services to their repositories. One instance per JVM. |
| `CustomizationFacade.java` | Facade + Singleton | Public API for all external subsystems. Hides all internal complexity. |
| `ApiServer.java` | Controller | HTTP server. One inner `Handler` class per module. Routes all 8 modules. |

### Cross-Subsystem Integration

| File | Pattern/Role | What It Does |
|------|-------------|--------------|
| `IEmployeeIntegration.java` | Target interface | What our code expects from the Onboarding team |
| `RealEmployeeIntegration.java` | Adapter | Translates `DbEmployeeRepository` API to `IEmployeeIntegration` |
| `IEmployeeDataProvider.java` | Target interface | Read-only employee data contract |
| `RealEmployeeDataProvider.java` | Adapter | Translates `DbEmployeeRepository` API to `IEmployeeDataProvider` |
| `IPerformanceForCustomization.java` | Target interface | What our code expects from the PM team |
| `RealPerformanceIntegration.java` | Adapter | Translates PM team's DB classes to our expected interface |

---

## 13. How to Run

### Prerequisites
- Java 11+
- SQLite JDBC JAR (included: `sqlite-jdbc.jar`)
- SLF4J JARs (included: `slf4j-api.jar`, `slf4j-simple.jar`)
- A browser (Chrome/Firefox)

### Step 1 — Compile
```bash
cd workflow_taskflow/backend
javac -cp "sqlite-jdbc.jar:slf4j-api.jar:slf4j-simple.jar" *.java
```
> On Windows: use `;` instead of `:` in the classpath

### Step 2 — Run the Backend
```bash
java -cp ".:sqlite-jdbc.jar:slf4j-api.jar:slf4j-simple.jar" ApiServer
```
> On Windows:
```cmd
java -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" ApiServer
```

The server starts on **http://localhost:8080**. The database (`hrms.db`) is auto-created on first run.
### Step 3 — Run the Frontend 
```bash
cd integration/fronend
start customization.html
```
or 

### Step 3 — Open the Frontend
Open any HTML file directly in a browser:
- `workflow_taskflow/frontend/workflow.html` — Workflow Engine
- `workflow_taskflow/frontend/taskflow.html` — Task Flow Builder
- `form_flexfield/frontend/forms.html` — Form Designer
- `form_flexfield/frontend/flexfields.html` — Flexfield Manager
- `eit_lookup/frontend/eit.html` — EIT Handler
- `eit_lookup/frontend/lookup.html` — Lookup Customizer
- `module_report/frontend/module_customizer.html` — Module Customizer
- `module_report/frontend/report_builder.html` — Report Builder

---

## 14. API Reference

### Workflow Engine — `http://localhost:8080/api/workflows`

| Method | Endpoint | Body | Description |
|--------|----------|------|-------------|
| GET | `/api/workflows` | — | Get all workflows |
| GET | `/api/workflows/{id}` | — | Get workflow by ID |
| POST | `/api/workflows/create` | `{workflowName, assignedTo}` | Create workflow |
| POST | `/api/workflows/{id}/status` | `{status}` | Activate / Deactivate |
| DELETE | `/api/workflows/{id}` | — | Delete workflow |
| GET | `/api/workflows/{id}/steps` | — | Get all steps for a workflow |
| POST | `/api/workflows/{id}/steps` | `{stepName, assignee, escalationHours}` | Add step |
| DELETE | `/api/workflows/steps/{stepId}` | — | Remove step |

### Task Flow Builder — `http://localhost:8080/api/taskflows`

| Method | Endpoint | Body | Description |
|--------|----------|------|-------------|
| GET | `/api/taskflows` | — | Get all task flows |
| GET | `/api/taskflows/{id}` | — | Get task flow by ID |
| POST | `/api/taskflows/create` | `{flowName, linkedMenu, validateOnNext, allowBackNav}` | Create flow |
| POST | `/api/taskflows/{id}/status` | `{status}` | Activate / Deactivate |
| DELETE | `/api/taskflows/{id}` | — | Delete flow |
| GET | `/api/taskflows/{id}/windows` | — | Get all windows for a flow |
| POST | `/api/taskflows/{id}/windows` | `{windowName}` | Add window to flow |
| DELETE | `/api/taskflows/{id}/windows` | `{windowName}` | Remove window from flow |

---

*HRMS Customization Subsystem — Code Crafters, Section D*
*Course: Object-Oriented Analysis and Design*

## 15. Contributors 
- Rachana A - PES1UG23AM223
- Rakshitha M - PES1UG23AM231
- Raksha - PES1UG23AM229
- Neha PM - PES1UG23AM183
