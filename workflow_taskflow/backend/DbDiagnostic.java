import java.sql.*;

public class DbDiagnostic {
    public static void main(String[] args) {
        String dbUrl = "jdbc:sqlite:hrms.db";
        System.out.println("[DbDiagnostic] Checking EIT records in database...");
        
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(dbUrl);
            System.out.println("[DbDiagnostic] Connected to: " + dbUrl);
            
            // Check if custom_fields table exists
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "custom_fields", null);
            if (!tables.next()) {
                System.err.println("[DbDiagnostic] ERROR: custom_fields table does not exist!");
                return;
            }
            System.out.println("[DbDiagnostic] custom_fields table found");
            
            // List all EIT records (form_id = -1)
            System.out.println("\n[DbDiagnostic] EIT Records (form_id = -1):");
            System.out.println("───────────────────────────────────────────");
            String sql = "SELECT field_id, field_name, field_type, form_id FROM custom_fields WHERE form_id = -1 ORDER BY field_id";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                boolean hasRows = false;
                while (rs.next()) {
                    hasRows = true;
                    System.out.println("  ID: " + rs.getInt("field_id") + 
                                     " | Name: " + rs.getString("field_name") + 
                                     " | Type: " + rs.getString("field_type") + 
                                     " | FormID: " + rs.getInt("form_id"));
                }
                if (!hasRows) {
                    System.out.println("  (No EIT records found)");
                }
            }
            
            // Test delete on first EIT record
            System.out.println("\n[DbDiagnostic] Testing delete operation...");
            System.out.println("───────────────────────────────────────────");
            try (Statement st = conn.createStatement(); 
                 ResultSet rs = st.executeQuery("SELECT field_id FROM custom_fields WHERE form_id = -1 LIMIT 1")) {
                if (rs.next()) {
                    int testId = rs.getInt("field_id");
                    System.out.println("  Test deleting field_id: " + testId);
                    
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM custom_fields WHERE field_id = ? AND form_id = -1");
                    ps.setInt(1, testId);
                    int rowsAffected = ps.executeUpdate();
                    System.out.println("  Rows affected: " + rowsAffected);
                    
                    if (rowsAffected > 0) {
                        System.out.println("  ✓ Delete successful - rolling back for safety");
                        conn.rollback();
                    } else {
                        System.err.println("  ✗ Delete had no effect!");
                    }
                } else {
                    System.out.println("  (No EIT records to test)");
                }
            }
            
            conn.close();
            System.out.println("\n[DbDiagnostic] Diagnostic complete");
        } catch (Exception e) {
            System.err.println("[DbDiagnostic] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
