import java.sql.*;

/**
 * InsertPerformanceData.java
 * Utility to insert sample performance data into hrms.db
 *
 * HOW TO RUN:
 *   cd workflow_taskflow/backend
 *   javac -cp ".:sqlite-jdbc.jar" InsertPerformanceData.java
 *   java -cp ".:sqlite-jdbc.jar" InsertPerformanceData
 */
public class InsertPerformanceData {

    private static final String DB_URL = "jdbc:sqlite:hrms.db";

    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DB_URL);
            
            System.out.println("╔════════════════════════════════════════╗");
            System.out.println("║  Inserting Sample Performance Data      ║");
            System.out.println("╚════════════════════════════════════════╝");
            
            // Create tables if they don't exist
            createTables(conn);
            
            // Clear existing data
            clearTables(conn);
            
            // Insert sample goals
            insertGoals(conn);
            
            // Insert sample appraisals
            insertAppraisals(conn);
            
            conn.close();
            
            System.out.println("\n✓ Sample data inserted successfully!");
            System.out.println("  Goals: 5 records");
            System.out.println("  Appraisals: 6 records");
            System.out.println("\nRefresh the Performance Management dashboard to see the data.");
            
        } catch (Exception e) {
            System.err.println("✗ Error inserting data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        // Goals table
        String createGoalsTable = "CREATE TABLE IF NOT EXISTS goals ("
            + "goal_id INTEGER PRIMARY KEY,"
            + "emp_id TEXT,"
            + "goal_title TEXT,"
            + "goal_description TEXT,"
            + "goal_status TEXT,"
            + "goal_start_date TEXT,"
            + "goal_end_date TEXT)";
        
        // Appraisals table
        String createAppraisalsTable = "CREATE TABLE IF NOT EXISTS appraisals ("
            + "appraisal_id INTEGER PRIMARY KEY,"
            + "emp_id TEXT,"
            + "reviewer_id TEXT,"
            + "appraisal_score REAL,"
            + "appraisal_status TEXT,"
            + "appraisal_period TEXT,"
            + "appraisal_date TEXT)";
        
        try (Statement st = conn.createStatement()) {
            st.execute(createGoalsTable);
            st.execute(createAppraisalsTable);
            System.out.println("✓ Tables created/verified");
        }
    }

    private static void clearTables(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("DELETE FROM goals");
            st.execute("DELETE FROM appraisals");
            System.out.println("✓ Cleared existing data");
        }
    }

    private static void insertGoals(Connection conn) throws SQLException {
        String insertGoal = "INSERT INTO goals (goal_id, emp_id, goal_title, goal_description, goal_status, goal_start_date, goal_end_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(insertGoal)) {
            // Goal 1
            ps.setInt(1, 1);
            ps.setString(2, "EMP001");
            ps.setString(3, "Complete Project Alpha");
            ps.setString(4, "Deliver the alpha version of the new HRMS module");
            ps.setString(5, "In Progress");
            ps.setString(6, "2026-01-15");
            ps.setString(7, "2026-03-31");
            ps.addBatch();
            
            // Goal 2
            ps.setInt(1, 2);
            ps.setString(2, "EMP001");
            ps.setString(3, "Improve Code Quality");
            ps.setString(4, "Reduce code complexity and improve test coverage to 85%");
            ps.setString(5, "In Progress");
            ps.setString(6, "2026-01-01");
            ps.setString(7, "2026-04-30");
            ps.addBatch();
            
            // Goal 3
            ps.setInt(1, 3);
            ps.setString(2, "EMP002");
            ps.setString(3, "Team Leadership");
            ps.setString(4, "Lead and mentor 3 junior developers");
            ps.setString(5, "In Progress");
            ps.setString(6, "2026-01-01");
            ps.setString(7, "2026-12-31");
            ps.addBatch();
            
            // Goal 4
            ps.setInt(1, 4);
            ps.setString(2, "EMP002");
            ps.setString(3, "Process Automation");
            ps.setString(4, "Automate HR approval workflows");
            ps.setString(5, "Completed");
            ps.setString(6, "2025-09-01");
            ps.setString(7, "2026-02-28");
            ps.addBatch();
            
            // Goal 5
            ps.setInt(1, 5);
            ps.setString(2, "EMP003");
            ps.setString(3, "Customer Satisfaction");
            ps.setString(4, "Maintain 95% customer satisfaction rating");
            ps.setString(5, "Not Started");
            ps.setString(6, "2026-03-01");
            ps.setString(7, "2026-09-30");
            ps.addBatch();
            
            int[] inserted = ps.executeBatch();
            System.out.println("✓ Inserted " + inserted.length + " goals");
        }
    }

    private static void insertAppraisals(Connection conn) throws SQLException {
        String insertAppraisal = "INSERT INTO appraisals (appraisal_id, emp_id, reviewer_id, appraisal_score, appraisal_status, appraisal_period, appraisal_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(insertAppraisal)) {
            // Appraisal 1
            ps.setInt(1, 1);
            ps.setString(2, "EMP001");
            ps.setString(3, "MGR001");
            ps.setFloat(4, 4.2f);
            ps.setString(5, "Completed");
            ps.setString(6, "Q1-2026");
            ps.setString(7, "2026-03-15");
            ps.addBatch();
            
            // Appraisal 2
            ps.setInt(1, 2);
            ps.setString(2, "EMP002");
            ps.setString(3, "MGR001");
            ps.setFloat(4, 4.5f);
            ps.setString(5, "Completed");
            ps.setString(6, "Q1-2026");
            ps.setString(7, "2026-03-20");
            ps.addBatch();
            
            // Appraisal 3
            ps.setInt(1, 3);
            ps.setString(2, "EMP003");
            ps.setString(3, "MGR002");
            ps.setFloat(4, 3.8f);
            ps.setString(5, "In Progress");
            ps.setString(6, "Q1-2026");
            ps.setString(7, "2026-03-25");
            ps.addBatch();
            
            // Appraisal 4
            ps.setInt(1, 4);
            ps.setString(2, "EMP001");
            ps.setString(3, "MGR001");
            ps.setFloat(4, 4.0f);
            ps.setString(5, "Completed");
            ps.setString(6, "Q4-2025");
            ps.setString(7, "2025-12-20");
            ps.addBatch();
            
            // Appraisal 5
            ps.setInt(1, 5);
            ps.setString(2, "EMP002");
            ps.setString(3, "MGR001");
            ps.setFloat(4, 4.3f);
            ps.setString(5, "Completed");
            ps.setString(6, "Q4-2025");
            ps.setString(7, "2025-12-22");
            ps.addBatch();
            
            // Appraisal 6
            ps.setInt(1, 6);
            ps.setString(2, "EMP003");
            ps.setString(3, "MGR002");
            ps.setFloat(4, 4.1f);
            ps.setString(5, "Completed");
            ps.setString(6, "Q4-2025");
            ps.setString(7, "2025-12-25");
            ps.addBatch();
            
            int[] inserted = ps.executeBatch();
            System.out.println("✓ Inserted " + inserted.length + " appraisals");
        }
        
        // Insert feedback
        insertFeedback(conn);
        
        // Insert KPIs
        insertKPIs(conn);
    }

    private static void insertFeedback(Connection conn) throws SQLException {
        try {
            // Create feedback table if not exists
            String createTable = "CREATE TABLE IF NOT EXISTS feedback ("
                + "feedback_id INTEGER PRIMARY KEY,"
                + "emp_id TEXT,"
                + "reviewer_id TEXT,"
                + "feedback_text TEXT,"
                + "feedback_type TEXT,"
                + "feedback_date TEXT)";
            
            try (Statement st = conn.createStatement()) {
                st.execute(createTable);
            }
            
            String insertFeedback = "INSERT INTO feedback (feedback_id, emp_id, reviewer_id, feedback_text, feedback_type, feedback_date) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement ps = conn.prepareStatement(insertFeedback)) {
                // Feedback 1
                ps.setInt(1, 1);
                ps.setString(2, "EMP001");
                ps.setString(3, "MGR001");
                ps.setString(4, "Excellent technical skills and problem-solving abilities. Great team player.");
                ps.setString(5, "360_DEGREE");
                ps.setString(6, "2026-03-10");
                ps.addBatch();
                
                // Feedback 2
                ps.setInt(1, 2);
                ps.setString(2, "EMP001");
                ps.setString(3, "PEER001");
                ps.setString(4, "Very responsive to feedback and always willing to help teammates.");
                ps.setString(5, "PEER");
                ps.setString(6, "2026-03-12");
                ps.addBatch();
                
                // Feedback 3
                ps.setInt(1, 3);
                ps.setString(2, "EMP002");
                ps.setString(3, "MGR001");
                ps.setString(4, "Outstanding leadership and mentoring skills. Needs to focus on time management.");
                ps.setString(5, "360_DEGREE");
                ps.setString(6, "2026-03-18");
                ps.addBatch();
                
                // Feedback 4
                ps.setInt(1, 4);
                ps.setString(2, "EMP002");
                ps.setString(3, "REPORT001");
                ps.setString(4, "Great manager - supportive and encouraging environment.");
                ps.setString(5, "DIRECT_REPORT");
                ps.setString(6, "2026-03-15");
                ps.addBatch();
                
                // Feedback 5
                ps.setInt(1, 5);
                ps.setString(2, "EMP003");
                ps.setString(3, "MGR002");
                ps.setString(4, "Good performer. Can improve communication with cross-functional teams.");
                ps.setString(5, "360_DEGREE");
                ps.setString(6, "2026-03-22");
                ps.addBatch();
                
                int[] inserted = ps.executeBatch();
                System.out.println("✓ Inserted " + inserted.length + " feedback records");
            }
        } catch (SQLException e) {
            System.out.println("ℹ  Feedback table may already exist or have constraints");
        }
    }

    private static void insertKPIs(Connection conn) throws SQLException {
        // Create KPIs table if not exists
        String createTable = "CREATE TABLE IF NOT EXISTS kpis ("
            + "kpi_id INTEGER PRIMARY KEY,"
            + "emp_id TEXT,"
            + "goal_id INTEGER,"
            + "kpi_name TEXT,"
            + "target_value REAL,"
            + "actual_value REAL,"
            + "unit TEXT)";
        
        try (Statement st = conn.createStatement()) {
            st.execute(createTable);
        } catch (SQLException e) {
            // Table might already exist, continue
        }
        
        String insertKPI = "INSERT INTO kpis (kpi_id, emp_id, goal_id, kpi_name, target_value, actual_value, unit) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(insertKPI)) {
            // KPI 1
            ps.setInt(1, 1);
            ps.setString(2, "EMP001");
            ps.setInt(3, 1);
            ps.setString(4, "Code Quality Score");
            ps.setFloat(5, 85.0f);
            ps.setFloat(6, 88.5f);
            ps.setString(7, "%");
            ps.addBatch();
            
            // KPI 2
            ps.setInt(1, 2);
            ps.setString(2, "EMP001");
            ps.setInt(3, 2);
            ps.setString(4, "Test Coverage");
            ps.setFloat(5, 80.0f);
            ps.setFloat(6, 85.0f);
            ps.setString(7, "%");
            ps.addBatch();
            
            // KPI 3
            ps.setInt(1, 3);
            ps.setString(2, "EMP002");
            ps.setInt(3, 3);
            ps.setString(4, "Team Productivity");
            ps.setFloat(5, 90.0f);
            ps.setFloat(6, 95.0f);
            ps.setString(7, "%");
            ps.addBatch();
            
            // KPI 4
            ps.setInt(1, 4);
            ps.setString(2, "EMP002");
            ps.setInt(3, 4);
            ps.setString(4, "Workflow Automation Completion");
            ps.setFloat(5, 100.0f);
            ps.setFloat(6, 100.0f);
            ps.setString(7, "%");
            ps.addBatch();
            
            // KPI 5
            ps.setInt(1, 5);
            ps.setString(2, "EMP003");
            ps.setInt(3, 5);
            ps.setString(4, "Customer Satisfaction Score");
            ps.setFloat(5, 95.0f);
            ps.setFloat(6, 96.5f);
            ps.setString(7, "%");
            ps.addBatch();
            
            int[] inserted = ps.executeBatch();
            System.out.println("✓ Inserted " + inserted.length + " KPIs");
        } catch (SQLException e) {
            System.out.println("ℹ  KPIs may already exist: " + e.getMessage());
        }
    }
}
