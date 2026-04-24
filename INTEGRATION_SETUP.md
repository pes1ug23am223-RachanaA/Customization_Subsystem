# HRMS Customization Subsystem - Data & Integration Setup

## ✅ What's Been Fixed and Added

### 1. **Performance Management Data** ✓
- **5 Goals** with various statuses (In Progress, Completed, Not Started)
- **6 Appraisals** with scores from 3.8 to 4.5/5.0
- **5 Feedback Records** (360° feedback from managers and peers)
- **3 Performance Cycles** (Q1-2026, Q4-2025, etc.)

**Tables Populated:**
```
- goals (5 records)
- appraisals (6 records)
- feedback (5 records)
- kpis (performance targets and actuals)
```

### 2. **Employee Onboarding Data** ✓
**8 Employees in different stages:**
```
PRE-JOINING (2):
  - EMP_NEW_001: Rajesh Kumar (Senior Software Engineer)
  - EMP_NEW_002: Priya Sharma (Product Manager)

ONBOARDING (2):
  - EMP_OB_001: Amit Patel (Junior Developer)
  - EMP_OB_002: Neha Gupta (HR Specialist)

ACTIVE (4):
  - EMP001: Arjun Singh (Senior Software Engineer)
  - EMP002: Deepak Malhotra (Team Lead)
  - EMP003: Kavya Reddy (QA Engineer)
  - MGR001: Vikram Desai (Engineering Manager)
```

### 3. **UI Integration Features Added** ✓

#### **Performance Management Page** (`integration/frontend/performance_management.html`)

New integration sections:
```
1. "Trigger Appraisal Workflow" Panel
   - Select employee
   - Choose appraisal period (e.g., Q2-2026)
   - Start appraisal workflow → Creates formal review cycle
   - Notifies reviewers automatically

2. "Create Performance Form" Panel
   - Create customized appraisal forms
   - Link to performance cycles
   - Use Form Designer integration
   - Distribute to reviewers/employees
```

**JavaScript Functions Added:**
- `triggerAppraisalWorkflow()` - Start performance appraisal workflow
- `createPerformanceForm()` - Create performance-linked forms
- `populateAppraisalDropdowns()` - Populate employee and cycle dropdowns

#### **Employee Onboarding Page** (`integration/frontend/employee_onboarding.html`)

New integration section:
```
"Link Performance Management for New Hires" Panel
   - Select employee
   - Choose goal template (Onboarding, Q1-Q4 Goals)
   - Auto-setup:
     * Create initial goals
     * Assign to current performance cycle
     * Trigger 30-day check-in workflow
```

**JavaScript Function Added:**
- `linkPerformanceManagement()` - Connect performance management to new hires
- `populatePerfLinkDropdown()` - Load employees for performance linking

---

## 📊 Data Insertion Utilities Created

### `InsertPerformanceData.java`
Populates all performance management tables:
- Creates goals with descriptions and dates
- Inserts appraisals with scores and statuses
- Adds 360° feedback records
- Inserts KPI targets and actuals

**Run:**
```bash
cd workflow_taskflow/backend
javac -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" InsertPerformanceData.java
java -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" InsertPerformanceData
```

### `InsertEmployeeData.java`
Populates employee table with realistic data:
- Pre-joining candidates
- Employees in onboarding
- Active team members
- Managers

**Run:**
```bash
cd workflow_taskflow/backend
javac -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" InsertEmployeeData.java
java -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" InsertEmployeeData
```

---

## 🔗 Integration Points

### Performance Management ↔ Workflow Engine
```
Trigger Appraisal Workflow:
  → /api/workflows/create (POST)
  → Creates workflow instance for employee
  → Notifies reviewers
  → Links to performance cycle
```

### Performance Management ↔ Form Designer
```
Create Performance Form:
  → /api/forms/create (POST)
  → Creates appraisal form for cycle
  → Uses Form Designer customization
  → Links to performance management
```

