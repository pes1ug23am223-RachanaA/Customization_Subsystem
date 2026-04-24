import java.util.List;

/**
 * CLASS: RealEmployeeDataProvider
 * Used by: Customization Subsystem (Code Crafters)
 *
 * PURPOSE:
 *   Provides real implementations of IEmployeeDataProvider by connecting
 *   to the SQLite HRMS database (hrms.db). Replaces MockOnboardingIntegration
 *   for production use.
 *
 * INTEGRATION:
 *   - Reads employee data from the HRMS database employees table
 *   - Employee IDs are Strings (e.g., "EMP001")
 *   - Provides controlled read-only access to employee data
 *   - Used by EIT Handler for emp ID validation
 *   - Used by Workflow Engine for employee lookups and assignment
 */
public class RealEmployeeDataProvider implements IEmployeeDataProvider {

    private static final String DB_URL = "jdbc:sqlite:hrms.db";
    private final DbEmployeeRepository employeeRepository;

    public RealEmployeeDataProvider() {
        // Indirection through the DB-team repository keeps SQL out of the
        // integration-facing provider and supports DIP/Protected Variations.
        employeeRepository = new DbEmployeeRepository(DB_URL);
    }

    /**
     * Retrieve a single employee by their unique ID from the database.
     *
     * @param empId employee identifier (e.g., "EMP001")
     * @return EmployeeDTO if found, otherwise null
     */
    @Override
    public EmployeeDTO getEmployeeById(String empId) {
        return employeeRepository.findEmployeeDtoById(empId);
    }

    /**
     * Retrieve all employees currently in the database.
     *
     * @return list of EmployeeDTO objects
     */
    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAllEmployeeDtos();
    }
}
