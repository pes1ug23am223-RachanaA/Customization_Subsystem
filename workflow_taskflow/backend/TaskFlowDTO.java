/**
 * DTO: TaskFlowDTO
 * Subsystem: Customization
 * Component: Task Flow Builder
 *
 * Maps to the "TaskFlow" entity in the database.
 * Shared between Customization Subsystem and DB Team.
 */
public class TaskFlowDTO {
    public int taskId;
    public String flowName;
    public String flowStatus;      // "Active" or "Inactive"
    public int sequenceOrder;
    public String linkedMenu;
    public boolean validateOnNext;
    public boolean allowBackNav;
}
