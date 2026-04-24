/**
 * DTO: WorkflowDTO
 * Subsystem: Customization
 * Component: Workflow Engine
 *
 * Maps to the "Workflow" entity in the database.
 * Shared between Customization Subsystem and DB Team.
 */
public class WorkflowDTO {
    public int workflowId;
    public String workflowName;
    public String currentStatus;   // "Active" or "Inactive"
    public String assignedTo;
}
