package com.hrms.customization.exception;

public final class ExceptionFactory {
    private ExceptionFactory() {}

    public static CustomizationException invalidModuleId(String detail)    { return new InvalidModuleIdException(detail); }
    public static CustomizationException moduleAlreadyEnabled(String name) { return new ModuleAlreadyEnabledException(name); }
    public static CustomizationException moduleAlreadyDisabled(String name){ return new ModuleAlreadyDisabledException(name); }
    public static CustomizationException unusedModuleConfig(String name)   { return new UnusedModuleConfigurationException(name); }
    public static CustomizationException reportGenerationFailed(String d)  { return new ReportGenerationFailedException(d); }
    public static CustomizationException reportFormatInvalid(String fmt)   { return new ReportExportFormatInvalidException(fmt); }
    public static CustomizationException fieldValidationFailed(String f)   { return new FieldValidationFailedException(f); }
}
