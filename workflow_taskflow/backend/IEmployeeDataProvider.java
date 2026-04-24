import java.util.List;



/**
 * INTERFACE: IEmployeeDataProvider
 * Provided by: Employee Offboarding Subsystem
 * Consumed by: Customization Subsystem (Code Crafters)
 *
 * PURPOSE:
 *   Provides controlled, read-only access to employee data required by
 *   the Customization subsystem for:
 *     - Workflow assignment and validation
 *     - Form (EIT) attachment
 *     - Lookup and display of employee-related metadata
 *
 * HOW TO USE:
 *   The Customization subsystem should invoke this interface instead of
 *   directly accessing any database or internal model classes.
 *
 *   Example usage:
 *     IEmployeeDataProvider provider = ... (injected by integration layer)
 *     EmployeeDTO emp = provider.getEmployeeById("EMP123");
 *
 * DESIGN CONSTRAINTS:
 *   - This interface is strictly READ-ONLY
 *   - No mutation of employee data is permitted through this contract
 *   - Implementation details (DB, caching, in-memory storage) are hidden
 *
 * EXPECTED BEHAVIOUR:
 *   - Returns null if employee is not found
 *   - Returns a list of currently known employees in the system
 */
public interface IEmployeeDataProvider {

    /**
     * Retrieve a single employee by their unique ID.
     *
     * @param empId  employee identifier (must match IDs used in workflow triggers)
     * @return EmployeeDTO if found, otherwise null
     */
    EmployeeDTO getEmployeeById(String empId);

    /**
     * Retrieve all employees currently available in the system.
     *
     * @return list of EmployeeDTO objects
     */
    List<EmployeeDTO> getAllEmployees();
}