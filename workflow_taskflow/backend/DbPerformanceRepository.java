import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DB-team access API for Performance Management data.
 *
 * Structural pattern support: RealPerformanceIntegration adapts this repository
 * to the interfaces consumed by the customization subsystem.
 *
 * GRASP: Information Expert
 * SOLID: SRP for performance data access, DIP for callers
 */
public class DbPerformanceRepository {

    private final String dbUrl;
    private Connection conn;

    public DbPerformanceRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(dbUrl);
        } catch (Exception e) {
            CustomizationErrorApi.getInstance().logError(
                "DbPerformanceRepository", "DB_CONNECT_FAILED", "Unable to connect to performance repository", e.getMessage());
        }
    }

    public List<PerformanceCycle> getAllCycles() {
        List<PerformanceCycle> list = new ArrayList<>();
        if (conn == null) return list;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT DISTINCT appraisal_period, appraisal_status FROM appraisals ORDER BY appraisal_period")) {
            while (rs.next()) {
                list.add(new PerformanceCycle(
                    rs.getString("appraisal_period"), "", "", rs.getString("appraisal_status")));
            }
        } catch (SQLException e) {
            log("PERFORMANCE_CYCLE_READ_FAILED", "Unable to load performance cycles", e);
        }
        return list;
    }

    public List<Goal> getGoalsForCycle(String cycleName) {
        List<Goal> list = new ArrayList<>();
        if (conn == null) return list;
        String sql = "SELECT goal_id, emp_id, goal_title, goal_description, goal_status, goal_start_date, goal_end_date FROM goals LIMIT 50";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Goal(
                    String.valueOf(rs.getInt("goal_id")),
                    rs.getString("emp_id"),
                    rs.getString("goal_title"),
                    rs.getString("goal_description"),
                    rs.getString("goal_status"),
                    rs.getString("goal_start_date"),
                    rs.getString("goal_end_date")));
            }
        } catch (SQLException e) {
            log("PERFORMANCE_GOAL_READ_FAILED", "Unable to load goals", e);
        }
        return list;
    }

    public List<Appraisal> getAppraisalsForCycle(String cycleName) {
        List<Appraisal> list = new ArrayList<>();
        if (conn == null) return list;
        String sql = (cycleName != null && !cycleName.isEmpty())
            ? "SELECT appraisal_id,emp_id,reviewer_id,appraisal_score,appraisal_status,appraisal_period,appraisal_date FROM appraisals WHERE appraisal_period=?"
            : "SELECT appraisal_id,emp_id,reviewer_id,appraisal_score,appraisal_status,appraisal_period,appraisal_date FROM appraisals LIMIT 50";
        try {
            PreparedStatement ps = (cycleName != null && !cycleName.isEmpty()) ? conn.prepareStatement(sql) : null;
            if (ps != null) ps.setString(1, cycleName);
            ResultSet rs = (ps != null) ? ps.executeQuery() : conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                list.add(new Appraisal(
                    String.valueOf(rs.getInt("appraisal_id")),
                    rs.getString("emp_id"),
                    rs.getString("reviewer_id"),
                    rs.getFloat("appraisal_score"),
                    rs.getString("appraisal_status"),
                    rs.getString("appraisal_period"),
                    rs.getString("appraisal_date")));
            }
        } catch (SQLException e) {
            log("PERFORMANCE_APPRAISAL_READ_FAILED", "Unable to load appraisals", e);
        }
        return list;
    }

    public List<Feedback> getAllFeedback() {
        List<Feedback> list = new ArrayList<>();
        if (conn == null) return list;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT feedback_id,emp_id,reviewer_id,feedback_text,feedback_type,feedback_date FROM feedback LIMIT 50")) {
            while (rs.next()) {
                list.add(new Feedback(
                    String.valueOf(rs.getInt("feedback_id")),
                    rs.getString("emp_id"),
                    rs.getString("reviewer_id"),
                    rs.getString("feedback_text"),
                    rs.getString("feedback_type"),
                    rs.getString("feedback_date")));
            }
        } catch (SQLException e) {
            log("PERFORMANCE_FEEDBACK_READ_FAILED", "Unable to load feedback", e);
        }
        return list;
    }

    public List<KPI> getAllKPIs() {
        List<KPI> list = new ArrayList<>();
        if (conn == null) return list;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT kpi_id,emp_id,goal_id,kpi_name,kpi_target_value,kpi_actual_value,kpi_unit FROM kpis LIMIT 50")) {
            while (rs.next()) {
                list.add(new KPI(
                    String.valueOf(rs.getInt("kpi_id")),
                    rs.getString("emp_id"),
                    String.valueOf(rs.getLong("goal_id")),
                    rs.getString("kpi_name"),
                    rs.getFloat("kpi_target_value"),
                    rs.getFloat("kpi_actual_value"),
                    rs.getString("kpi_unit")));
            }
        } catch (SQLException e) {
            log("PERFORMANCE_KPI_READ_FAILED", "Unable to load KPIs", e);
        }
        return list;
    }

    public int getAverageScore() {
        return readIntAggregate("SELECT AVG(appraisal_score) FROM appraisals", "PERFORMANCE_AVG_SCORE_FAILED");
    }

    public int getGoalsOnTrackCount() {
        return readIntAggregate("SELECT COUNT(*) FROM goals WHERE goal_status='In Progress'", "PERFORMANCE_ON_TRACK_COUNT_FAILED");
    }

    public int getGoalsOnTrackPercentage() {
        if (conn == null) return 0;
        try (Statement st = conn.createStatement()) {
            ResultSet total = st.executeQuery("SELECT COUNT(*) FROM goals");
            int totalCount = total.next() ? total.getInt(1) : 0;
            if (totalCount == 0) return 0;
            ResultSet onTrack = st.executeQuery("SELECT COUNT(*) FROM goals WHERE goal_status='In Progress'");
            int onTrackCount = onTrack.next() ? onTrack.getInt(1) : 0;
            return (int) ((onTrackCount * 100.0) / totalCount);
        } catch (SQLException e) {
            log("PERFORMANCE_ON_TRACK_PCT_FAILED", "Unable to compute goal progress percentage", e);
            return 0;
        }
    }

    public int countRows(String tableName) {
        if (conn == null) return 0;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            log("PERFORMANCE_TABLE_COUNT_FAILED", "Unable to count rows for " + tableName, e);
            return 0;
        }
    }

    public List<String> loadIds(String tableName, String valueColumn, String orderColumn) {
        List<String> values = new ArrayList<>();
        if (conn == null) return values;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT " + valueColumn + " FROM " + tableName + " ORDER BY " + orderColumn)) {
            while (rs.next()) values.add(rs.getString(1));
        } catch (SQLException e) {
            log("PERFORMANCE_CONFIG_READ_FAILED", "Unable to load IDs from " + tableName, e);
        }
        return values;
    }

    private int readIntAggregate(String sql, String code) {
        if (conn == null) return 0;
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? (int) rs.getFloat(1) : 0;
        } catch (SQLException e) {
            log(code, "Unable to read performance aggregate", e);
            return 0;
        }
    }

    private void log(String code, String message, Exception e) {
        CustomizationErrorApi.getInstance().logError("DbPerformanceRepository", code, message, e.getMessage());
    }
}
