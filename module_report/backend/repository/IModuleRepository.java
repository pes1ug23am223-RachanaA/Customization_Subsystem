package com.hrms.customization.repository;

import com.hrms.customization.model.Module;
import java.util.List;

public interface IModuleRepository {
    Module       getModuleById(int moduleId);
    List<Module> getAllModules();
    void         updateModuleStatus(int moduleId, boolean enabled);
    void         updateModuleConfig(String moduleName, String config);
    boolean      getModuleStatus(String moduleName);
}
