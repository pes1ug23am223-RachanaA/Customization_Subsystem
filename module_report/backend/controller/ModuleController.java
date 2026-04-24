package com.hrms.customization.controller;

import com.hrms.customization.exception.CustomizationException;
import com.hrms.customization.service.ModuleCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    private final ModuleCustomizer moduleCustomizer;

    @Autowired
    public ModuleController(ModuleCustomizer moduleCustomizer) {
        this.moduleCustomizer = moduleCustomizer;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllModules() {
        return ResponseEntity.ok(ApiResponse.ok(moduleCustomizer.listAllModules()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getModule(@PathVariable int id) {
        try { return ResponseEntity.ok(ApiResponse.ok(moduleCustomizer.getModule(id))); }
        catch (CustomizationException ex) { return toError(ex); }
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Map<String, Object>> enableModule(@PathVariable int id) {
        try { return ResponseEntity.ok(ApiResponse.ok(moduleCustomizer.enableModule(id))); }
        catch (CustomizationException ex) { return toError(ex); }
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Map<String, Object>> disableModule(@PathVariable int id) {
        try { return ResponseEntity.ok(ApiResponse.ok(moduleCustomizer.disableModule(id))); }
        catch (CustomizationException ex) { return toError(ex); }
    }

    @PatchMapping("/{name}/config")
    public ResponseEntity<Map<String, Object>> configureModule(
            @PathVariable String name, @RequestBody Map<String, String> body) {
        try {
            String warning = moduleCustomizer.configureModule(name, body.get("config"));
            return warning != null
                ? ResponseEntity.ok(ApiResponse.okWithWarning(null, warning))
                : ResponseEntity.ok(ApiResponse.ok(null));
        } catch (CustomizationException ex) { return toError(ex); }
    }

    private ResponseEntity<Map<String, Object>> toError(CustomizationException ex) {
        int status = "MAJOR".equals(ex.getSeverity()) ? 400 : 200;
        return ResponseEntity.status(status)
               .body(ApiResponse.error(ex.getErrorCode(), ex.getSeverity(), ex.getMessage()));
    }
}