### Employee Onboarding ↔ Performance Management
```
Link Performance Management:
  → /api/performance/create-goals (POST)
  → Creates initial goals for new hire
  → Assigns to current performance cycle
  → Triggers 30-day check-in workflow
```

### Employee Onboarding ↔ Workflow Engine
```
Trigger Onboarding Workflow:
  → /api/employees/onboard (POST)
  → Creates onboarding workflow
  → Can integrate with performance management
  → Manages employee lifecycle
```

---

## 📈 Runtime Status

### Database Tables (Live)
- ✓ goals (5 records)
- ✓ appraisals (6 records)
- ✓ feedback (5 records)
- ✓ employees (8 records)
- ✓ kpis (5 records)
- ✓ custom_forms, custom_fields
- ✓ workflows, workflow_steps
- ✓ task_flows, task_flow_windows

### Dashboard Metrics (Before vs After)
```
BEFORE:
  Performance Cycles: 0
  Goals: 0
  Appraisals: 0
  Feedback: 0
  Employees: 0

AFTER:
  Performance Cycles: 3 ✓
  Goals: 5 ✓
  Appraisals: 6 ✓
  Feedback: 5 ✓
  Employees: 8 ✓
```

---

## 🎯 What's Now Connected

### Performance Management Subsystem
- ✅ Reads live data from hrms.db
- ✅ Shows performance cycles, goals, appraisals, feedback, KPIs
- ✅ Can trigger appraisal workflows
- ✅ Can create performance forms
- ✅ Integrated with workflow engine

### Employee Onboarding Subsystem
- ✅ Reads employee data from hrms.db
- ✅ Shows employee pipeline (pre-joining, onboarding, active)
- ✅ Can trigger onboarding workflows
- ✅ Can link to performance management
- ✅ Integrated with performance system

### Integration Bridges
- ✅ Onboarding → Performance Management (link new hires to performance)
- ✅ Performance Management → Workflows (trigger appraisal workflows)
- ✅ Performance Management → Forms (create appraisal forms)

---

## 🚀 How to Use

### 1. Refresh the Dashboard
- Open `http://localhost:8080/integration/frontend/performance_management.html`
- See all performance data (3 cycles, 5 goals, 6 appraisals, 5 feedback)

### 2. Trigger Appraisal Workflow
- Navigate to "KPIs" tab
- Scroll to "Trigger Appraisal Workflow" section
- Select an employee (EMP001, EMP002, EMP003)
- Enter appraisal period (e.g., Q2-2026)
- Click "Start Appraisal Workflow"

### 3. Create Performance Form
- In "KPIs" tab, scroll to "Create Performance Form" section
- Enter form name
- Select a performance cycle
- Click "Create Form"

### 4. Employee Onboarding Integration
- Open `http://localhost:8080/integration/frontend/employee_onboarding.html`
- See employee pipeline with 8 employees
- For pre-joining employees, click "Start Workflow" button
- Or use "Link Performance Management" section to setup performance goals

---

## 📝 Files Modified/Created

### New Files:
- ✓ `InsertPerformanceData.java` - Performance data population utility
- ✓ `InsertEmployeeData.java` - Employee data population utility

### Modified Files:
- ✓ `integration/frontend/performance_management.html`
  - Added "Trigger Appraisal Workflow" panel
  - Added "Create Performance Form" panel
  - Added JavaScript functions for triggers

- ✓ `integration/frontend/employee_onboarding.html`
  - Added "Link Performance Management for New Hires" panel
  - Added JavaScript function for performance linking
  - Enhanced employee dropdown population

---

## ✨ Result

The HRMS Customization Subsystem now has:
1. **Populated Data** - Real sample data in all tables
2. **UI Integration** - Buttons and forms to trigger workflows
3. **Connected Systems** - Performance ↔ Onboarding ↔ Workflows
4. **Live Dashboards** - Shows meaningful data instead of zeros

**All metrics are now visible and interactive!**
