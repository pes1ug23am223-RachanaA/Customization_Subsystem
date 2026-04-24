package com.hrms.customization.model;

public class Module {
    private int     moduleId;
    private String  moduleName;
    private String  moduleType;
    private boolean isEnabled;
    private String  config;

    public Module() {}
    public Module(int moduleId, String moduleName, String moduleType, boolean isEnabled, String config) {
        this.moduleId   = moduleId;
        this.moduleName = moduleName;
        this.moduleType = moduleType;
        this.isEnabled  = isEnabled;
        this.config     = config;
    }

    public int     getModuleId()   { return moduleId; }
    public String  getModuleName() { return moduleName; }
    public String  getModuleType() { return moduleType; }
    public boolean isEnabled()     { return isEnabled; }
    public String  getConfig()     { return config; }

    public void setModuleId(int id)         { this.moduleId   = id; }
    public void setModuleName(String name)  { this.moduleName = name; }
    public void setModuleType(String type)  { this.moduleType = type; }
    public void setEnabled(boolean enabled) { this.isEnabled  = enabled; }
    public void setConfig(String config)    { this.config     = config; }
}
