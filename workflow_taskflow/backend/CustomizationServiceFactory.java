/**
 * FACTORY: CustomizationServiceFactory
 * Subsystem: Customization
 *
 * CREATIONAL PATTERN: Singleton
 * Ensures only ONE instance of each service exists in the application.
 * All components and the UI layer get services from here.
 *
 * USAGE:
 *   WorkflowService ws = CustomizationServiceFactory.getInstance().getWorkflowService();
 *   TaskFlowService ts = CustomizationServiceFactory.getInstance().getTaskFlowService();
 *
 */
public class CustomizationServiceFactory {

    // Singleton instance
    private static CustomizationServiceFactory instance;

    // Services — created once, reused everywhere
    private final WorkflowService workflowService;
    private final TaskFlowService taskFlowService;

    private CustomizationServiceFactory() {
        IWorkflowRepository workflowRepo = new DbWorkflowRepository("jdbc:sqlite:hrms.db");
        ITaskFlowRepository taskFlowRepo  = new DbTaskFlowRepository("jdbc:sqlite:hrms.db");
        // ─────────────────────────────────────────────────────────────────────

        WorkflowServiceImpl wfService = new WorkflowServiceImpl(workflowRepo);
        // Register default observer — UI team can add theirs here too
        wfService.registerObserver(new ConsoleNotificationObserver());

        this.workflowService = wfService;
        this.taskFlowService = new TaskFlowServiceImpl(taskFlowRepo);
    }

    public static synchronized CustomizationServiceFactory getInstance() {
        if (instance == null) {
            instance = new CustomizationServiceFactory();
        }
        return instance;
    }

    public WorkflowService getWorkflowService() {
        return workflowService;
    }

    public TaskFlowService getTaskFlowService() {
        return taskFlowService;
    }
}
