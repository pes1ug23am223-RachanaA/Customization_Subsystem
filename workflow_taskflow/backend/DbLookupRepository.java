import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DbLookupRepository — Real SQLite impl for Lookup Customizer.
 * Uses custom_fields table with form_id=-2 as Lookup sentinel.
 * Schema: custom_fields(field_id, field_name, field_type, default_value, form_id, is_mandatory)
 *   field_name = lookup code (e.g. DEPARTMENT)
 *   field_type = "LOOKUP"
 *   default_value = comma-separated values (e.g. "HR,Finance,Engineering")
 *   form_id = -2 for all lookup records
 */
public class DbLookupRepository {

    private static final int SENTINEL = -2;
    private final String dbUrl;
    private Connection conn;

    public DbLookupRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
        seedDefaults();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(dbUrl);
            conn.setAutoCommit(true);
            System.out.println("[DbLookupRepository] Connected: " + dbUrl);
        } catch (Exception e) { System.err.println("[DbLookupRepository] " + e.getMessage()); }
    }

    private void seedDefaults() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM custom_fields WHERE form_id="+SENTINEL)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String[][] seeds = {
                    {"GENDER","Male,Female,Non-Binary"},
                    {"EMPLOYMENT_TYPE","Full-Time,Part-Time,Contract"},
                    {"DEPARTMENT","HR,Finance,Engineering,Sales"},
                    {"ONBOARDING_STAGE","Pre-Joining,Orientation,Probation,Confirmed"}
                };
                for (String[] s : seeds) save(s[0], Arrays.asList(s[1].split(",")));
                System.out.println("[DbLookupRepository] Seeded default lookups.");
            }
        } catch (SQLException e) { System.err.println("[DbLookupRepository] seed: " + e.getMessage()); }
    }

    public void save(String name, List<String> values) {
        try (PreparedStatement chk = conn.prepareStatement(
                "SELECT field_id FROM custom_fields WHERE field_name=? AND form_id="+SENTINEL)) {
            chk.setString(1, name);
            ResultSet rs = chk.executeQuery();
            if (rs.next()) {
                try (PreparedStatement upd = conn.prepareStatement(
                        "UPDATE custom_fields SET default_value=? WHERE field_name=? AND form_id="+SENTINEL)) {
                    upd.setString(1, String.join(",", values)); upd.setString(2, name); upd.executeUpdate();
                }
            } else {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO custom_fields(field_name,field_type,default_value,form_id,is_mandatory) VALUES(?,?,?,?,0)")) {
                    ins.setString(1, name); ins.setString(2, "LOOKUP");
                    ins.setString(3, String.join(",", values)); ins.setInt(4, SENTINEL); ins.executeUpdate();
                }
            }
            System.out.println("[DbLookupRepository] Saved lookup: " + name);
        } catch (SQLException e) { System.err.println("[DbLookupRepository] save: " + e.getMessage()); }
    }

    public void addValue(String lookupName, String newValue) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT default_value FROM custom_fields WHERE field_name=? AND form_id="+SENTINEL)) {
            ps.setString(1, lookupName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String cur = rs.getString(1);
                String updated = (cur == null || cur.isEmpty()) ? newValue : cur + "," + newValue;
                try (PreparedStatement upd = conn.prepareStatement(
                        "UPDATE custom_fields SET default_value=? WHERE field_name=? AND form_id="+SENTINEL)) {
                    upd.setString(1, updated); upd.setString(2, lookupName); upd.executeUpdate();
                }
            }
        } catch (SQLException e) { System.err.println("[DbLookupRepository] addValue: " + e.getMessage()); }
    }

    public List<LookupData> findAll() {
        List<LookupData> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT field_name,default_value FROM custom_fields WHERE form_id="+SENTINEL+" ORDER BY field_name")) {
            while (rs.next()) {
                String raw = rs.getString(2);
                List<String> vals = raw != null && !raw.isEmpty()
                    ? new ArrayList<>(Arrays.asList(raw.split(","))) : new ArrayList<>();
                list.add(new LookupData(rs.getString(1), vals));
            }
        } catch (SQLException e) { System.err.println("[DbLookupRepository] findAll: " + e.getMessage()); }
        return list;
    }

    public void deleteByName(String lookupName) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM custom_fields WHERE field_name=? AND form_id="+SENTINEL)) {
            ps.setString(1, lookupName);
            ps.executeUpdate();
            System.out.println("[DbLookupRepository] Deleted lookup: " + lookupName);
        } catch (SQLException e) { System.err.println("[DbLookupRepository] deleteByName: " + e.getMessage()); }
    }

    public static class LookupData {
        public String name; public List<String> values;
        public LookupData(String name, List<String> values) { this.name=name; this.values=values; }
    }
}
