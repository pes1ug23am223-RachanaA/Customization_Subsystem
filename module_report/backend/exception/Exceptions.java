package com.hrms.customization.exception;

// ── Module Exceptions ─────────────────────────────────────────────────────────

/** INVALID_MODULE_ID (MAJOR) — module ID does not exist */
class InvalidModuleIdException extends CustomizationException {
    public InvalidModuleIdException(String detail) {
        super("INVALID_MODULE_ID", "MAJOR", "INVALID_MODULE_ID: " + detail);
    }
}

/** MODULE_ALREADY_ENABLED (MINOR) — enableModule called on already-active module */
class ModuleAlreadyEnabledException extends CustomizationException {
    public ModuleAlreadyEnabledException(String name) {
        super("MODULE_ALREADY_ENABLED", "MINOR",
              "MODULE_ALREADY_ENABLED: '" + name + "' is already active.");
    }
}

/** MODULE_ALREADY_DISABLED (MINOR) — disableModule called on already-disabled module */
class ModuleAlreadyDisabledException extends CustomizationException {
    public ModuleAlreadyDisabledException(String name) {
        super("MODULE_ALREADY_DISABLED", "MINOR",
              "MODULE_ALREADY_DISABLED: '" + name + "' is already disabled.");
    }
}

/** UNUSED_MODULE_CONFIGURATION (WARNING) — config saved on a disabled module */
class UnusedModuleConfigurationException extends CustomizationException {
    public UnusedModuleConfigurationException(String name) {
        super("UNUSED_MODULE_CONFIGURATION", "WARNING",
              "UNUSED_MODULE_CONFIGURATION: Config saved but '" + name + "' is disabled.");
    }
}

// ── Report Exceptions ─────────────────────────────────────────────────────────

/** REPORT_GENERATION_FAILED (MAJOR) — bad ID or generation error */
class ReportGenerationFailedException extends CustomizationException {
    public ReportGenerationFailedException(String detail) {
        super("REPORT_GENERATION_FAILED", "MAJOR", "REPORT_GENERATION_FAILED: " + detail);
    }
}

/** REPORT_EXPORT_FORMAT_INVALID (WARNING) — unsupported format, defaults to PDF */
class ReportExportFormatInvalidException extends CustomizationException {
    public ReportExportFormatInvalidException(String fmt) {
        super("REPORT_EXPORT_FORMAT_INVALID", "WARNING",
              "REPORT_EXPORT_FORMAT_INVALID: '" + fmt + "' unsupported. Defaulting to PDF.");
    }
}

/** FIELD_VALIDATION_FAILED (MAJOR) — required field is blank */
class FieldValidationFailedException extends CustomizationException {
    public FieldValidationFailedException(String field) {
        super("FIELD_VALIDATION_FAILED", "MAJOR",
              "FIELD_VALIDATION_FAILED: '" + field + "' cannot be empty.");
    }
}
