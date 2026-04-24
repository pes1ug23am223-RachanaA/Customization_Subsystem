/**
 * EXCEPTION: TaskFlowException
 * Subsystem: Customization — Component: Task Flow Builder
 *
 * Error codes used:
 *   TASKFLOW_NOT_FOUND         — Major
 *   TASKFLOW_STATUS_UNCHANGED  — Minor
 *   DUPLICATE_TASKFLOW_NAME    — Warning
 *   INVALID_TASKFLOW_NAME      — Warning
 *   INVALID_WINDOW_NAME        — Warning
 *   INVALID_SEQUENCE           — Warning
 */
public class TaskFlowException extends RuntimeException {

    private final String errorCode;

    public TaskFlowException(String errorCode, String message) {
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
