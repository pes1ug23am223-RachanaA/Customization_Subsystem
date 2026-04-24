import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DB-team access API for employee data used by onboarding/offboarding
 * integrations. External subsystems should not issue SQL directly; they
 * collaborate through this repository.
 *
 * GRASP: Information Expert and Indirection
 * SOLID: DIP-friendly boundary for employee persistence access
 */
public class DbEmployeeRepository {

    private final String dbUrl;
    private Connection conn;

    public DbEmployeeRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(dbUrl);
        } catch (ClassNotFoundException | SQLException e) {
            CustomizationErrorApi.getInstance().logError(
                "DbEmployeeRepository", "DB_CONNECT_FAILED", "Unable to connect to employee repository", e.getMessage());
        }
    }

    public EmployeeDTO findEmployeeDtoById(String empId) {
        if (empId == null || empId.trim().isEmpty() || conn == null) return null;
        String sql = "SELECT emp_id, name, role, department, employment_status FROM employees WHERE emp_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, empId.trim());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                EmployeeDTO emp = new EmployeeDTO();
                emp.empId = rs.getString("emp_id");
                emp.name = rs.getString("name");
                emp.role = rs.getString("role");
                emp.department = rs.getString("department");
                emp.status = rs.getString("employment_status");
                return emp;
            }
        } catch (SQLException e) {
            CustomizationErrorApi.getInstance().logError(
                "DbEmployeeRepository", "EMPLOYEE_READ_FAILED", "Unable to load employee", e.getMessage());
        }
        return null;
    }

    public Employee findEmployeeById(String empId) {
        EmployeeDTO dto = findEmployeeDtoById(empId);
        return dto == null ? null : new Employee(dto.empId, dto.name, dto.role, dto.department, dto.status);
    }

    public List<EmployeeDTO> findAllEmployeeDtos() {
        List<EmployeeDTO> employees = new ArrayList<>();
        if (conn == null) return employees;
        String sql = "SELECT emp_id, name, role, department, employment_status FROM employees ORDER BY emp_id";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                EmployeeDTO emp = new EmployeeDTO();
                emp.empId = rs.getString("emp_id");
                emp.name = rs.getString("name");
                emp.role = rs.getString("role");
                emp.department = rs.getString("department");
                emp.status = rs.getString("employment_status");
                employees.add(emp);
            }
        } catch (SQLException e) {
            CustomizationErrorApi.getInstance().logError(
                "DbEmployeeRepository", "EMPLOYEE_LIST_FAILED", "Unable to load employees", e.getMessage());
        }
        return employees;
    }

    public boolean updateRole(String empId, String role) {
        if (conn == null) return false;
        String sql = "UPDATE employees SET role = ? WHERE emp_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            pstmt.setString(2, empId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            CustomizationErrorApi.getInstance().logError(
                "DbEmployeeRepository", "EMPLOYEE_ROLE_UPDATE_FAILED", "Unable to update employee role", e.getMessage());
            return false;
        }
    }
}
