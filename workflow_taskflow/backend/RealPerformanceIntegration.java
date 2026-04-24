import java.util.ArrayList;
import java.util.List;

/**
 * RealPerformanceIntegration
 *
 * Implements BOTH interfaces provided by the Performance Management team:
 *   - IPerformanceForCustomization (their adapter interface for us)
 *   - IPerformanceSubsystem        (their full subsystem contract)
 *
 * Connects to hrms.db via JDBC and reads from:
 *   goals, appraisals, feedback, kpis, employees tables
 *
 * Used by ApiServer's /api/performance/* endpoints.
 * Also used by WorkflowEngine for performance review workflow discovery.
 *
 * INTEGRATION PATTERN: Adapter
 *   Translates PM team's model classes into Customization DTOs.
 */
public class RealPerformanceIntegration {

    private static final String DB_URL = "jdbc:sqlite:hrms.db";
    private final DbPerformanceRepository performanceRepository;

    public RealPerformanceIntegration() {
        // Adapter pattern: this class translates raw repository results into
        // the customization-facing interface contract used by the subsystem.
        performanceRepository = new DbPerformanceRepository(DB_URL);
    }

    // ── IPerformanceForCustomization methods ──────────────────────────────────

    public String getServerPort() { return "8080"; }
    public List<String> getFormIds()     { return performanceRepository.loadIds("custom_forms", "form_name", "form_id"); }
    public List<String> getWorkflowIds() { return performanceRepository.loadIds("workflows", "workflow_name", "workflow_id"); }
    public List<String> getTaskFlowIds() { return performanceRepository.loadIds("task_flows", "flow_name", "task_id"); }

    public List<PerformanceCycle> getAllPerformanceCycles() {
        return performanceRepository.getAllCycles();
    }

    public List<Goal> getGoalsForCycle(String cycleName) {
        return performanceRepository.getGoalsForCycle(cycleName);
    }

    public List<Appraisal> getAppraisalsForCycle(String cycleName) {
        return performanceRepository.getAppraisalsForCycle(cycleName);
    }

    public String getFormDescription(String formId) {
        return "Performance-linked customization form: " + formId;
    }

    public String getWorkflowDescription(String wfId) {
        return "Performance-linked customization workflow: " + wfId;
    }

    public String getTaskFlowDescription(String tfId) {
        return "Performance-linked customization task flow: " + tfId;
    }

    // ── IPerformanceSubsystem methods ─────────────────────────────────────────

    public List<Goal> getAllGoals() { return getGoalsForCycle(null); }

    public List<Goal> getGoalsByEmployee(String employeeId) {
        List<Goal> list = new ArrayList<>();
        for (Goal goal : performanceRepository.getGoalsForCycle(null))
            if (employeeId != null && employeeId.equals(goal.employeeId)) list.add(goal);
        return list;
    }

    public List<Appraisal> getAllAppraisals() { return getAppraisalsForCycle(null); }

    public List<Appraisal> getAppraisalsByEmployee(String employeeId) {
        List<Appraisal> list = new ArrayList<>();
        for (Appraisal appraisal : performanceRepository.getAppraisalsForCycle(null))
            if (employeeId != null && employeeId.equals(appraisal.employeeId)) list.add(appraisal);
        return list;
    }

    public List<Feedback> getAllFeedback() {
        return performanceRepository.getAllFeedback();
    }

    public List<Feedback> getFeedbackForEmployee(String employeeId) {
        List<Feedback> list = new ArrayList<>();
        for (Feedback feedback : performanceRepository.getAllFeedback())
            if (employeeId != null && employeeId.equals(feedback.employeeId)) list.add(feedback);
        return list;
    }

    public List<KPI> getAllKPIs() {
        return performanceRepository.getAllKPIs();
    }

    public int getAverageScore() {
        return performanceRepository.getAverageScore();
    }

    public int getGoalsOnTrackCount() {
        return performanceRepository.getGoalsOnTrackCount();
    }

    public int getGoalsOnTrackPercentage() {
        return performanceRepository.getGoalsOnTrackPercentage();
    }
}
