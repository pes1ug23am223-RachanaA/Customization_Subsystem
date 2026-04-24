

/**
 * CLASS: EmployeeDTO  (Data Transfer Object for Employee Information)
 * Provided by: Employee Offboarding Subsystem
 * Consumed by: Customization Subsystem (Code Crafters)
 *
 * PURPOSE:
 *   Represents a simplified, read-only view of employee data that can be safely
 *   consumed by external subsystems such as Workflow Engine and Form Designer.
 *
 *   This object is intentionally minimal and does NOT expose internal
 *   implementation details of the Offboarding system.
 *
 * USAGE:
 *   Returned by IEmployeeDataProvider methods when employee information is requested.
 *
 * DESIGN PRINCIPLES:
 *   - Immutable by convention (fields should not be modified externally)
 *   - Decoupled from internal models (e.g., EmployeeRecord, DB entities)
 *   - Contains only fields required for workflow assignment and form rendering
 *
 * FIELD DESCRIPTIONS:
 *   empId           - Unique identifier of the employee (primary key)
 *   name            - Full name of the employee
 *   role            - Employee's job role/title
 *   department      - Department to which the employee belongs
 *   lastWorkingDay  - Final working date (ISO format: YYYY-MM-DD)
 *   status          - Current offboarding status (e.g. "Running", "Complete", "Error")
 */
public class EmployeeDTO {

    public String empId;
    public String name;
    public String role;
    public String department;
    public String lastWorkingDay;
    public String status;

}