package com.hrms.customization.controller;

import com.hrms.customization.exception.CustomizationException;
import com.hrms.customization.model.Report;
import com.hrms.customization.service.ReportBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportBuilder reportBuilder;

    @Autowired
    public ReportController(ReportBuilder reportBuilder) {
        this.reportBuilder = reportBuilder;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllReports() {
        return ResponseEntity.ok(ApiResponse.ok(reportBuilder.listAllReports()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable int id) {
        try { return ResponseEntity.ok(ApiResponse.ok(reportBuilder.getReport(id))); }
        catch (CustomizationException ex) { return toError(ex); }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createReport(@RequestBody Map<String, String> body) {
        try {
            Map<String, Object> result = reportBuilder.createReport(
                body.get("name"), body.get("inputType"), body.get("format"));
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.okFlat(result));
        } catch (CustomizationException ex) { return toError(ex); }
    }

    @PostMapping("/{id}/generate")
    public ResponseEntity<Map<String, Object>> generateReport(@PathVariable int id) {
        try { return ResponseEntity.ok(ApiResponse.ok(reportBuilder.generateReport(id))); }
        catch (CustomizationException ex) { return toError(ex); }
    }

    @PatchMapping("/{id}/type")
    public ResponseEntity<Map<String, Object>> updateType(
            @PathVariable int id, @RequestBody Map<String, String> body) {
        try { return ResponseEntity.ok(ApiResponse.ok(reportBuilder.setReportType(id, body.get("type")))); }
        catch (CustomizationException ex) { return toError(ex); }
    }

    @PatchMapping("/{id}/format")
    public ResponseEntity<Map<String, Object>> updateFormat(
            @PathVariable int id, @RequestBody Map<String, String> body) {
        try {
            Map<String, Object> result = reportBuilder.setExportFormat(id, body.get("format"));
            return ResponseEntity.ok(ApiResponse.okFlat(result));
        } catch (CustomizationException ex) { return toError(ex); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReport(@PathVariable int id) {
        try {
            reportBuilder.deleteReport(id);
            Map<String, Object> m = new HashMap<>(); m.put("deleted", true); m.put("reportId", id);
            return ResponseEntity.ok(ApiResponse.okFlat(m));
        } catch (CustomizationException ex) { return toError(ex); }
    }

    /**
     * GET /api/reports/{id}/download
     * Streams real file bytes to the browser with Content-Disposition: attachment.
     * The browser shows the native Save-As dialog automatically.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable int id) {
        try {
            Report report    = reportBuilder.getReport(id);
            byte[] bytes     = reportBuilder.downloadReport(id);
            String fmt       = report.getFormat().toUpperCase();
            String mime      = switch (fmt) {
                case "EXCEL" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                case "CSV"   -> "text/csv";
                default      -> "application/pdf";
            };
            String ext       = switch (fmt) { case "EXCEL" -> ".xlsx"; case "CSV" -> ".csv"; default -> ".pdf"; };
            String safeName  = report.getReportName().replaceAll("[^a-zA-Z0-9_\\-]", "_");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mime));
            headers.setContentDispositionFormData("attachment", safeName + ext);
            headers.setContentLength(bytes.length);

            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (CustomizationException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    private ResponseEntity<Map<String, Object>> toError(CustomizationException ex) {
        int status = "MAJOR".equals(ex.getSeverity()) ? 400 : 200;
        return ResponseEntity.status(status)
               .body(ApiResponse.error(ex.getErrorCode(), ex.getSeverity(), ex.getMessage()));
    }
}
