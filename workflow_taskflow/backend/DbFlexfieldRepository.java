import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DbFlexfieldRepository — SQLite implementation for Flexfield Manager.
 * Flat-package version used by ApiServer directly.
 *
 * Maps to: custom_fields table where form_id IS NULL = flexfields
 * Schema: custom_fields(field_id, field_name, field_type, default_value, form_id, is_mandatory)
 *   field_name    = flexfield name (e.g. "Project Code")
 *   field_type    = KEY | DESCRIPTIVE
 *   default_value = segment count as string (e.g. "3")
 *   form_id       = NULL for flexfields (distinguishes from EITs/Lookups/FormFields)
 */
public class DbFlexfieldRepository {

    private final String dbUrl;
    private Connection conn;

    public DbFlexfieldRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(dbUrl);
            conn.setAutoCommit(true);
            System.out.println("[DbFlexfieldRepository] Connected: " + dbUrl);
        } catch (Exception e) {
            System.err.println("[DbFlexfieldRepository] " + e.getMessage());
        }
    }

    /** Save a new flexfield (form_id = NULL distinguishes from form fields) */
    public void save(String name, String type, String segments) {
        String sql = "INSERT INTO custom_fields(field_name,field_type,default_value,form_id,is_mandatory) VALUES(?,?,?,NULL,0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, segments == null || segments.isEmpty() ? "" : segments);
            ps.executeUpdate();
            System.out.println("[DbFlexfieldRepository] Saved flexfield: " + name);
        } catch (SQLException e) {
            System.err.println("[DbFlexfieldRepository] save: " + e.getMessage());
        }
    }

    /** Find all flexfields (form_id IS NULL) */
    public List<FlexData> findAll() {
        List<FlexData> list = new ArrayList<>();
        String sql = "SELECT field_id,field_name,field_type,default_value FROM custom_fields WHERE form_id IS NULL ORDER BY field_id";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String segmentStr = rs.getString("default_value");
                list.add(new FlexData(rs.getInt("field_id"), rs.getString("field_name"), rs.getString("field_type"), segmentStr));
            }
        } catch (SQLException e) {
            System.err.println("[DbFlexfieldRepository] findAll: " + e.getMessage());
        }
        return list;
    }

    /** Update flexfield segments */
    public void updateSegments(int fieldId, String segments) {
        String sql = "UPDATE custom_fields SET default_value = ? WHERE field_id = ? AND form_id IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, segments == null || segments.isEmpty() ? "" : segments);
            ps.setInt(2, fieldId);
            ps.executeUpdate();
            System.out.println("[DbFlexfieldRepository] Updated segments for flexfield: " + fieldId);
        } catch (SQLException e) {
            System.err.println("[DbFlexfieldRepository] updateSegments: " + e.getMessage());
        }
    }

    /** Delete flexfield by ID */
    public void deleteById(int fieldId) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM custom_fields WHERE field_id=? AND form_id IS NULL")) {
            ps.setInt(1, fieldId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DbFlexfieldRepository] delete: " + e.getMessage());
        }
    }

    public static class FlexData {
        public int fieldId;
        public String name, type, segments;
        public FlexData(int id, String name, String type, String segs) {
            fieldId = id; this.name = name; this.type = type; segments = segs;
        }
    }
}
