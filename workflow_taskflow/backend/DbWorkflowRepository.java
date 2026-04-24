import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DbWorkflowRepository — Real SQLite implementation of IWorkflowRepository.
 * Tables: workflows, workflow_steps
 */
public class DbWorkflowRepository implements IWorkflowRepository {

    private final String dbUrl;
    private Connection conn;

    public DbWorkflowRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
        ensureSchema();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(dbUrl);
            conn.setAutoCommit(true);
            System.out.println("[DbWorkflowRepository] Connected: " + dbUrl);
        } catch (Exception e) {
            System.err.println("[DbWorkflowRepository] Connect error: " + e.getMessage());
        }
    }

    private void ensureSchema() {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS workflows (" +
                "workflow_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "workflow_name TEXT NOT NULL UNIQUE," +
                "current_status TEXT DEFAULT 'Active'," +
                "assigned_to TEXT DEFAULT '')");
            st.execute("CREATE TABLE IF NOT EXISTS workflow_steps (" +
                "step_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "workflow_id INTEGER NOT NULL," +
                "step_name TEXT NOT NULL," +
                "assignee TEXT DEFAULT ''," +
                "escalation_hours INTEGER DEFAULT 0," +
                "FOREIGN KEY(workflow_id) REFERENCES workflows(workflow_id))");
        } catch (SQLException e) {
            System.err.println("[DbWorkflowRepository] Schema error: " + e.getMessage());
        }
    }

    @Override public WorkflowDTO getWorkflowById(int id) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT workflow_id,workflow_name,current_status,assigned_to FROM workflows WHERE workflow_id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapWf(rs);
        } catch (SQLException e) { System.err.println("[DbWfRepo] getById: " + e.getMessage()); }
        return null;
    }

    @Override public List<WorkflowDTO> getAllWorkflows() {
        List<WorkflowDTO> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT workflow_id,workflow_name,current_status,assigned_to FROM workflows ORDER BY workflow_id")) {
            while (rs.next()) list.add(mapWf(rs));
        } catch (SQLException e) { System.err.println("[DbWfRepo] getAll: " + e.getMessage()); }
        return list;
    }

    @Override public int saveWorkflow(WorkflowDTO wf) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO workflows(workflow_name,current_status,assigned_to) VALUES(?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, wf.workflowName);
            ps.setString(2, wf.currentStatus != null ? wf.currentStatus : "Active");
            ps.setString(3, wf.assignedTo != null ? wf.assignedTo : "");
            ps.executeUpdate();
            ResultSet k = ps.getGeneratedKeys();
            if (k.next()) return k.getInt(1);
        } catch (SQLException e) { System.err.println("[DbWfRepo] save: " + e.getMessage()); }
        return -1;
    }

    @Override public void updateWorkflowStatus(int id, String status) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE workflows SET current_status=? WHERE workflow_id=?")) {
            ps.setString(1, status); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DbWfRepo] updateStatus: " + e.getMessage()); }
    }

    @Override public void deleteWorkflow(int id) {
        // Remove all steps first (referential integrity), then delete the workflow row
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM workflow_steps WHERE workflow_id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DbWfRepo] deleteSteps: " + e.getMessage()); }
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM workflows WHERE workflow_id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
            System.out.println("[DbWorkflowRepository] Workflow " + id + " permanently deleted.");
        } catch (SQLException e) { System.err.println("[DbWfRepo] deleteWorkflow: " + e.getMessage()); }
    }

    @Override public List<WorkflowStepDTO> getWorkflowSteps(int wfId) {
        List<WorkflowStepDTO> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT step_id,workflow_id,step_name,assignee,escalation_hours FROM workflow_steps WHERE workflow_id=? ORDER BY step_id")) {
            ps.setInt(1, wfId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapStep(rs));
        } catch (SQLException e) { System.err.println("[DbWfRepo] getSteps: " + e.getMessage()); }
        return list;
    }

    @Override public void addStep(int wfId, String name, String assignee, int escHours) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO workflow_steps(workflow_id,step_name,assignee,escalation_hours) VALUES(?,?,?,?)")) {
            ps.setInt(1, wfId); ps.setString(2, name);
            ps.setString(3, assignee != null ? assignee : "");
            ps.setInt(4, escHours); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DbWfRepo] addStep: " + e.getMessage()); }
    }

    @Override public void removeStep(int stepId) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM workflow_steps WHERE step_id=?")) {
            ps.setInt(1, stepId); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DbWfRepo] removeStep: " + e.getMessage()); }
    }

    @Override public void assignUserToStep(int stepId, String userId) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE workflow_steps SET assignee=? WHERE step_id=?")) {
            ps.setString(1, userId); ps.setInt(2, stepId); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DbWfRepo] assignUser: " + e.getMessage()); }
    }

    @Override public String getStatus(int id) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT current_status FROM workflows WHERE workflow_id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) { System.err.println("[DbWfRepo] getStatus: " + e.getMessage()); }
        return "NOT_FOUND";
    }

    private WorkflowDTO mapWf(ResultSet rs) throws SQLException {
        WorkflowDTO w = new WorkflowDTO();
        w.workflowId = rs.getInt("workflow_id"); w.workflowName = rs.getString("workflow_name");
        w.currentStatus = rs.getString("current_status"); w.assignedTo = rs.getString("assigned_to");
        return w;
    }
    private WorkflowStepDTO mapStep(ResultSet rs) throws SQLException {
        WorkflowStepDTO s = new WorkflowStepDTO();
        s.stepId = rs.getInt("step_id"); s.workflowId = rs.getInt("workflow_id");
        s.stepName = rs.getString("step_name"); s.assignee = rs.getString("assignee");
        s.escalationHours = rs.getInt("escalation_hours"); return s;
    }
}
