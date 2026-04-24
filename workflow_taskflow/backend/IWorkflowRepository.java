import java.util.List;

/**
 * INTERFACE: IWorkflowRepository
 * Subsystem: Customization — Component: Workflow Engine
 *
 * Formal contract with the DB Team.
 * SOLID: Dependency Inversion — Workflow Engine depends on this abstraction.
 * GRASP: Polymorphism — allows Mock and Real DB implementations.
 *
 * NOTE: This file is provided by the DB Team.
 * Copied here only for compilation. Do NOT modify.
 */
public interface IWorkflowRepository {
    WorkflowDTO getWorkflowById(int workflowId);
    List<WorkflowDTO> getAllWorkflows();
    int saveWorkflow(WorkflowDTO wf);
    void updateWorkflowStatus(int workflowId, String status);
    void deleteWorkflow(int workflowId);
    List<WorkflowStepDTO> getWorkflowSteps(int workflowId);
    void addStep(int workflowId, String stepName, String assignee, int escalationHours);
    void removeStep(int stepId);
    void assignUserToStep(int stepId, String userId);
    String getStatus(int workflowId);
}
