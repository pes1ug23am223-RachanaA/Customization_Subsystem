package com.hrms.customization.repository;

import com.hrms.customization.model.Module;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CLASS: DbModuleRepository
 * Subsystem: Customization — Module Customizer
 *
 * PURPOSE:
 *   Real DB-backed implementation of IModuleRepository.
 *   Connects to hrms.db and stores modules in the custom_modules table.
 *
 * DATABASE SCHEMA:
 *   custom_modules (module_id, module_name, module_type, is_enabled)
 *   - module_id: INTEGER PRIMARY KEY
 *   - module_name: VARCHAR(100)
 *   - module_type: VARCHAR(50) [Core, Extension]
 *   - is_enabled: BOOLEAN (0/1)
 */
public class DbModuleRepository implements IModuleRepository {

    private final String dbUrl;
    private Connection conn;

    public DbModuleRepository(String dbUrl) {
        this.dbUrl = dbUrl;
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection(dbUrl);
            System.out.println("[DbModuleRepository] Connected to DB: " + dbUrl);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("[DbModuleRepository] ERROR: Cannot connect to " + dbUrl);
            System.err.println(e.getMessage());
        }
    }

    @Override
    public Module getModuleById(int moduleId) {
        String sql = "SELECT module_id, module_name, module_type, is_enabled FROM custom_modules WHERE module_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, moduleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Module module = new Module();
                module.setModuleId(rs.getInt("module_id"));
                module.setModuleName(rs.getString("module_name"));
                module.setModuleType(rs.getString("module_type"));
                module.setEnabled(rs.getInt("is_enabled") == 1);
                return module;
            }
        } catch (SQLException e) {
            System.err.println("[DbModuleRepository] Error fetching module " + moduleId + ": " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Module> getAllModules() {
        List<Module> modules = new ArrayList<>();
        String sql = "SELECT module_id, module_name, module_type, is_enabled FROM custom_modules ORDER BY module_id";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Module module = new Module();
                module.setModuleId(rs.getInt("module_id"));
                module.setModuleName(rs.getString("module_name"));
                module.setModuleType(rs.getString("module_type"));
                module.setEnabled(rs.getInt("is_enabled") == 1);
                modules.add(module);
            }
        } catch (SQLException e) {
            System.err.println("[DbModuleRepository] Error fetching all modules: " + e.getMessage());
        }
        return modules;
    }

    @Override
    public void updateModuleStatus(int moduleId, boolean enabled) {
        String sql = "UPDATE custom_modules SET is_enabled = ? WHERE module_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enabled ? 1 : 0);
            ps.setInt(2, moduleId);
            ps.executeUpdate();
            System.out.println("[DbModuleRepository] Updated module " + moduleId + " status to " + (enabled ? "ENABLED" : "DISABLED"));
        } catch (SQLException e) {
            System.err.println("[DbModuleRepository] Error updating module status: " + e.getMessage());
        }
    }

    @Override
    public void updateModuleConfig(String moduleName, String config) {
        // Note: custom_modules table doesn't have a config column in current schema
        // This is a placeholder for future extensibility
        System.out.println("[DbModuleRepository] Config update not supported in current schema for module: " + moduleName);
    }

    @Override
    public boolean getModuleStatus(String moduleName) {
        String sql = "SELECT is_enabled FROM custom_modules WHERE module_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, moduleName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("is_enabled") == 1;
            }
        } catch (SQLException e) {
            System.err.println("[DbModuleRepository] Error fetching module status for " + moduleName + ": " + e.getMessage());
        }
        return false;
    }

    public void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException ignored) {}
    }
}
