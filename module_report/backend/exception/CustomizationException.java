package com.hrms.customization.exception;

public class CustomizationException extends RuntimeException {
    private final String errorCode;
    private final String severity;

    public CustomizationException(String errorCode, String severity, String message) {
        super(message);
        this.errorCode = errorCode;
        this.severity  = severity;
    }

    public String getErrorCode() { return errorCode; }
    public String getSeverity()  { return severity;  }
}
