import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DbReportRepository — Flat-package SQLite impl for Report Builder.
 * Table: hr_reports(report_id, report_name, report_type, export_format, generated_date, schedule_config)
 */
public class DbReportRepository {

    private final String dbUrl;
    private Connection conn;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DbReportRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
        ensureSchema();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(dbUrl);
            conn.setAutoCommit(true);
            System.out.println("[DbReportRepository] Connected: " + dbUrl);
        } catch (Exception e) { System.err.println("[DbReportRepository] " + e.getMessage()); }
    }

    private void ensureSchema() {
        // hr_reports already exists in DB; add report_id as INTEGER if schema uses VARCHAR
        try (Statement st = conn.createStatement()) {
            // Check if integer_id column exists, add if not
            try { st.execute("ALTER TABLE hr_reports ADD COLUMN integer_id INTEGER"); }
            catch (SQLException ignored) {}
            st.execute("UPDATE hr_reports SET integer_id=rowid WHERE integer_id IS NULL");
        } catch (SQLException e) { /* ignore */ }
    }

    public int save(String name, String type, String format) {
        String reportKey = UUID.randomUUID().toString();
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO hr_reports(report_id,report_name,report_type,export_format,schedule_config) VALUES(?,?,?,?,?)")) {
            ps.setString(1, reportKey);
            ps.setString(2,name);
            ps.setString(3,type);
            ps.setString(4,format);
            ps.setString(5,"On-demand");
            ps.executeUpdate();
            int rowid = findIntegerIdByReportKey(reportKey);
            if (rowid > 0) {
                try (PreparedStatement upd = conn.prepareStatement("UPDATE hr_reports SET integer_id=? WHERE rowid=?")) {
                    upd.setInt(1,rowid); upd.setInt(2,rowid); upd.executeUpdate();
                }
                System.out.println("[DbReportRepository] Saved report: "+name+" (id="+rowid+")");
            }
            return rowid;
        } catch (SQLException e) { System.err.println("[DbReportRepository] save: " + e.getMessage()); }
        return -1;
    }

    public List<ReportData> findAll() {
        List<ReportData> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT rowid,report_name,report_type,export_format,schedule_config,generated_date,integer_id FROM hr_reports ORDER BY rowid")) {
            while (rs.next()) list.add(mapReport(rs));
        } catch (SQLException e) { System.err.println("[DbReportRepository] findAll: " + e.getMessage()); }
        return list;
    }

    public ReportData findById(int id) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT rowid,report_name,report_type,export_format,schedule_config,generated_date,integer_id FROM hr_reports WHERE integer_id=?")) {
            ps.setInt(1,id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapReport(rs);
        } catch (SQLException e) { System.err.println("[DbReportRepository] findById: " + e.getMessage()); }
        return null;
    }

    public void markGenerated(int id) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE hr_reports SET generated_date=? WHERE integer_id=?")) {
            ps.setString(1, LocalDateTime.now().format(FMT)); ps.setInt(2,id); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DbReportRepository] generate: " + e.getMessage()); }
    }

    public void delete(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM hr_reports WHERE integer_id=?")) {
            ps.setInt(1,id); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DbReportRepository] delete: " + e.getMessage()); }
    }

    private int findIntegerIdByReportKey(String reportKey) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT rowid FROM hr_reports WHERE report_id = ? ORDER BY rowid DESC LIMIT 1")) {
            ps.setString(1, reportKey);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    private ReportData mapReport(ResultSet rs) throws SQLException {
        int id; try { id=rs.getInt("integer_id"); if(rs.wasNull())id=rs.getInt("rowid"); }
        catch(SQLException e){id=rs.getInt("rowid");}
        return new ReportData(id, rs.getString("report_name"), rs.getString("report_type"),
            rs.getString("export_format"), rs.getString("schedule_config"), rs.getString("generated_date"));
    }

    public static class ReportData {
        public int reportId; public String reportName,reportType,exportFormat,schedule,generatedDate;
        public ReportData(int id,String name,String type,String fmt,String sched,String gen){
            reportId=id;reportName=name;reportType=type;exportFormat=fmt;schedule=sched;generatedDate=gen;}
    }
}
