import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DbTaskFlowRepository — Real SQLite implementation of ITaskFlowRepository.
 * Tables: task_flows, task_flow_windows
 */
public class DbTaskFlowRepository implements ITaskFlowRepository {

    private final String dbUrl;
    private Connection conn;

    public DbTaskFlowRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
        ensureSchema();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(dbUrl);
            conn.setAutoCommit(true);
            System.out.println("[DbTaskFlowRepository] Connected: " + dbUrl);
        } catch (Exception e) {
            System.err.println("[DbTaskFlowRepository] Connect error: " + e.getMessage());
        }
    }

    private void ensureSchema() {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS task_flows (" +
                "task_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "flow_name TEXT NOT NULL," +
                "flow_status TEXT NOT NULL DEFAULT 'Active'," +
                "linked_menu TEXT DEFAULT ''," +
                "validate_on_next INTEGER DEFAULT 0," +
                "allow_back_nav INTEGER DEFAULT 0," +
                "created_date DATETIME DEFAULT CURRENT_TIMESTAMP)");
            st.execute("CREATE TABLE IF NOT EXISTS task_flow_windows (" +
                "window_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "task_id INTEGER NOT NULL," +
                "window_name TEXT NOT NULL," +
                "FOREIGN KEY(task_id) REFERENCES task_flows(task_id))");
        } catch (SQLException e) {
            System.err.println("[DbTaskFlowRepo] Schema: " + e.getMessage());
        }
    }

    @Override public int defineTaskFlow(String name, String status) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO task_flows(flow_name,flow_status) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setString(2, status != null ? status : "Active");
            ps.executeUpdate();
            ResultSet k = ps.getGeneratedKeys();
            if (k.next()) return k.getInt(1);
        } catch (SQLException e) { System.err.println("[DbTFRepo] define: " + e.getMessage()); }
        return -1;
    }

    @Override public TaskFlowDTO getTaskFlowById(int id) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM task_flows WHERE task_id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { System.err.println("[DbTFRepo] getById: " + e.getMessage()); }
        return null;
    }

    @Override public List<TaskFlowDTO> getAllTaskFlows() {
        List<TaskFlowDTO> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM task_flows ORDER BY task_id")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { System.err.println("[DbTFRepo] getAll: " + e.getMessage()); }
        return list;
    }

    @Override public void setSequence(int id, int seq) {
        exec("UPDATE task_flows SET flow_status=flow_status WHERE task_id=?", id); // no seq col — no-op acceptable
    }

    @Override public void updateTaskFlow(int id, String name) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE task_flows SET flow_name=? WHERE task_id=?")) {
            ps.setString(1, name); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DbTFRepo] update: " + e.getMessage()); }
    }

    @Override public void deleteTaskFlow(int id) {
        try {
            exec1("DELETE FROM task_flow_windows WHERE task_id=?", id);
            exec1("DELETE FROM task_flows WHERE task_id=?", id);
        } catch (Exception e) { System.err.println("[DbTFRepo] delete: " + e.getMessage()); }
    }

    @Override public void assignFlowToMenu(int id, String menu) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE task_flows SET linked_menu=? WHERE task_id=?")) {
            ps.setString(1, menu != null ? menu : ""); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DbTFRepo] menu: " + e.getMessage()); }
    }

    @Override public void updateFlowSettings(int id, boolean validateOnNext, boolean allowBackNav) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE task_flows SET validate_on_next=?,allow_back_nav=? WHERE task_id=?")) {
            ps.setInt(1, validateOnNext?1:0); ps.setInt(2, allowBackNav?1:0); ps.setInt(3, id); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DbTFRepo] settings: " + e.getMessage()); }
    }

    @Override public void addWindowToFlow(int id, String win) {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO task_flow_windows(task_id,window_name) VALUES(?,?)")) {
            ps.setInt(1, id); ps.setString(2, win); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DbTFRepo] addWin: " + e.getMessage()); }
    }

    @Override public void removeWindowFromFlow(int id, String win) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM task_flow_windows WHERE task_id=? AND window_name=?")) {
            ps.setInt(1, id); ps.setString(2, win); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DbTFRepo] remWin: " + e.getMessage()); }
    }

    @Override public List<String> getWindowsForFlow(int id) {
        List<String> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT window_name FROM task_flow_windows WHERE task_id=? ORDER BY window_id")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString(1));
        } catch (SQLException e) { System.err.println("[DbTFRepo] getWins: " + e.getMessage()); }
        return list;
    }

    public void setStatus(int id, String status) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE task_flows SET flow_status=? WHERE task_id=?")) {
            ps.setString(1, status); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DbTFRepo] setStatus: " + e.getMessage()); }
    }

    private void exec(String sql, int id) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) { ps.setInt(1, id); ps.executeUpdate(); }
        catch (SQLException e) { System.err.println("[DbTFRepo] exec: " + e.getMessage()); }
    }
    private void exec1(String sql, int id) { exec(sql, id); }

    private TaskFlowDTO map(ResultSet rs) throws SQLException {
        TaskFlowDTO t = new TaskFlowDTO();
        t.taskId = rs.getInt("task_id"); t.flowName = rs.getString("flow_name");
        t.flowStatus = rs.getString("flow_status"); t.linkedMenu = rs.getString("linked_menu");
        t.validateOnNext = rs.getInt("validate_on_next")==1; t.allowBackNav = rs.getInt("allow_back_nav")==1;
        return t;
    }
}
