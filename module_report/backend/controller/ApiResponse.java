package com.hrms.customization.controller;

import java.util.HashMap;
import java.util.Map;

public final class ApiResponse {
    private ApiResponse() {}

    public static Map<String, Object> ok(Object data) {
        Map<String, Object> m = new HashMap<>();
        m.put("success", true); m.put("data", data); return m;
    }

    public static Map<String, Object> okWithWarning(Object data, String warning) {
        Map<String, Object> m = ok(data); m.put("warning", warning); return m;
    }

    public static Map<String, Object> okFlat(Map<String, Object> payload) {
        Map<String, Object> m = new HashMap<>();
        m.put("success", true); m.putAll(payload); return m;
    }

    public static Map<String, Object> error(String code, String severity, String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("success", false); m.put("code", code);
        m.put("severity", severity); m.put("message", message); return m;
    }
}
