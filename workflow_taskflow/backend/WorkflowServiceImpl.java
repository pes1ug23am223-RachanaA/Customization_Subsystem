import java.util.ArrayList;
import java.util.List;

/**
 * IMPLEMENTATION: WorkflowServiceImpl
 * Subsystem: Customization — Component: Workflow Engine
 *
 * DESIGN PATTERNS APPLIED:
 *
 * 1. CREATIONAL — Singleton (via CustomizationServiceFactory)
 *    Only one instance of WorkflowServiceImpl is created per application run.
 *    Access via: CustomizationServiceFactory.getInstance().getWorkflowService()
 *
 * 2. STRUCTURAL — Facade
 *    WorkflowServiceImpl is exposed through CustomizationFacade to other subsystems.
 *    External teams only see the facade — not this class directly.
 *
 * 3. BEHAVIOURAL — Observer
 *    Notifies all registered WorkflowObserver implementations when status changes.
 *    UI Team / Notification Service registers their observer at startup.
 *
 * GRASP:
 *    - Information Expert: knows all workflow data, handles all workflow logic
 *    - Controller: single point of control for workflow operations
 *    - Low Coupling: depends only on IWorkflowRepository interface, not DB team's class
 *
 * SOLID:
 *    - Single Responsibility: only handles workflow business logic
 *    - Dependency Inversion: uses IWorkflowRepository, not MockWorkflowRepository directly
 */
public class WorkflowServiceImpl implements WorkflowService {

    private final IWorkflowRepository repo;
    private final List<WorkflowObserver> observers = new ArrayList<>();

    /**
     * Constructor accepts any IWorkflowRepository implementation.
     * Pass MockWorkflowRepository now; swap to DB team's class later.
     */
    public WorkflowServiceImpl(IWorkflowRepository repo) {
        this.repo = repo;
    }

    // ─── Observer Management ─────────────────────────────────────────────────

    @Override
    public void registerObserver(WorkflowObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(WorkflowObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(int workflowId, String name, String oldStatus, String newStatus) {
        for (WorkflowObserver obs : observers) {
            obs.onWorkflowStatusChanged(workflowId, name, oldStatus, newStatus);
        }
    }

    // ─── Workflow CRUD ────────────────────────────────────────────────────────

    @Override
    public List<WorkflowDTO> getAllWorkflows() {
        return repo.getAllWorkflows();
    }

    @Override
    public WorkflowDTO getWorkflowById(int workflowId) {
        WorkflowDTO wf = repo.getWorkflowById(workflowId);
        if (wf == null) throw new WorkflowException("WORKFLOW_NOT_FOUND",
                "Workflow ID " + workflowId + " does not exist.");
        return wf;
    }

    @Override
    public int createWorkflow(String name, String assignedTo) {
        if (name == null || name.trim().isEmpty())
            throw new WorkflowException("INVALID_WORKFLOW_NAME", "Workflow name cannot be empty.");

        // Check for duplicate name — ignore workflows that are already deleted
        boolean exists = repo.getAllWorkflows().stream()
                .anyMatch(w -> w.workflowName.equalsIgnoreCase(name.trim())
                            && !"Deleted".equalsIgnoreCase(w.currentStatus));
        if (exists)
            throw new WorkflowException("DUPLICATE_WORKFLOW_NAME",
                    "A workflow named '" + name + "' already exists.");

        WorkflowDTO wf = new WorkflowDTO();
        wf.workflowName = name.trim();
        wf.currentStatus = "Active";
        wf.assignedTo = assignedTo;
        return repo.saveWorkflow(wf);
    }

    @Override
    public void updateWorkflowStatus(int workflowId, String newStatus) {
        WorkflowDTO wf = getWorkflowById(workflowId);  // throws if not found
        String oldStatus = wf.currentStatus;

        if (oldStatus.equals(newStatus)) {
            if ("Active".equals(newStatus))
                throw new WorkflowException("WORKFLOW_ALREADY_ACTIVE",
                        "Workflow '" + wf.workflowName + "' is already active.");
            else
                throw new WorkflowException("WORKFLOW_ALREADY_INACTIVE",
                        "Workflow '" + wf.workflowName + "' is already inactive.");
        }

        repo.updateWorkflowStatus(workflowId, newStatus);
        notifyObservers(workflowId, wf.workflowName, oldStatus, newStatus);
    }

    @Override
    public void deleteWorkflow(int workflowId) {
        getWorkflowById(workflowId);  // validates existence; throws WorkflowException if not found
        // Permanently remove from database (steps are cascade-deleted inside the repo)
        repo.deleteWorkflow(workflowId);
    }

    // ─── Step Management ──────────────────────────────────────────────────────

    @Override
    public List<WorkflowStepDTO> getSteps(int workflowId) {
        getWorkflowById(workflowId);  // validates workflow exists
        return repo.getWorkflowSteps(workflowId);
    }

    @Override
    public void addStep(int workflowId, String stepName, String assignee, int escalationHours) {
        getWorkflowById(workflowId);  // validates workflow exists

        if (stepName == null || stepName.trim().isEmpty())
            throw new WorkflowException("INVALID_STEP_NAME", "Step name cannot be empty.");

        if (escalationHours < 0)
            throw new WorkflowException("INVALID_ESCALATION",
                    "Escalation hours cannot be negative.");

        repo.addStep(workflowId, stepName.trim(), assignee, escalationHours);
    }

    @Override
    public void removeStep(int stepId) {
        repo.removeStep(stepId);
    }

    @Override
    public void assignUserToStep(int stepId, String userId) {
        if (userId == null || userId.trim().isEmpty())
            throw new WorkflowException("INVALID_USER", "User ID cannot be empty.");
        repo.assignUserToStep(stepId, userId.trim());
    }
}
