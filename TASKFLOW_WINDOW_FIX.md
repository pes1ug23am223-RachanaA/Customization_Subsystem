# Task Flow Builder - Window Type & Fields Fix

## Problem Statement
Users reported that when adding windows to a task flow, the window type was always showing as "Middle" and the field count was showing as "0", even after selecting different types (Entry, Review, Exit) and entering fields.

## Root Causes Identified

1. **Frontend Issue - Missing Fields in API Request**
   - The `addWindow()` function in taskflow.html was reading the window type and fields from the form inputs
   - But the API request body only sent `windowName` - not `windowType` or `fields`
   - Modal input fields were not being cleared after adding a window

2. **Backend Issue - Missing Database Schema**
   - The `task_flow_windows` table in the database only had `window_id`, `task_id`, and `window_name` columns
   - No columns for `window_type` or `fields` existed
   - When windows were retrieved from the database, they defaulted to `"Middle"` type

3. **Backend API Issue - No Type/Fields Support**
   - The API endpoint `/api/taskflows/{id}/windows` (POST) only accepted and processed `windowName`
   - Window type and fields parameters were ignored if sent

## Fixes Applied

### 1. Frontend (taskflow.html)

**File:** `workflow_taskflow/frontend/taskflow.html`

```javascript
// Before: Only sent windowName
body: JSON.stringify({ windowName: name, windowType: type })

// After: Also sends windowType and fields, with proper array conversion
body: JSON.stringify({ windowName: name, windowType: type, fields: fields })
```

**Additional fixes:**
- Added clearing of modal input fields after successful window addition
- Modal now resets all three inputs: `newWinName`, `newWinType`, and `newWinFields`

### 2. Backend API Handler (ApiServer.java)

**File:** `workflow_taskflow/backend/ApiServer.java`

```java
// Before:
String b = body(ex), wName = p(b, "windowName");
taskFlowService.addWindow(taskId, wName);

// After:
String b = body(ex), wName = p(b, "windowName"), wType = p(b, "windowType");
String windowType = !nil(wType) ? wType.trim() : "Middle";
String fieldsParam = p(b, "fields");
String fields = "";
if (!nil(fieldsParam)) {
    fields = fieldsParam.replaceAll("[\\[\\]\"]", "").replaceAll(",\\s*", ",").trim();
}
taskFlowService.addWindow(taskId, wName, windowType, fields);
```

**Additional changes:**
- Updated window retrieval endpoint to return window objects with type and fields instead of just names
- Added `fieldsJson()` helper method to convert comma-separated fields to JSON array

### 3. Database Schema (DbTaskFlowRepository.java)

**File:** `workflow_taskflow/backend/DbTaskFlowRepository.java`

```sql
-- Before:
CREATE TABLE IF NOT EXISTS task_flow_windows (
    window_id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER NOT NULL,
    window_name TEXT NOT NULL,
    FOREIGN KEY(task_id) REFERENCES task_flows(task_id))

-- After:
CREATE TABLE IF NOT EXISTS task_flow_windows (
    window_id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id INTEGER NOT NULL,
    window_name TEXT NOT NULL,
    window_type TEXT DEFAULT 'Middle',
    fields TEXT DEFAULT '',
    FOREIGN KEY(task_id) REFERENCES task_flows(task_id))
```

**Additional changes:**
- Added migration code to safely add new columns to existing tables
- Created `WindowData` inner class to hold window details (name, type, fields)
- Added `getWindowsForFlowWithDetails()` method to retrieve full window information
- Added overloaded `addWindowToFlow(int id, String win, String windowType, String fields)` method

### 4. Service Layer (TaskFlowService & TaskFlowServiceImpl)

**File:** `workflow_taskflow/backend/TaskFlowService.java`
- Added overloaded `addWindow()` method signature to support window type and fields

**File:** `workflow_taskflow/backend/TaskFlowServiceImpl.java`
- Implemented new `addWindow(int taskId, String windowName, String windowType, String fields)` method
- Method properly validates and passes parameters to repository

## Testing Recommendations

1. **Frontend Testing:**
   - Add a window with type "Entry" and enter fields "Name, ID Number"
   - Verify the table now shows:
     - Window type: "Entry" (not "Middle")
     - Fields count: 2 (not 0)
   - Verify the modal inputs clear after adding

2. **Backend Testing:**
   - Check database `task_flow_windows` table has window_type and fields columns
   - Verify GET `/api/taskflows/{id}/windows` returns full window objects like:
     ```json
     [
       {"name": "Personal Details", "type": "Entry", "fields": ["Name", "ID Number"]},
       {"name": "Review", "type": "Review", "fields": []}
     ]
     ```

3. **End-to-End Testing:**
   - Create a new task flow
   - Add windows with different types: Entry, Middle, Review, Exit
   - Add varying numbers of fields to each window
   - Simulate the task flow
   - Verify navigation works correctly through all windows

## Files Modified

1. `workflow_taskflow/frontend/taskflow.html` - Frontend form handling
2. `workflow_taskflow/backend/ApiServer.java` - API endpoint handling
3. `workflow_taskflow/backend/DbTaskFlowRepository.java` - Database schema and access
4. `workflow_taskflow/backend/TaskFlowService.java` - Service interface
5. `workflow_taskflow/backend/TaskFlowServiceImpl.java` - Service implementation

## Compilation Status

✅ Backend compiles successfully with no errors
✅ All changes maintain backward compatibility
✅ Existing task flows continue to work (defaults to "Middle" type)
