package backend.repository;

import backend.dto.FlexFieldDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CLASS: DbFlexfieldRepository
 * Subsystem: Customization — Flexfield Manager
 *
 * PURPOSE:
 *   Real DB-backed implementation of IFlexfieldRepository.
 *   Reads/writes the HRMS SQLite database hrms.db using the
 *   DB team's schema: custom_fields table (re-used for flexfields).
 *
 *   Flexfields are stored in custom_fields with:
 *     field_name  = flexfield name
 *     field_type  = KEY | DESCRIPTIVE
 *     default_value = comma-separated segment values (e.g. "Engineering,HR,Finance")
 *     form_id     = NULL (flexfields are global, not bound to a form)
 *
 * HOW TO ACTIVATE:
 *   In RepositoryFactory.java, change:
 *     return new MockFlexfieldRepository();
 *   to:
 *     return new DbFlexfieldRepository("jdbc:sqlite:hrms.db");
 *
 * DESIGN:
 *   - DIP: Implements IFlexfieldRepository — callers never see this class
 *   - OCP: Existing Mock stays untouched
 *   - Matches DB team's CustomizationRepositoryImpl signature exactly
 */
public class DbFlexfieldRepository implements IFlexfieldRepository {

    private final String dbUrl;
    private Connection conn;

    public DbFlexfieldRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection(dbUrl);
            System.out.println("[DbFlexfieldRepository] Connected to DB: " + dbUrl);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("[DbFlexfieldRepository] ERROR: Cannot connect to " + dbUrl);
            System.err.println(e.getMessage());
        }
    }

    /** Flexfields have no form_id (NULL = global flexfield) */
    @Override
    public void addField(String name, String type, String segments) {
        String sql = "INSERT INTO custom_fields (field_name, field_type, default_value, form_id, is_mandatory) VALUES (?,?,?,NULL,0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, segments);
            ps.executeUpdate();
            System.out.println("[DbFlexfieldRepository] Added flexfield: " + name);
        } catch (SQLException e) {
            System.err.println("[DbFlexfieldRepository] addField error: " + e.getMessage());
        }
    }

    @Override
    public FlexFieldDTO getFieldById(int fieldId) {
        String sql = "SELECT field_id, field_name, field_type, default_value FROM custom_fields WHERE field_id = ? AND form_id IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fieldId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new FlexFieldDTO(
                    rs.getInt("field_id"),
                    rs.getString("field_name"),
                    rs.getString("field_type"),
                    rs.getString("default_value")
                );
            }
        } catch (SQLException e) {
            System.err.println("[DbFlexfieldRepository] getFieldById error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<FlexFieldDTO> getAllFlexfields() {
        List<FlexFieldDTO> list = new ArrayList<>();
        String sql = "SELECT field_id, field_name, field_type, default_value FROM custom_fields WHERE form_id IS NULL ORDER BY field_id";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new FlexFieldDTO(
                    rs.getInt("field_id"),
                    rs.getString("field_name"),
                    rs.getString("field_type"),
                    rs.getString("default_value")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DbFlexfieldRepository] getAllFlexfields error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void removeField(int fieldId) {
        String sql = "DELETE FROM custom_fields WHERE field_id = ? AND form_id IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fieldId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DbFlexfieldRepository] removeField error: " + e.getMessage());
        }
    }

    /**
     * Replaces a single segment (by index) in the comma-separated segments string.
     * Matches DB team's CustomizationRepositoryImpl signature: (fieldId, segmentIndex, value).
     */
    @Override
    public void updateFieldSegment(int fieldId, int segmentIndex, String value) {
        FlexFieldDTO existing = getFieldById(fieldId);
        if (existing == null) return;

        String[] segs = existing.getSegments() != null
            ? existing.getSegments().split(",") : new String[]{};

        if (segmentIndex >= 0 && segmentIndex < segs.length) {
            segs[segmentIndex] = value.trim();
        }

        String newSegments = String.join(",", segs);
        String sql = "UPDATE custom_fields SET default_value = ? WHERE field_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newSegments);
            ps.setInt(2, fieldId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DbFlexfieldRepository] updateFieldSegment error: " + e.getMessage());
        }
    }

    @Override
    public boolean validateField(int fieldId) {
        return getFieldById(fieldId) != null;
    }

    /**
     * Returns valid values for a flexfield (comma-separated segments).
     * Used by Onboarding integration: IEmployeeIntegration.assignRole() validates
     * role strings against lookup.getValues("DEPARTMENT").
     */
    @Override
    public List<String> getValues(int fieldId) {
        FlexFieldDTO field = getFieldById(fieldId);
        if (field == null || field.getSegments() == null || field.getSegments().isBlank()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String s : field.getSegments().split(",")) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }

    /** Call on application shutdown */
    public void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException ignored) {}
    }
}
