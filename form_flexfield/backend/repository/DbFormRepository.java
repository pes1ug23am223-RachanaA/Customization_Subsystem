package backend.repository;

import backend.dto.FieldDTO;
import backend.dto.FormDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CLASS: DbFormRepository
 * Subsystem: Customization — Form Designer
 *
 * PURPOSE:
 *   Real DB-backed implementation of IFormRepository.
 *   Connects to the shared HRMS SQLite database (hrms.db) using the
 *   DB team's schema: custom_forms + custom_fields tables.
 *
 * INTEGRATION:
 *   - Uses the same DB JAR (hrms-database-1.0-SNAPSHOT.jar) as all other subsystems
 *   - Tables: custom_forms (form_id, form_name, layout_type, module_id, created_date)
 *             custom_fields (field_id, field_name, field_type, form_id, is_mandatory, default_value)
 *
 * HOW TO ACTIVATE:
 *   In RepositoryFactory.java, change:
 *     return new MockFormRepository();
 *   to:
 *     return new DbFormRepository("jdbc:sqlite:hrms.db");
 *
 * DESIGN:
 *   - DIP: Implements IFormRepository — callers never see this class directly
 *   - OCP: Existing Mock stays untouched; this is a new class, not an edit
 */
public class DbFormRepository implements IFormRepository {

    private final String dbUrl;
    private Connection conn;

    public DbFormRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection(dbUrl);
            System.out.println("[DbFormRepository] Connected to DB: " + dbUrl);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("[DbFormRepository] ERROR: Cannot connect to " + dbUrl);
            System.err.println(e.getMessage());
        }
    }

    @Override
    public int createForm(String name, String layoutType) {
        String sql = "INSERT INTO custom_forms (form_name, layout_type, created_date) VALUES (?, ?, date('now'))";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, layoutType);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                System.out.println("[DbFormRepository] Created form '" + name + "' with id=" + id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("[DbFormRepository] createForm error: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public FormDTO getFormById(int formId) {
        String sql = "SELECT form_id, form_name, layout_type FROM custom_forms WHERE form_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, formId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                FormDTO form = new FormDTO(
                    rs.getInt("form_id"),
                    rs.getString("form_name"),
                    rs.getString("layout_type")
                );
                // Load fields
                for (FieldDTO f : getFieldsByForm(formId)) {
                    form.addField(f);
                }
                return form;
            }
        } catch (SQLException e) {
            System.err.println("[DbFormRepository] getFormById error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<FormDTO> getAllForms() {
        List<FormDTO> forms = new ArrayList<>();
        String sql = "SELECT form_id, form_name, layout_type FROM custom_forms ORDER BY form_id";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                FormDTO form = new FormDTO(
                    rs.getInt("form_id"),
                    rs.getString("form_name"),
                    rs.getString("layout_type")
                );
                for (FieldDTO f : getFieldsByForm(form.getFormId())) {
                    form.addField(f);
                }
                forms.add(form);
            }
        } catch (SQLException e) {
            System.err.println("[DbFormRepository] getAllForms error: " + e.getMessage());
        }
        return forms;
    }

    @Override
    public void updateForm(int formId, String newName) {
        String sql = "UPDATE custom_forms SET form_name = ? WHERE form_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, formId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DbFormRepository] updateForm error: " + e.getMessage());
        }
    }

    @Override
    public void deleteForm(int formId) {
        // Delete fields first (SQLite may not have FK cascade enabled)
        String delFields = "DELETE FROM custom_fields WHERE form_id = ?";
        String delForm   = "DELETE FROM custom_forms WHERE form_id = ?";
        try (PreparedStatement ps1 = conn.prepareStatement(delFields);
             PreparedStatement ps2 = conn.prepareStatement(delForm)) {
            ps1.setInt(1, formId);
            ps1.executeUpdate();
            ps2.setInt(1, formId);
            ps2.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DbFormRepository] deleteForm error: " + e.getMessage());
        }
    }

    public void deleteField(int formId, int fieldId) {
        String sql = "DELETE FROM custom_fields WHERE field_id = ? AND form_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fieldId);
            ps.setInt(2, formId);
            ps.executeUpdate();
            System.out.println("[DbFormRepository] Deleted field " + fieldId + " from form " + formId);
        } catch (SQLException e) {
            System.err.println("[DbFormRepository] deleteField error: " + e.getMessage());
        }
    }

    @Override
    public void addFieldToForm(int formId, FieldDTO field) {
        String sql = "INSERT INTO custom_fields (field_name, field_type, form_id, is_mandatory, default_value) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, field.getFieldName());
            ps.setString(2, field.getFieldType());
            ps.setInt(3, formId);
            ps.setBoolean(4, field.isMandatory());
            ps.setString(5, field.getDefaultValue());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DbFormRepository] addFieldToForm error: " + e.getMessage());
        }
    }

    @Override
    public void removeFieldFromForm(int formId, int fieldId) {
        String sql = "DELETE FROM custom_fields WHERE form_id = ? AND field_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, formId);
            ps.setInt(2, fieldId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DbFormRepository] removeFieldFromForm error: " + e.getMessage());
        }
    }

    @Override
    public List<FieldDTO> getFieldsByForm(int formId) {
        List<FieldDTO> fields = new ArrayList<>();
        String sql = "SELECT field_id, field_name, field_type, is_mandatory, default_value FROM custom_fields WHERE form_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, formId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                fields.add(new FieldDTO(
                    rs.getInt("field_id"),
                    rs.getString("field_name"),
                    rs.getString("field_type"),
                    rs.getBoolean("is_mandatory"),
                    rs.getString("default_value")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DbFormRepository] getFieldsByForm error: " + e.getMessage());
        }
        return fields;
    }

    /** Call on application shutdown */
    public void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException ignored) {}
    }
}
