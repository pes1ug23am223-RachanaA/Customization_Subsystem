import java.util.List;

/**
 * FACADE: CustomizationFacade
 * Subsystem: Customization
 *
 * STRUCTURAL PATTERN: Facade
 * Provides a single simplified entry point for ALL other subsystems
 * (Recruitment, Employee Management, Payroll, etc.) to access
 * Customization features.
 *
 * External subsystems NEVER call WorkflowServiceImpl or TaskFlowServiceImpl directly.
 * They only use this facade.
 *
 * USAGE by other subsystems:
 *   CustomizationFacade facade = CustomizationFacade.getInstance();
 *   List<WorkflowDTO> flows = facade.getAllWorkflows();
 *   facade.triggerWorkflow(workflowId, "Active");
 *
 * GRASP: Controller — acts as the system-level controller for cross-subsystem calls
 * SOLID: Open/Closed — add new methods here without touching service classes
 */
public class CustomizationFacade {

    private static CustomizationFacade instance;

    private final WorkflowService workflowService;
    private final TaskFlowService taskFlowService;

    private CustomizationFacade() {
        CustomizationServiceFactory factory = CustomizationServiceFactory.getInstance();
        this.workflowService = factory.getWorkflowService();
        this.taskFlowService = factory.getTaskFlowService();
    }

    public static synchronized CustomizationFacade getInstance() {
        if (instance == null) {
            instance = new CustomizationFacade();
        }
        return instance;
    }

    // ─── Workflow Methods (for external subsystems) ───────────────────────────

    /** Get all workflows — used by Recruitment, Leave, Payroll subsystems */
    public List<WorkflowDTO> getAllWorkflows() {
        return workflowService.getAllWorkflows();
    }

    /** Get a specific workflow by ID */
    public WorkflowDTO getWorkflow(int workflowId) {
        return workflowService.getWorkflowById(workflowId);
    }

    /** Get all steps for a workflow — used when executing approval chains */
    public List<WorkflowStepDTO> getWorkflowSteps(int workflowId) {
        return workflowService.getSteps(workflowId);
    }

    /** Trigger a workflow status change — e.g., Leave subsystem activates Leave workflow */
    public void triggerWorkflowStatus(int workflowId, String status) {
        workflowService.updateWorkflowStatus(workflowId, status);
    }

    // ─── Task Flow Methods (for external subsystems) ──────────────────────────

    /** Get all task flows — used by UI team to build navigation menus */
    public List<TaskFlowDTO> getAllTaskFlows() {
        return taskFlowService.getAllTaskFlows();
    }

    /** Get windows for a task flow — used by UI team to render multi-step screens */
    public List<String> getTaskFlowWindows(int taskId) {
        return taskFlowService.getWindows(taskId);
    }

    /** Get a specific task flow — used by navigation/routing layer */
    public TaskFlowDTO getTaskFlow(int taskId) {
        return taskFlowService.getTaskFlowById(taskId);
    }
}
