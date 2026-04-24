import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DbEITRepository — Real SQLite impl for EIT Handler.
 * Uses custom_fields table with form_id=-1 as EIT sentinel.
 * Schema: custom_fields(field_id, field_name, field_type, default_value, form_id, is_mandatory)
 *   field_type = data type (Text/Number/Date)
 *   default_value = encoded "context|empId" payload
 *   form_id = -1 for all EIT records
 */
public class DbEITRepository {

    private static final int SENTINEL = -1;
    private final String dbUrl;
    private Connection conn;

    public DbEITRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(dbUrl);
            conn.setAutoCommit(true);
            System.out.println("[DbEITRepository] Connected: " + dbUrl);
        } catch (Exception e) { System.err.println("[DbEITRepository] " + e.getMessage()); }
    }

    public void save(String name, String type, String context, String linkedEmployee) {
        String sql = "INSERT INTO custom_fields(field_name,field_type,default_value,form_id,is_mandatory) VALUES(?,?,?,?,0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, type != null ? type : "Text");
            ps.setString(3, encodePayload(context, linkedEmployee));
            ps.setInt(4, SENTINEL);
            ps.executeUpdate();
            System.out.println("[DbEITRepository] Saved EIT: " + name);
        } catch (SQLException e) { System.err.println("[DbEITRepository] save: " + e.getMessage()); }
    }

    public List<EITData> findAll() {
        List<EITData> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT field_id,field_name,field_type,default_value FROM custom_fields WHERE form_id="+SENTINEL+" ORDER BY field_name")) {
            while (rs.next()) {
                String[] decoded = decodePayload(rs.getString(4));
                list.add(new EITData(rs.getInt(1), rs.getString(2), rs.getString(3), decoded[0], decoded[1]));
            }
        } catch (SQLException e) { System.err.println("[DbEITRepository] findAll: " + e.getMessage()); }
        return list;
    }

    public void deleteByName(String name) throws SQLException {
        if (conn == null) {
            System.err.println("[DbEITRepository] deleteByName: Connection is null");
            throw new SQLException("Database connection not initialized");
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM custom_fields WHERE field_name=? AND form_id="+SENTINEL)) {
            ps.setString(1, name);
            int rowsAffected = ps.executeUpdate();
            System.out.println("[DbEITRepository] deleteByName: Deleted " + rowsAffected + " rows for: " + name);
            if (rowsAffected == 0) {
                System.err.println("[DbEITRepository] WARNING: No rows deleted for name: " + name);
            }
        } catch (SQLException e) { 
            System.err.println("[DbEITRepository] deleteByName error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void deleteById(int eitId) throws SQLException {
        if (conn == null) {
            System.err.println("[DbEITRepository] deleteById: Connection is null");
            throw new SQLException("Database connection not initialized");
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM custom_fields WHERE field_id=? AND form_id="+SENTINEL)) {
            ps.setInt(1, eitId);
            int rowsAffected = ps.executeUpdate();
            System.out.println("[DbEITRepository] deleteById: Deleted " + rowsAffected + " rows for ID: " + eitId);
            if (rowsAffected == 0) {
                System.err.println("[DbEITRepository] WARNING: No rows deleted for ID: " + eitId);
            }
        } catch (SQLException e) { 
            System.err.println("[DbEITRepository] deleteById error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private String encodePayload(String context, String linkedEmployee) {
        String safeContext = context != null && !context.trim().isEmpty() ? context.trim() : "EMPLOYEE";
        String safeEmployee = linkedEmployee != null ? linkedEmployee.trim() : "";
        return safeEmployee.isEmpty() ? safeContext : safeContext + "|" + safeEmployee;
    }

    private String[] decodePayload(String payload) {
        if (payload == null || payload.trim().isEmpty()) return new String[]{"EMPLOYEE", ""};
        String[] parts = payload.split("\\|", 2);
        if (parts.length == 1) return new String[]{parts[0], ""};
        return new String[]{parts[0], parts[1]};
    }

    public static class EITData {
        public int fieldId;
        public String name;
        public String type;
        public String context;
        public String linkedEmployee;
        public EITData(int id, String name, String type, String ctx, String linkedEmployee) {
            fieldId = id;
            this.name = name;
            this.type = type;
            this.context = ctx;
            this.linkedEmployee = linkedEmployee;
        }
    }
}
