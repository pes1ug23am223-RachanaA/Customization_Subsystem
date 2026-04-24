package backend.repository;

import backend.dto.EITDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CLASS: DbEITRepository
 * Subsystem: Customization — EIT Handler
 *
 * PURPOSE:
 *   Real DB-backed implementation of IEITRepository.
 *   Maps EITs to the DB team's schema — no dedicated eit table exists,
 *   so EITs are stored in custom_fields with field_type = 'EIT'
 *   and form_id = -1 (sentinel value for EIT records).
 *
 *   DB Schema used:
 *     custom_fields(field_id, field_name, field_type, default_value, form_id, is_mandatory)
 *     field_type = EIT context (EMPLOYEE, JOB, POSITION)
 *     default_value = EIT data type (Text, Number, Date)
 *     form_id = -1 for EITs (distinguishes from form fields and flexfields)
 *
 * HOW TO ACTIVATE:
 *   The ApiServer already manages EIT data in-memory via eitStore[].
 *   This class is available for the eit_lookup module's own IEITRepository usage.
 *
 * NOTE:
 *   The Workflow/ApiServer EIT endpoint uses its own in-memory eitStore for
 *   the HTTP API. This repository backs the eit_lookup/backend service layer.
 */
public class DbEITRepository implements IEITRepository {

    private static final int EIT_FORM_ID_SENTINEL = -1;
    private final String dbUrl;
    private Connection conn;

    public DbEITRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection(dbUrl);
            System.out.println("[DbEITRepository] Connected to DB: " + dbUrl);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("[DbEITRepository] ERROR: Cannot connect to " + dbUrl);
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void save(EITDTO eit) {
        // form_id = -1 is our EIT sentinel; field_type = EIT context, default_value = data type
        String sql = "INSERT INTO custom_fields (field_name, field_type, default_value, form_id, is_mandatory) VALUES (?,?,?,?,0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eit.getName());
            ps.setString(2, eit.getType());  // e.g. "Text", "Number", "Date"
            ps.setString(3, "EMPLOYEE");      // default context
            ps.setInt(4, EIT_FORM_ID_SENTINEL);
            ps.executeUpdate();
            System.out.println("[DbEITRepository] Saved EIT: " + eit.getName());
        } catch (SQLException e) {
            System.err.println("[DbEITRepository] save error: " + e.getMessage());
        }
    }

    @Override
    public List<EITDTO> findAll() {
        List<EITDTO> list = new ArrayList<>();
        String sql = "SELECT field_id, field_name, field_type FROM custom_fields WHERE form_id = " + EIT_FORM_ID_SENTINEL;
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int fieldId = rs.getInt("field_id");
                String fieldName = rs.getString("field_name");
                String fieldType = rs.getString("field_type");
                EITDTO eit = new EITDTO(fieldId, fieldName, fieldType);
                list.add(eit);
            }
        } catch (SQLException e) {
            System.err.println("[DbEITRepository] findAll error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void deleteById(int fieldId) throws SQLException {
        if (conn == null) {
            System.err.println("[DbEITRepository] deleteById: Connection is null");
            throw new SQLException("Connection is null");
        }
        String sql = "DELETE FROM custom_fields WHERE field_id = ? AND form_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fieldId);
            ps.setInt(2, EIT_FORM_ID_SENTINEL);
            int rowsAffected = ps.executeUpdate();
            System.out.println("[DbEITRepository] deleteById: Deleted " + rowsAffected + " rows for ID: " + fieldId);
            if (rowsAffected == 0) {
                System.err.println("[DbEITRepository] WARNING: No rows deleted for ID: " + fieldId);
            }
        } catch (SQLException e) {
            System.err.println("[DbEITRepository] deleteById error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void deleteByName(String name) throws SQLException {
        if (conn == null) {
            System.err.println("[DbEITRepository] deleteByName: Connection is null");
            throw new SQLException("Connection is null");
        }
        String sql = "DELETE FROM custom_fields WHERE field_name = ? AND form_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, EIT_FORM_ID_SENTINEL);
            int rowsAffected = ps.executeUpdate();
            System.out.println("[DbEITRepository] deleteByName: Deleted " + rowsAffected + " rows for name: " + name);
            if (rowsAffected == 0) {
                System.err.println("[DbEITRepository] WARNING: No rows deleted for name: " + name);
            }
        } catch (SQLException e) {
            System.err.println("[DbEITRepository] deleteByName error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException ignored) {}
    }
}
