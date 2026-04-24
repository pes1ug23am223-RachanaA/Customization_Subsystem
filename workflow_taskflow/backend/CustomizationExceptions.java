/**
 * FILE: CustomizationExceptions.java
 * Subsystem: Customization — Shared Exception Registry
 *
 * Covers ALL exceptions from the Exception Specification (Deliverable 4):
 *
 * MAJOR EXCEPTIONS:
 *   INVALID_MODULE_ID          — Module ID does not exist
 *   FORM_NOT_FOUND             — Requested form does not exist
 *   WORKFLOW_EXECUTION_FAILED  — Workflow could not be executed
 *   TASK_SEQUENCE_ERROR        — Invalid task sequence order
 *   FIELD_VALIDATION_FAILED    — Field validation failed
 *   REPORT_GENERATION_FAILED   — Unable to generate report
 *
 * MINOR EXCEPTIONS:
 *   MODULE_ALREADY_ENABLED     — Module is already enabled
 *   MODULE_ALREADY_DISABLED    — Module is already disabled
 *   LOOKUP_VALUE_NOT_FOUND     — Lookup value does not exist
 *   EXTRA_INFO_NOT_FOUND       — Extra information ID not found
 *   EMPLOYEE_DATA_READ_ONLY    — Employee data is read-only in this subsystem
 *   CANDIDATE_DATA_READ_ONLY   — Candidate data is read-only in this subsystem
 *
 * WARNING EXCEPTIONS:
 *   EMPTY_FIELD_NAME           — Field name cannot be empty
 *   DUPLICATE_LOOKUP_VALUE     — Value already exists in lookup
 *   REPORT_EXPORT_FORMAT_INVALID — Unsupported report export format
 *   UNUSED_MODULE_CONFIGURATION — Configuration exists but module is disabled
 *
 * HOW TO USE:
 *   throw CustomizationExceptions.invalidModuleId(moduleId);
 *   throw CustomizationExceptions.formNotFound(formId);
 *   etc.
 */
public class CustomizationExceptions {

    // ─── MAJOR EXCEPTIONS ────────────────────────────────────────────────────

    public static CustomizationException invalidModuleId(int moduleId) {
        return new CustomizationException("INVALID_MODULE_ID", "MAJOR",
            "Module ID does not exist: " + moduleId,
            "Display error and prevent operation until a valid moduleId is provided.");
    }

    public static CustomizationException formNotFound(int formId) {
        return new CustomizationException("FORM_NOT_FOUND", "MAJOR",
            "Requested form does not exist: formId=" + formId,
            "Notify user and redirect to form creation page.");
    }

    public static CustomizationException workflowExecutionFailed(String reason) {
        return new CustomizationException("WORKFLOW_EXECUTION_FAILED", "MAJOR",
            "Workflow could not be executed: " + reason,
            "Rollback changes and log error for debugging.");
    }

    public static CustomizationException taskSequenceError(String detail) {
        return new CustomizationException("TASK_SEQUENCE_ERROR", "MAJOR",
            "Invalid task sequence order: " + detail,
            "Validate sequence before saving and prompt user to correct it.");
    }

    public static CustomizationException fieldValidationFailed(String fieldName, String reason) {
        return new CustomizationException("FIELD_VALIDATION_FAILED", "MAJOR",
            "Field validation failed for '" + fieldName + "': " + reason,
            "Highlight invalid fields and prevent submission.");
    }

    public static CustomizationException reportGenerationFailed(String reason) {
        return new CustomizationException("REPORT_GENERATION_FAILED", "MAJOR",
            "Unable to generate report: " + reason,
            "Retry operation or notify user to check input data.");
    }

    // ─── MINOR EXCEPTIONS ────────────────────────────────────────────────────

    public static CustomizationException moduleAlreadyEnabled(String moduleName) {
        return new CustomizationException("MODULE_ALREADY_ENABLED", "MINOR",
            "Module is already enabled: " + moduleName,
            "Inform user and skip operation.");
    }

    public static CustomizationException moduleAlreadyDisabled(String moduleName) {
        return new CustomizationException("MODULE_ALREADY_DISABLED", "MINOR",
            "Module is already disabled: " + moduleName,
            "Inform user and skip operation.");
    }

    public static CustomizationException lookupValueNotFound(String value) {
        return new CustomizationException("LOOKUP_VALUE_NOT_FOUND", "MINOR",
            "Lookup value does not exist: " + value,
            "Notify user and allow re-entry.");
    }

    public static CustomizationException extraInfoNotFound(int eitId) {
        return new CustomizationException("EXTRA_INFO_NOT_FOUND", "MINOR",
            "Extra information ID not found: " + eitId,
            "Ignore update/delete and notify user.");
    }

    public static CustomizationException employeeDataReadOnly() {
        return new CustomizationException("EMPLOYEE_DATA_READ_ONLY", "MINOR",
            "Employee data is read-only in this subsystem.",
            "Restrict editing and show informational message. " +
            "Employee data is owned by the Recruitment/Onboarding subsystem.");
    }

    public static CustomizationException candidateDataReadOnly() {
        return new CustomizationException("CANDIDATE_DATA_READ_ONLY", "MINOR",
            "Candidate data is read-only in this subsystem.",
            "Restrict editing and notify user. " +
            "Candidate data is owned by the Recruitment Management subsystem.");
    }

    // ─── WARNING EXCEPTIONS ───────────────────────────────────────────────────

    public static CustomizationException emptyFieldName() {
        return new CustomizationException("EMPTY_FIELD_NAME", "WARNING",
            "Field name cannot be empty.",
            "Prompt user to enter a valid field name.");
    }

    public static CustomizationException duplicateLookupValue(String value) {
        return new CustomizationException("DUPLICATE_LOOKUP_VALUE", "WARNING",
            "Value already exists in lookup: " + value,
            "Warn user and prevent duplication.");
    }

    public static CustomizationException reportExportFormatInvalid(String format) {
        return new CustomizationException("REPORT_EXPORT_FORMAT_INVALID", "WARNING",
            "Unsupported report export format: " + format,
            "Suggest valid formats: PDF, CSV, Excel.");
    }

    public static CustomizationException unusedModuleConfiguration(String moduleName) {
        return new CustomizationException("UNUSED_MODULE_CONFIGURATION", "WARNING",
            "Configuration exists but module is disabled: " + moduleName,
            "Notify user and suggest enabling module.");
    }

    // ─── Base Exception ───────────────────────────────────────────────────────

    public static class CustomizationException extends RuntimeException {
        private final String errorCode;
        private final String category;
        private final String resolutionHint;

        public CustomizationException(String errorCode, String category, String message, String resolutionHint) {
            super(message);
            this.errorCode = errorCode;
            this.category = category;
            this.resolutionHint = resolutionHint;
        }

        public String getErrorCode()     { return errorCode; }
        public String getCategory()      { return category; }
        public String getResolutionHint(){ return resolutionHint; }

        public String toJson() {
            return "{\"errorCode\":\"" + errorCode + "\",\"category\":\"" + category +
                   "\",\"message\":\"" + getMessage().replace("\"","\\\"") +
                   "\",\"hint\":\"" + resolutionHint.replace("\"","\\\"") + "\"}";
        }

        @Override
        public String toString() {
            return "[" + category + "/" + errorCode + "] " + getMessage();
        }
    }
}
