# HRMS Customization - Bug Fixes Applied

## Summary
Fixed 4 critical issues preventing proper functionality of EIT Handler, Task Flow Builder, Onboarding/Offboarding workflows, and Appraisal workflow display.

---

## Issue 1: Add EIT - "Failed to fetch" Error
**Problem:** EIT form was throwing "Failed to fetch" error when trying to add a new EIT.

**Root Cause:** API endpoint mismatch - frontend was calling `/api/eit` (singular) but server registered endpoint as `/api/eits` (plural).

**Fix Applied:**
- **File:** `eit_lookup/frontend/eit.html` (line 218)
- **Change:** Changed fetch call from `${API}/api/eit` to `${API}/api/eits`
```javascript
// Before
const res = await fetch(`${API}/api/eit`, {

// After  
const res = await fetch(`${API}/api/eits`, {
```

**Status:** ✅ Fixed

---

## Issue 2: TaskFlow Page - Unable to Create Task Flows
**Problem:** TaskFlow page was not functioning - unable to create task flows, windows, or manage task flows.

**Root Cause:** All API calls were missing the `/api` prefix. Frontend was calling `/taskflows` instead of `/api/taskflows`, causing requests to fail.

**Fix Applied:**
- **File:** `workflow_taskflow/frontend/taskflow.html`
- **Changes:** Updated 8 API endpoint calls to include `/api` prefix

| Line | Before | After |
|------|--------|-------|
| 352 | `${API}/taskflows` | `${API}/api/taskflows` |
| 387 | `${API}/taskflows/${id}/windows` | `${API}/api/taskflows/${id}/windows` |
| 582 | `${API}/taskflows/create` | `${API}/api/taskflows/create` |
| 598 | `${API}/taskflows/${newId}/windows` | `${API}/api/taskflows/${newId}/windows` |
| 628 | `${API}/taskflows/${selectedTf.taskId}/windows` | `${API}/api/taskflows/${selectedTf.taskId}/windows` |
| 651 | `${API}/taskflows/${selectedTf.taskId}/windows` | `${API}/api/taskflows/${selectedTf.taskId}/windows` |
| 669 | `${API}/taskflows/${selectedTf.taskId}/status` | `${API}/api/taskflows/${selectedTf.taskId}/status` |
| 690 | `${API}/taskflows/${selectedTf.taskId}` | `${API}/api/taskflows/${selectedTf.taskId}` |

**Status:** ✅ Fixed

---

## Issue 3: Onboarding/Offboarding Triggers Not Working
**Problem:** Offboarding trigger was not functional - endpoint didn't exist in the API server.

**Root Cause:** Missing offboarding endpoint in ApiServer. Only `/api/employees/onboard` was implemented, but offboarding endpoint was missing.

**Fix Applied:**
- **File:** `workflow_taskflow/backend/ApiServer.java` (lines 744-761)
- **Changes:** Added complete offboarding endpoint handler

```java
} else if ("POST".equals(method) && path.matches("/api/employees/[^/]+/offboard")) {
    // Offboarding endpoint: /api/employees/{empId}/offboard
    String empId = path.substring("/api/employees/".length(), path.lastIndexOf("/offboard"));
    String b = body(ex);
    String lastDay = p(b, "lastWorkingDay");
    String reason = p(b, "reason");
    Employee emp = employeeIntegration.getEmployee(empId);
    if (emp == null) throw CustomizationExceptions.employeeDataReadOnly();
    
    // Create offboarding workflow
    String wfName = "Offboarding: " + emp.name + " - " + (nil(reason) ? "Exit" : reason);
    int wfId = workflowService.createWorkflow(wfName, "HR Admin");
    
    // Add offboarding steps
    workflowService.addStep(wfId, "Resignation Received", "HR Admin", 24);
    workflowService.addStep(wfId, "Asset Return", "IT Department", 48);
    workflowService.addStep(wfId, "Knowledge Transfer", "Line Manager", 72);
    workflowService.addStep(wfId, "Final Settlement", "Finance", 48);
    workflowService.addStep(wfId, "Exit Interview", "HR Admin", 24);
    
    send(ex, 200, "{\"message\":\"Offboarding workflow triggered\",...}");
}
```

**Features:**
- Extracts employee ID and offboarding details from request
- Creates offboarding workflow with predefined steps
- Automatically adds all 5 offboarding workflow steps (Resignation → Asset Return → Knowledge Transfer → Settlement → Exit Interview)
- Returns proper response with workflow instance ID

**Status:** ✅ Fixed

---

## Issue 4: Appraisal Workflow Not Appearing in Workflow Page
**Problem:** Appraisal workflows triggered from Performance Management page were not appearing in the Workflow Engine page.

**Root Cause:** Frontend was sending `workflowName` field but API server was expecting `name` field. This caused the workflow creation request to fail silently or be rejected.

**Fix Applied:**
- **File:** `integration/frontend/performance_management.html` (line 445)
- **Change:** Changed request body field from `workflowName` to `name`

```javascript
// Before
body: JSON.stringify({ workflowName: wfName, assignedTo: empId })

// After
body: JSON.stringify({ name: wfName, assignedTo: empId })
```

**Status:** ✅ Fixed

---

## Testing Instructions

1. **Compile Backend:**
   ```bash
   cd workflow_taskflow/backend
   javac -cp ".;hrms-database-1_0-SNAPSHOT-all.jar;slf4j-simple.jar;slf4j-api.jar;sqlite-jdbc.jar" *.java
   ```

2. **Start Server:**
   ```bash
   java -cp ".;hrms-database-1_0-SNAPSHOT-all.jar;slf4j-simple.jar;slf4j-api.jar;sqlite-jdbc.jar" ApiServer
   ```

3. **Test Each Feature:**
   - **EIT:** Open `eit_lookup/frontend/eit.html` → Try adding a new EIT
   - **TaskFlow:** Open `workflow_taskflow/frontend/taskflow.html` → Create and manage task flows
   - **Onboarding/Offboarding:** Open `integration/frontend/employee_onboarding.html` → Trigger workflows
   - **Appraisal:** Open `integration/frontend/performance_management.html` → Create appraisal workflow and verify it appears in Workflow Engine

---

## Files Modified

1. ✅ `eit_lookup/frontend/eit.html` - Fixed API endpoint (1 change)
2. ✅ `workflow_taskflow/frontend/taskflow.html` - Fixed API paths (8 changes)  
3. ✅ `workflow_taskflow/backend/ApiServer.java` - Added offboarding endpoint (1 new endpoint)
4. ✅ `integration/frontend/performance_management.html` - Fixed workflow field name (1 change)

**Total Changes:** 11 fixes applied across 4 files

---

## Verification

- ✅ Java compilation successful (no errors)
- ✅ All API endpoint paths corrected
- ✅ Missing offboarding endpoint implemented
- ✅ Field name mismatches resolved
- ✅ Ready for testing
