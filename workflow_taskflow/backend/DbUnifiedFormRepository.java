import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified form repository used by ApiServer so Form Designer shares the same
 * live SQLite database contract as the other customization modules.
 */
public class DbUnifiedFormRepository {

    private final String dbUrl;
    private Connection conn;

    public DbUnifiedFormRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(dbUrl);
            conn.setAutoCommit(true);
            System.out.println("[DbUnifiedFormRepository] Connected: " + dbUrl);
        } catch (Exception e) {
            System.err.println("[DbUnifiedFormRepository] " + e.getMessage());
        }
    }

    public int createForm(String name, String layoutType) {
        String sql = "INSERT INTO custom_forms(form_name,layout_type,created_date) VALUES(?,?,date('now'))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, layoutType);
            ps.executeUpdate();
            return findLatestFormId(name, layoutType);
        } catch (SQLException e) {
            System.err.println("[DbUnifiedFormRepository] createForm: " + e.getMessage());
        }
        return -1;
    }

    public List<FormData> findAll() {
        List<FormData> forms = new ArrayList<>();
        String sql = "SELECT form_id, form_name, layout_type, module_id, created_date FROM custom_forms ORDER BY form_id";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                FormData form = new FormData(
                    rs.getInt("form_id"),
                    rs.getString("form_name"),
                    rs.getString("layout_type"),
                    rs.getInt("module_id"),
                    rs.getString("created_date")
                );
                form.fields = findFieldsByFormId(form.formId);
                forms.add(form);
            }
        } catch (SQLException e) {
            System.err.println("[DbUnifiedFormRepository] findAll: " + e.getMessage());
        }
        return forms;
    }

    public boolean formExists(int formId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM custom_forms WHERE form_id = ?")) {
            ps.setInt(1, formId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[DbUnifiedFormRepository] formExists: " + e.getMessage());
            return false;
        }
    }

    public int addField(int formId, String fieldName, String fieldType, boolean mandatory, String defaultValue) {
        String sql = "INSERT INTO custom_fields(field_name,field_type,form_id,is_mandatory,default_value) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fieldName);
            ps.setString(2, fieldType);
            ps.setInt(3, formId);
            ps.setBoolean(4, mandatory);
            ps.setString(5, defaultValue);
            ps.executeUpdate();
            return findLatestFieldId(formId, fieldName);
        } catch (SQLException e) {
            System.err.println("[DbUnifiedFormRepository] addField: " + e.getMessage());
        }
        return -1;
    }

    public void deleteForm(int formId) {
        try (PreparedStatement fieldPs = conn.prepareStatement(
                    "DELETE FROM custom_fields WHERE form_id = ?");
             PreparedStatement formPs = conn.prepareStatement(
                    "DELETE FROM custom_forms WHERE form_id = ?")) {
            fieldPs.setInt(1, formId);
            fieldPs.executeUpdate();
            formPs.setInt(1, formId);
            formPs.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DbUnifiedFormRepository] deleteForm: " + e.getMessage());
        }
    }

    public void deleteField(int formId, int fieldId) {
        try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM custom_fields WHERE field_id = ? AND form_id = ?")) {
            ps.setInt(1, fieldId);
            ps.setInt(2, formId);
            ps.executeUpdate();
            System.out.println("[DbUnifiedFormRepository] Deleted field " + fieldId + " from form " + formId);
        } catch (SQLException e) {
            System.err.println("[DbUnifiedFormRepository] deleteField: " + e.getMessage());
        }
    }

    private List<FieldData> findFieldsByFormId(int formId) {
        List<FieldData> fields = new ArrayList<>();
        String sql = "SELECT field_id, field_name, field_type, is_mandatory, default_value FROM custom_fields WHERE form_id = ? ORDER BY field_id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, formId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                fields.add(new FieldData(
                    rs.getInt("field_id"),
                    rs.getString("field_name"),
                    rs.getString("field_type"),
                    rs.getBoolean("is_mandatory"),
                    rs.getString("default_value")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DbUnifiedFormRepository] findFieldsByFormId: " + e.getMessage());
        }
        return fields;
    }

    private int findLatestFormId(String name, String layoutType) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT form_id FROM custom_forms WHERE form_name = ? AND layout_type = ? ORDER BY form_id DESC LIMIT 1")) {
            ps.setString(1, name);
            ps.setString(2, layoutType);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    private int findLatestFieldId(int formId, String fieldName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT field_id FROM custom_fields WHERE form_id = ? AND field_name = ? ORDER BY field_id DESC LIMIT 1")) {
            ps.setInt(1, formId);
            ps.setString(2, fieldName);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    public static class FormData {
        public int formId;
        public String formName;
        public String layoutType;
        public int moduleId;
        public String createdDate;
        public List<FieldData> fields = new ArrayList<>();

        public FormData(int formId, String formName, String layoutType, int moduleId, String createdDate) {
            this.formId = formId;
            this.formName = formName;
            this.layoutType = layoutType;
            this.moduleId = moduleId;
            this.createdDate = createdDate;
        }
    }

    public static class FieldData {
        public int fieldId;
        public String fieldName;
        public String fieldType;
        public boolean mandatory;
        public String defaultValue;

        public FieldData(int fieldId, String fieldName, String fieldType, boolean mandatory, String defaultValue) {
            this.fieldId = fieldId;
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.mandatory = mandatory;
            this.defaultValue = defaultValue;
        }
    }
}
