/**
 * CONCRETE OBSERVER: ConsoleNotificationObserver
 * Subsystem: Customization — Component: Workflow Engine
 *
 * BEHAVIOURAL PATTERN: Observer (Concrete Observer)
 *
 * Prints workflow status changes to the console.
 * In production, the UI Team or Notification Service would register
 * their own observer implementation instead.
 *
 * Replace with: EmailNotificationObserver, AuditLogObserver, etc.
 */
public class ConsoleNotificationObserver implements WorkflowObserver {

    @Override
    public void onWorkflowStatusChanged(int workflowId, String workflowName,
                                         String oldStatus, String newStatus) {
        System.out.println("[WORKFLOW NOTIFICATION] '" + workflowName +
                "' (ID=" + workflowId + ") changed: " + oldStatus + " → " + newStatus);
    }
}
