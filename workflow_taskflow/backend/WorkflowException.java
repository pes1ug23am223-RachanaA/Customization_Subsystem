/**
 * EXCEPTION: WorkflowException
 * Subsystem: Customization — Component: Workflow Engine
 *
 * Custom exception for all workflow-related errors.
 * Carries an error code that maps to the exceptions defined
 * in CustomizationExceptions.pdf.
 *
 * Error codes used:
 *   WORKFLOW_NOT_FOUND        — Major
 *   WORKFLOW_ALREADY_ACTIVE   — Minor
 *   WORKFLOW_ALREADY_INACTIVE — Minor
 *   INVALID_WORKFLOW_NAME     — Warning
 *   DUPLICATE_WORKFLOW_NAME   — Warning
 *   INVALID_STEP_NAME         — Warning
 *   INVALID_ESCALATION        — Warning
 *   INVALID_USER              — Warning
 */
public class WorkflowException extends RuntimeException {

    private final String errorCode;

    public WorkflowException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "[" + errorCode + "] " + getMessage();
    }
}
