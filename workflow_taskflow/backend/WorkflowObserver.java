/**
 * INTERFACE: WorkflowObserver
 * Subsystem: Customization — Component: Workflow Engine
 *
 * BEHAVIOURAL PATTERN: Observer
 * Any component that needs to be notified when a workflow step
 * changes status must implement this interface.
 *
 * Usage:
 *   - WorkflowEngine notifies all registered observers on status change
 *   - Other subsystems (e.g., Notification Service, Audit Log) implement this
 *
 * Example:
 *   workflowService.registerObserver(new EmailNotificationObserver());
 *   workflowService.registerObserver(new AuditLogObserver());
 */
public interface WorkflowObserver {
    /**
     * Called by WorkflowEngine whenever a workflow changes status.
     * @param workflowId  The ID of the workflow that changed
     * @param workflowName The name of the workflow
     * @param oldStatus   Status before the change
     * @param newStatus   Status after the change
     */
    void onWorkflowStatusChanged(int workflowId, String workflowName,
                                  String oldStatus, String newStatus);
}
