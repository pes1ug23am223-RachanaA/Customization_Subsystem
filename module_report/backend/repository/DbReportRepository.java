import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * DbReportRepository — SQLite implementation for Report Builder.
 * Simplified version without package dependencies.
 */
public class DbReportRepository {

    private final String dbUrl;
    private Connection conn;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DbReportRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection(dbUrl);
            System.out.println("[DbReportRepository] Connected to DB: " + dbUrl);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("[DbReportRepository] ERROR: Cannot connect to " + dbUrl);
        }
    }

    public int saveReport(String name, String inputType, String format) {
        String sql = "INSERT INTO hr_reports (report_name, report_type, export_format, schedule_config) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, inputType);
            ps.setString(3, format);
            ps.setString(4, "On-demand");
            ps.executeUpdate();
            
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid() as id")) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DbReportRepository] Error saving report: " + e.getMessage());
        }
        return -1;
    }

    public Map<String, Object> getReportById(int reportId) {
        String sql = "SELECT report_id, report_name, report_type, export_format, generated_date, schedule_config FROM hr_reports WHERE report_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reportId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("reportId", rs.getInt("report_id"));
                report.put("reportName", rs.getString("report_name"));
                report.put("inputType", rs.getString("report_type"));
                report.put("format", rs.getString("export_format"));
                report.put("schedule", rs.getString("schedule_config"));
                return report;
            }
        } catch (SQLException e) {
            System.err.println("[DbReportRepository] Error fetching report: " + e.getMessage());
        }
        return null;
    }

    public List<Map<String, Object>> getAllReports() {
        List<Map<String, Object>> reports = new ArrayList<>();
        String sql = "SELECT report_id, report_name, report_type, export_format, schedule_config FROM hr_reports ORDER BY report_id";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("reportId", rs.getInt("report_id"));
                report.put("reportName", rs.getString("report_name"));
                report.put("inputType", rs.getString("report_type"));
                report.put("format", rs.getString("export_format"));
                report.put("schedule", rs.getString("schedule_config"));
                reports.add(report);
            }
        } catch (SQLException e) {
            System.err.println("[DbReportRepository] Error fetching all reports: " + e.getMessage());
        }
        return reports;
    }

    public void customizeReportType(int reportId, String type) {
        String sql = "UPDATE hr_reports SET report_type = ? WHERE report_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setInt(2, reportId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DbReportRepository] Error customizing report type: " + e.getMessage());
        }
    }

    public void exportReportFormat(int reportId, String format) {
        String sql = "UPDATE hr_reports SET export_format = ? WHERE report_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, format);
            ps.setInt(2, reportId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DbReportRepository] Error exporting report format: " + e.getMessage());
        }
    }

    public void generateReport(int reportId) {
        String sql = "UPDATE hr_reports SET generated_date = ? WHERE report_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LocalDateTime.now().format(FMT));
            ps.setInt(2, reportId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DbReportRepository] Error generating report: " + e.getMessage());
        }
    }

    public void deleteReport(int reportId) {
        String sql = "DELETE FROM hr_reports WHERE report_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reportId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DbReportRepository] Error deleting report: " + e.getMessage());
        }
    }
}
