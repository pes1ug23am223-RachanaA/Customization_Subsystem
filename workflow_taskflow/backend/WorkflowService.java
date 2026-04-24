import java.util.List;

/**
 * INTERFACE: WorkflowService
 * Subsystem: Customization — Component: Workflow Engine
 *
 * GRASP: Controller — WorkflowService is the controller for all
 * workflow-related operations. UI and API layer talk to this, not to the repo.
 *
 * SOLID: Interface Segregation — only workflow operations here.
 */
public interface WorkflowService {
    // Workflow CRUD
    List<WorkflowDTO> getAllWorkflows();
    WorkflowDTO getWorkflowById(int workflowId);
    int createWorkflow(String name, String assignedTo);
    void updateWorkflowStatus(int workflowId, String status);
    void deleteWorkflow(int workflowId);

    // Step management
    List<WorkflowStepDTO> getSteps(int workflowId);
    void addStep(int workflowId, String stepName, String assignee, int escalationHours);
    void removeStep(int stepId);
    void assignUserToStep(int stepId, String userId);

    // Observer registration (Behavioural: Observer pattern)
    void registerObserver(WorkflowObserver observer);
    void removeObserver(WorkflowObserver observer);
}
