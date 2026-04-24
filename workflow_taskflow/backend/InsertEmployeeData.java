import java.sql.*;

/**
 * InsertEmployeeData.java
 * Utility to insert sample employee onboarding data into hrms.db
 *
 * Creates employees in various onboarding stages for integration testing.
 *
 * HOW TO RUN:
 *   cd workflow_taskflow/backend
 *   javac -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" InsertEmployeeData.java
 *   java -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" InsertEmployeeData
 */
public class InsertEmployeeData {

    private static final String DB_URL = "jdbc:sqlite:hrms.db";

    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DB_URL);
            
            System.out.println("╔════════════════════════════════════════╗");
            System.out.println("║  Inserting Sample Employee Data         ║");
            System.out.println("╚════════════════════════════════════════╝");
            
            // Create tables if they don't exist
            createTables(conn);
            
            // Clear existing data
            clearEmployees(conn);
            
            // Insert sample employees
            insertEmployees(conn);
            
            conn.close();
            
            System.out.println("\n✓ Sample employee data inserted successfully!");
            System.out.println("  Total Employees: 8 records");
            System.out.println("  - Pre-Joining: 2");
            System.out.println("  - Onboarding: 2");
            System.out.println("  - Active: 4");
            System.out.println("\nRefresh the Employee Onboarding page to see the data.");
            
        } catch (Exception e) {
            System.err.println("✗ Error inserting data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        String createEmployeesTable = "CREATE TABLE IF NOT EXISTS employees ("
            + "emp_id TEXT PRIMARY KEY,"
            + "name TEXT,"
            + "role TEXT,"
            + "department TEXT,"
            + "employment_status TEXT)";
        
        try (Statement st = conn.createStatement()) {
            st.execute(createEmployeesTable);
            System.out.println("✓ Employee table created/verified");
        }
    }

    private static void clearEmployees(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("DELETE FROM employees");
            System.out.println("✓ Cleared existing employee data");
        }
    }

    private static void insertEmployees(Connection conn) throws SQLException {
        String insertEmployee = "INSERT INTO employees (emp_id, name, role, department, employment_status) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(insertEmployee)) {
            // Pre-Joining: 2 employees
            ps.setString(1, "EMP_NEW_001");
            ps.setString(2, "Rajesh Kumar");
            ps.setString(3, "Senior Software Engineer");
            ps.setString(4, "Engineering");
            ps.setString(5, "PRE_JOINING");
            ps.addBatch();
            
            ps.setString(1, "EMP_NEW_002");
            ps.setString(2, "Priya Sharma");
            ps.setString(3, "Product Manager");
            ps.setString(4, "Product");
            ps.setString(5, "PRE_JOINING");
            ps.addBatch();
            
            // Onboarding: 2 employees
            ps.setString(1, "EMP_OB_001");
            ps.setString(2, "Amit Patel");
            ps.setString(3, "Junior Developer");
            ps.setString(4, "Engineering");
            ps.setString(5, "ONBOARDING");
            ps.addBatch();
            
            ps.setString(1, "EMP_OB_002");
            ps.setString(2, "Neha Gupta");
            ps.setString(3, "HR Specialist");
            ps.setString(4, "Human Resources");
            ps.setString(5, "ONBOARDING");
            ps.addBatch();
            
            // Active: 4 employees
            ps.setString(1, "EMP001");
            ps.setString(2, "Arjun Singh");
            ps.setString(3, "Senior Software Engineer");
            ps.setString(4, "Engineering");
            ps.setString(5, "ACTIVE");
            ps.addBatch();
            
            ps.setString(1, "EMP002");
            ps.setString(2, "Deepak Malhotra");
            ps.setString(3, "Team Lead");
            ps.setString(4, "Engineering");
            ps.setString(5, "ACTIVE");
            ps.addBatch();
            
            ps.setString(1, "EMP003");
            ps.setString(2, "Kavya Reddy");
            ps.setString(3, "QA Engineer");
            ps.setString(4, "Quality Assurance");
            ps.setString(5, "ACTIVE");
            ps.addBatch();
            
            ps.setString(1, "MGR001");
            ps.setString(2, "Vikram Desai");
            ps.setString(3, "Engineering Manager");
            ps.setString(4, "Engineering");
            ps.setString(5, "ACTIVE");
            ps.addBatch();
            
            int[] inserted = ps.executeBatch();
            System.out.println("✓ Inserted " + inserted.length + " employees");
        }
    }
}
