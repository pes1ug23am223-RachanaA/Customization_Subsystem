/**
 * DTO: WorkflowStepDTO
 * Subsystem: Customization
 * Component: Workflow Engine
 *
 * Maps to the "WorkflowStep" entity in the database.
 * Represents a single approval step within a Workflow.
 */
public class WorkflowStepDTO {
    public int stepId;
    public int workflowId;
    public String stepName;
    public String assignee;
    public int escalationHours;
}
