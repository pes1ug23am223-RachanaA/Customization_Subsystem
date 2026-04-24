import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DbModuleRepository — Flat-package SQLite impl for Module Customizer.
 * Table: custom_modules(module_id, module_name, module_type, is_enabled)
 */
public class DbModuleRepository {

    private final String dbUrl;
    private Connection conn;

    public DbModuleRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
        seedDefaults();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(dbUrl);
            conn.setAutoCommit(true);
            System.out.println("[DbModuleRepository] Connected: " + dbUrl);
        } catch (Exception e) { System.err.println("[DbModuleRepository] " + e.getMessage()); }
    }

    private void seedDefaults() {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM custom_modules");
            if (rs.next() && rs.getInt(1) == 0) {
                String[][] rows = {
                    {"Recruitment","Core","1"},{"Payroll Processing","Core","1"},
                    {"Leave Management","Core","1"},{"Attendance","Core","1"},
                    {"Performance Appraisal","Extension","0"},{"Expense Management","Extension","1"},
                    {"Onboarding","Extension","1"},{"Succession Planning","Extension","0"}
                };
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO custom_modules(module_name,module_type,is_enabled) VALUES(?,?,?)")) {
                    for (String[] r : rows) { ps.setString(1,r[0]);ps.setString(2,r[1]);ps.setInt(3,Integer.parseInt(r[2]));ps.addBatch(); }
                    ps.executeBatch();
                }
                System.out.println("[DbModuleRepository] Seeded 8 default modules.");
            }
        } catch (SQLException e) { System.err.println("[DbModuleRepository] seed: " + e.getMessage()); }
    }

    public List<ModuleData> findAll() {
        List<ModuleData> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT module_id,module_name,module_type,is_enabled FROM custom_modules ORDER BY module_id")) {
            while (rs.next())
                list.add(new ModuleData(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4)==1));
        } catch (SQLException e) { System.err.println("[DbModuleRepository] findAll: " + e.getMessage()); }
        return list;
    }

    public void updateStatus(int moduleId, boolean enabled) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE custom_modules SET is_enabled=? WHERE module_id=?")) {
            ps.setInt(1, enabled?1:0); ps.setInt(2, moduleId); ps.executeUpdate();
            System.out.println("[DbModuleRepository] Module "+moduleId+" → "+(enabled?"ENABLED":"DISABLED"));
        } catch (SQLException e) { System.err.println("[DbModuleRepository] updateStatus: " + e.getMessage()); }
    }

    public static class ModuleData {
        public int moduleId; public String moduleName, moduleType; public boolean isEnabled;
        public ModuleData(int id, String name, String type, boolean enabled) { moduleId=id;moduleName=name;moduleType=type;isEnabled=enabled; }
    }
}
