package backend.repository;

import backend.dto.LookupDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CLASS: DbLookupRepository
 * Subsystem: Customization — Lookup Customizer
 *
 * PURPOSE:
 *   Real DB-backed implementation of ILookupRepository.
 *   Maps Lookups to the DB team's schema — stored in custom_fields with
 *   field_type = 'LOOKUP' and form_id = -2 (sentinel for lookup records).
 *   The default_value column stores comma-separated values.
 *
 *   Example row:
 *     field_name   = "DEPARTMENT"
 *     field_type   = "LOOKUP"
 *     default_value = "HR,Finance,Engineering,Sales"
 *     form_id      = -2
 *
 * NOTE:
 *   The Workflow/ApiServer Lookup endpoint uses its own in-memory lookupStore[].
 *   This class backs the eit_lookup/backend service layer's ILookupRepository.
 */
public class DbLookupRepository implements ILookupRepository {

    private static final int LOOKUP_FORM_ID_SENTINEL = -2;
    private final String dbUrl;
    private Connection conn;

    public DbLookupRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection(dbUrl);
            System.out.println("[DbLookupRepository] Connected to DB: " + dbUrl);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("[DbLookupRepository] ERROR: Cannot connect to " + dbUrl);
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void save(LookupDTO lookup) {
        // Check if already exists — update instead of insert
        String check = "SELECT field_id FROM custom_fields WHERE field_name = ? AND form_id = " + LOOKUP_FORM_ID_SENTINEL;
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, lookup.getName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Update existing
                String update = "UPDATE custom_fields SET default_value = ? WHERE field_name = ? AND form_id = " + LOOKUP_FORM_ID_SENTINEL;
                try (PreparedStatement upd = conn.prepareStatement(update)) {
                    upd.setString(1, String.join(",", lookup.getValues()));
                    upd.setString(2, lookup.getName());
                    upd.executeUpdate();
                }
            } else {
                // Insert new
                String insert = "INSERT INTO custom_fields (field_name, field_type, default_value, form_id, is_mandatory) VALUES (?,?,?,?,0)";
                try (PreparedStatement ins = conn.prepareStatement(insert)) {
                    ins.setString(1, lookup.getName());
                    ins.setString(2, "LOOKUP");
                    ins.setString(3, String.join(",", lookup.getValues()));
                    ins.setInt(4, LOOKUP_FORM_ID_SENTINEL);
                    ins.executeUpdate();
                }
            }
            System.out.println("[DbLookupRepository] Saved lookup: " + lookup.getName());
        } catch (SQLException e) {
            System.err.println("[DbLookupRepository] save error: " + e.getMessage());
        }
    }

    @Override
    public List<LookupDTO> findAll() {
        List<LookupDTO> list = new ArrayList<>();
        String sql = "SELECT field_name, default_value FROM custom_fields WHERE form_id = " + LOOKUP_FORM_ID_SENTINEL + " ORDER BY field_name";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String raw = rs.getString("default_value");
                List<String> values = raw != null
                    ? Arrays.asList(raw.split(","))
                    : new ArrayList<>();
                list.add(new LookupDTO(rs.getString("field_name"), values));
            }
        } catch (SQLException e) {
            System.err.println("[DbLookupRepository] findAll error: " + e.getMessage());
        }
        return list;
    }

    public void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException ignored) {}
    }
}
