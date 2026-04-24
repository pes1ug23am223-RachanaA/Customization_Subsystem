package com.hrms.customization.service;

import com.hrms.customization.exception.ExceptionFactory;
import com.hrms.customization.model.Module;
import com.hrms.customization.repository.IModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ModuleCustomizer — Business Logic Layer for Module Customizer component.
 * Enforces all exception rules from CustomizationExceptions.pdf before
 * delegating to the repository (DB) layer.
 */
@Service
public class ModuleCustomizer {

    private final IModuleRepository moduleRepo;

    @Autowired
    public ModuleCustomizer(IModuleRepository moduleRepo) {
        this.moduleRepo = moduleRepo;
    }

    /** Returns all modules — used to populate the main table. */
    public List<Module> listAllModules() {
        return moduleRepo.getAllModules();
    }

    /**
     * Returns one module by ID.
     * EXCEPTION: INVALID_MODULE_ID (MAJOR) if not found.
     */
    public Module getModule(int moduleId) {
        Module mod = moduleRepo.getModuleById(moduleId);
        if (mod == null) throw ExceptionFactory.invalidModuleId("Module ID " + moduleId + " does not exist.");
        return mod;
    }

    /**
     * Enables a module.
     * EXCEPTION: INVALID_MODULE_ID (MAJOR) if not found.
     * EXCEPTION: MODULE_ALREADY_ENABLED (MINOR) if already active.
     */
    public Module enableModule(int moduleId) {
        Module mod = moduleRepo.getModuleById(moduleId);
        if (mod == null)      throw ExceptionFactory.invalidModuleId("Module ID " + moduleId + " does not exist.");
        if (mod.isEnabled())  throw ExceptionFactory.moduleAlreadyEnabled(mod.getModuleName());
        moduleRepo.updateModuleStatus(moduleId, true);
        return moduleRepo.getModuleById(moduleId);
    }

    /**
     * Disables a module.
     * EXCEPTION: INVALID_MODULE_ID (MAJOR) if not found.
     * EXCEPTION: MODULE_ALREADY_DISABLED (MINOR) if already off.
     */
    public Module disableModule(int moduleId) {
        Module mod = moduleRepo.getModuleById(moduleId);
        if (mod == null)      throw ExceptionFactory.invalidModuleId("Module ID " + moduleId + " does not exist.");
        if (!mod.isEnabled()) throw ExceptionFactory.moduleAlreadyDisabled(mod.getModuleName());
        moduleRepo.updateModuleStatus(moduleId, false);
        return moduleRepo.getModuleById(moduleId);
    }

    /**
     * Saves config for a module. Config is saved regardless; but if the
     * module is disabled a WARNING message is returned to the controller.
     * EXCEPTION: UNUSED_MODULE_CONFIGURATION (WARNING) if module is disabled.
     *
     * @return warning string, or null if none
     */
    public String configureModule(String moduleName, String config) {
        boolean isActive = moduleRepo.getModuleStatus(moduleName);
        moduleRepo.updateModuleConfig(moduleName, config);
        if (!isActive) return ExceptionFactory.unusedModuleConfig(moduleName).getMessage();
        return null;
    }

    /** Utility: returns true if the named module is enabled. */
    public boolean isModuleActive(String moduleName) {
        return moduleRepo.getModuleStatus(moduleName);
    }
}
