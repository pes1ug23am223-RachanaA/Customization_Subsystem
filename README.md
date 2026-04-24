# HRMS Customization Subsystem — v2 Upgrade

## What Changed

### 1. Form Designer
- **Fix**: "Open Form" no longer redirects to a new page — shows inline form view with field rendering and submit validation
- **Fix**: Duplicate field names now raise a clear error message (both for regular fields and flexfields)
- **Fix**: Duplicate form names are blocked on creation
- **New**: "Remove" button on every field row allows deletion of individual fields
- **New**: Form submit validates required fields before accepting

### 2. Flexfield Manager
- **Changed**: "Segments" column now shows actual segment names (not just a count)
- **New**: "Edit Segments" button opens an inline segment manager — add/remove named segments
- **New**: "Used In Forms" column shows which forms reference each flexfield
- **Fix**: Duplicate flexfield names are blocked

### 3. Workflow Engine
- **New**: "Approve Step" button marks the current step as approved and advances to the next one
- **New**: Progress bar shows approved vs. pending steps
- **New**: Elapsed time displayed on active steps (auto-refreshes every 30s)
- **New**: Fast-approval warning dialog if a step is approved in under 1 hour
- **New**: Escalation log panel records overdue steps and fast approvals
- **New**: Predefined workflow templates: Leave Approval, Performance Appraisal, Onboarding, Offboarding
- **Fix**: Approval state persists in localStorage across page reloads

### 4. Task Flow Builder
- **Redesign**: Teal color scheme to visually distinguish from Workflow (blue)
- **New**: Interactive simulation mode — click "Simulate Flow" to walk through windows
- **New**: Validation-on-next: blocks advance if required fields are not filled
- **New**: Review & Submit window shows collected data before final submission
- **New**: Predefined templates: Employee Onboarding, Leave Application, Expense Claim, Recruitment
- **New**: Role-based assignment (Employee, HR Admin, Manager, etc.)
- **New**: Right panel explains the difference between Task Flow and Workflow clearly

### 5. EIT Handler
- **New**: EIT cards with context badge (EMPLOYEE / JOB / POSITION), type badge, linked employee
- **New**: "Inject EIT into Form" panel — attach EIT fields directly into any form
- **Fix**: Duplicate EIT names within the same context are blocked

### 6. Lookup Customizer
- **New**: Employee Data Lookup panel — live search by name, ID, department, job title, status
- **New**: "Extract to Lookup" button — creates a reusable lookup from search result values
- **New**: "Use in Form" button — adds a dropdown field using the lookup to any form
- **Fix**: Duplicate lookup codes and duplicate values within a lookup are blocked

### 7. Report Builder
- **New**: CSV and PDF export formats
- **New**: Export dialog with column selector — choose which columns to include
- **New**: Row filter (active employees, onboarding, by department, recent)
- **New**: Data preview table shows live rows from the selected source
- **New**: 8 data sources: employees, performance cycles, appraisals, workflows, forms, fields, EITs, lookups
- **Fix**: Client-side fallback export when server endpoint unavailable

### 8. Module Customizer
- **New**: Display order configuration per module
- **New**: Role access control per module (All Users, HR Admin, Manager, etc.)
- **New**: Sidebar visibility toggle per module
- **New**: Require confirmation on delete toggle
- **New**: Change log audit trail (this session)
- **New**: "Enable All" button
- **New**: Module search/filter

### 9. Performance Management
- **New**: Workflow choice — Custom or Predefined template (Standard 6-step, Fast Track, Probation Review)
- **New**: Form choice — Custom or Predefined template (Standard Appraisal, KPI Review, Probation Form)
- **Fix**: Predefined workflows automatically populate with the correct approval steps
- **Fix**: Predefined forms automatically populate with the correct fields

### 10. Employee Onboarding & Offboarding
- **New**: Workflow type choice — Custom or Predefined (Standard, Fast Track, Executive)
- **New**: Predefined onboarding steps added automatically on workflow creation
- **New**: Offboarding section with reason, last working day, and workflow trigger
- **New**: Link to Performance Management with cycle selection
- **Fix**: Pipeline shows employee counts per stage with proper categorization

### 11. UI / Design
- Removed all emojis from navigation labels and banners
- Consistent CSS variable system across all modules
- Teal sidebar theme for Task Flow to differentiate from Workflow (blue)
- Toast notifications have type-colored left border (success/error/warning)
- Cleaner panel headers with proper separation

### 12. Code Quality
- `nav.js` shared sidebar navigation (single source of truth)
- `style.css` v2 with CSS variables for consistent theming
- All forms use `btn-ghost` / `btn-danger` / `btn-success` classes (no inline styles)
- Duplicate-check logic in every create/add operation
- Exception messages propagated from server responses to user-facing toasts

## Running the System

1. Compile and start the Java backend:
   ```
   cd workflow_taskflow/backend
   javac -cp sqlite-jdbc.jar:slf4j-api.jar:slf4j-simple.jar *.java
   java -cp .:sqlite-jdbc.jar:slf4j-api.jar:slf4j-simple.jar ApiServer
   ```
2. Open any HTML file directly in a browser (no web server needed for frontend).
3. The backend serves on `http://localhost:8080`.

## Project Structure

```
hrms_v2/
  form_flexfield/
    frontend/       forms.html, flexfields.html, forms.js, style.css, nav.js
    backend/        DTO, exception, facade, factory, observer, repository, service
  workflow_taskflow/
    frontend/       workflow.html, taskflow.html
    backend/        *.java, *.jar, hrms.db (ApiServer entry point)
  eit_lookup/
    frontend/       eit.html, lookup.html
    backend/        dto, repository, service
  module_report/
    frontend/       module_customizer.html, report_builder.html
    backend/        controller, exception, model, repository, service
  integration/
    frontend/       employee_onboarding.html, performance_management.html, customization.html
    backend/        IPerformanceForCustomization.java, IPerformanceSubsystem.java
  hrms.db           Shared SQLite database
  README.md
  INTEGRATION_SETUP.md
```
