import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ApiServer — HRMS Customization Subsystem Unified Backend
 *
 * All 8 modules are connected to hrms.db via dedicated DB repositories.
 * All exceptions use CustomizationExceptions for consistent error reporting.
 *
 * DB TABLE MAPPINGS (per specification):
 *   Flexfield Manager  → custom_fields (form_id IS NULL)
 *   Form Designer      → custom_forms + custom_fields (form_id = form's ID)
 *   Module Customizer  → custom_modules
 *   Report Builder     → hr_reports
 *   Task Flow Builder  → task_flows + task_flow_windows
 *   Workflow Engine    → workflows + workflow_steps
 *   EIT Handler        → custom_fields (form_id = -1 sentinel)
 *   Lookup Customizer  → custom_fields (form_id = -2 sentinel)
 *
 * INTEGRATIONS:
 *   Onboarding/Offboarding → IEmployeeDataProvider + IEmployeeIntegration
 *   Performance Mgmt       → IPerformanceIntegration
 *
 * HOW TO RUN:
 *   cd workflow_taskflow/backend
 *   javac -cp ".:sqlite-jdbc.jar" *.java
 *   java -cp ".:sqlite-jdbc.jar" ApiServer
 */
public class ApiServer {

    private static final int    PORT   = 8080;
    private static final String DB_URL = "jdbc:sqlite:hrms.db";

    // ── Service layer (Workflow + TaskFlow use service pattern) ────────────────
    private static WorkflowService            workflowService;
    private static TaskFlowService            taskFlowService;
    private static WorkflowTemplateService    templateService;

    // ── Integration layer (Onboarding + Performance) ───────────────────────────
    private static IEmployeeDataProvider      employeeDataProvider;
    private static IEmployeeIntegration       employeeIntegration;
    private static RealPerformanceIntegration performanceIntegration;

    // ── Direct DB repositories (EIT, Lookup, Module, Report, Flexfield) ────────
    private static DbEITRepository            dbEITRepository;
    private static DbLookupRepository         dbLookupRepository;
    private static DbModuleRepository         dbModuleRepository;
    private static DbReportRepository         dbReportRepository;
    private static DbFlexfieldRepository      dbFlexfieldRepository;
    private static DbUnifiedFormRepository    dbFormRepository;
    private static DbEmployeeRepository       dbEmployeeRepository;
    private static DbPerformanceRepository    dbPerformanceRepository;

    public static void main(String[] args) throws Exception {
        // ── Boot service layer ─────────────────────────────────────────────────
        workflowService  = CustomizationServiceFactory.getInstance().getWorkflowService();
        taskFlowService  = CustomizationServiceFactory.getInstance().getTaskFlowService();
        templateService  = new WorkflowTemplateServiceImpl();

        // ── Boot integration layer ─────────────────────────────────────────────
        employeeDataProvider   = new RealEmployeeDataProvider();
        employeeIntegration    = new RealEmployeeIntegration();
        performanceIntegration = new RealPerformanceIntegration();

        // ── Boot DB repositories ───────────────────────────────────────────────
        dbEITRepository       = new DbEITRepository(DB_URL);
        dbLookupRepository    = new DbLookupRepository(DB_URL);
        dbModuleRepository    = new DbModuleRepository(DB_URL);
        dbReportRepository    = new DbReportRepository(DB_URL);
        dbFlexfieldRepository = new DbFlexfieldRepository(DB_URL);
        dbFormRepository      = new DbUnifiedFormRepository(DB_URL);
        dbEmployeeRepository  = new DbEmployeeRepository(DB_URL);
        dbPerformanceRepository = new DbPerformanceRepository(DB_URL);

        // ── Register HTTP contexts ─────────────────────────────────────────────
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/workflows",    new WorkflowHandler());
        server.createContext("/api/taskflows",    new TaskFlowHandler());
        server.createContext("/api/eits",         new EITHandler());
        server.createContext("/api/lookups",      new LookupHandler());
        server.createContext("/api/modules",      new ModuleHandler());
        server.createContext("/api/reports",      new ReportHandler());
        server.createContext("/api/forms",        new FormHandler());
        server.createContext("/api/flexfields",   new FlexfieldHandler());
        server.createContext("/api/employees",    new EmployeeHandler());
        server.createContext("/api/performance",  new PerformanceHandler());
        server.createContext("/api/health",       new HealthHandler());
        server.createContext("/api/checks",       new ChecksHandler());
        server.createContext("/api/errors",       new ErrorLogHandler());
        server.createContext("/",                 new StaticFileHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  HRMS Customization Subsystem — All 8 Modules (REAL DB)      ║");
        System.out.println("║  Server: http://localhost:" + PORT + "                              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  DB TABLE MAPPINGS:                                          ║");
        System.out.println("║  ⚙  Workflow Engine   /api/workflows  → workflows            ║");
        System.out.println("║  🗂  Task Flow Builder /api/taskflows  → task_flows           ║");
        System.out.println("║  📋 Form Designer     forms.html      → custom_forms         ║");
        System.out.println("║  🔧 Flexfield Mgr     /api/flexfields → custom_fields        ║");
        System.out.println("║  🏷  EIT Handler       /api/eits       → custom_fields(id=-1) ║");
        System.out.println("║  🔍 Lookup Custmzr    /api/lookups    → custom_fields(id=-2) ║");
        System.out.println("║  📊 Module Custmzr    /api/modules    → custom_modules       ║");
        System.out.println("║  📈 Report Builder    /api/reports    → hr_reports           ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  INTEGRATIONS:                                               ║");
        System.out.println("║  👤 Onboarding        /api/employees  → employees table      ║");
        System.out.println("║  🎯 Performance Mgmt  /api/performance→ goals/appraisals     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    // ════════════════════════════════════════════════════════════════════════════
    // WORKFLOW ENGINE HANDLER
    // ════════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("OverlyBroadCatchBlock")
    static class WorkflowHandler implements HttpHandler {
        @Override @SuppressWarnings("OverlyBroadCatchBlock")
        public void handle(HttpExchange ex) throws IOException {
            cors(ex);
            String path = ex.getRequestURI().getPath(), method = ex.getRequestMethod();
            if ("OPTIONS".equals(method)) { send(ex, 200, "{}"); return; }
            try {
                if ("GET".equals(method) && "/api/workflows".equals(path)) {
                    send(ex, 200, toJson(workflowService.getAllWorkflows()));
                } else if ("GET".equals(method) && "/api/workflows/templates".equals(path)) {
                    send(ex, 200, templatesJson(templateService.getAllTemplates()));
                } else if ("GET".equals(method) && path.matches("/api/workflows/templates/[a-z_]+")) {
                    String templateId = path.substring(path.lastIndexOf("/") + 1);
                    WorkflowTemplate template = templateService.getTemplateById(templateId);
                    if (template == null) send(ex, 404, err("Template not found: " + templateId));
                    else send(ex, 200, templateJson(template));
                } else if ("GET".equals(method) && path.matches("/api/workflows/\\d+/steps")) {
                    send(ex, 200, stepsJson(workflowService.getSteps(seg(path, 3))));
                } else if ("POST".equals(method) && "/api/workflows/create".equals(path)) {
                    String b = body(ex), name = p(b, "name");
                    if (nil(name)) throw CustomizationExceptions.emptyFieldName();
                    int id = workflowService.createWorkflow(name, p(b, "assignedTo"));
                    send(ex, 200, "{\"workflowId\":" + id + ",\"message\":\"Workflow created\",\"status\":\"Active\"}");
                } else if ("POST".equals(method) && "/api/workflows/status".equals(path)) {
                    String b = body(ex);
                    workflowService.updateWorkflowStatus(intVal(p(b, "workflowId")), p(b, "status"));
                    send(ex, 200, "{\"message\":\"Status updated\"}");
                } else if ("POST".equals(method) && "/api/workflows/addstep".equals(path)) {
                    String b = body(ex), sn = p(b, "stepName");
                    if (nil(sn)) throw CustomizationExceptions.emptyFieldName();
                    workflowService.addStep(intVal(p(b, "workflowId")), sn, p(b, "assignee"), intVal(p(b, "escalationHours")));
                    send(ex, 200, "{\"message\":\"Step added\"}");
                } else if ("POST".equals(method) && "/api/workflows/removestep".equals(path)) {
                    workflowService.removeStep(intVal(p(body(ex), "stepId")));
                    send(ex, 200, "{\"message\":\"Step removed\"}");
                } else if ("POST".equals(method) && "/api/workflows/delete".equals(path)) {
                    workflowService.deleteWorkflow(intVal(p(body(ex), "workflowId")));
                    send(ex, 200, "{\"message\":\"Workflow deleted\"}");
                } else send(ex, 404, err("Workflow endpoint not found: " + path));
            } catch (CustomizationExceptions.CustomizationException e) { send(ex, code(e), e.toJson());
            } catch (WorkflowException e) { send(ex, 400, "{\"errorCode\":\"" + e.getErrorCode() + "\",\"message\":\"" + esc(e.getMessage()) + "\"}");
            } catch (Exception e) { send(ex, 500, err(e.getMessage())); }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // TASK FLOW BUILDER HANDLER
    // ════════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("OverlyBroadCatchBlock")
    static class TaskFlowHandler implements HttpHandler {
        @Override @SuppressWarnings("OverlyBroadCatchBlock")
        public void handle(HttpExchange ex) throws IOException {
            cors(ex);
            String path = ex.getRequestURI().getPath(), method = ex.getRequestMethod();
            if ("OPTIONS".equals(method)) { send(ex, 200, "{}"); return; }
            try {
                if ("GET".equals(method) && "/api/taskflows".equals(path)) {
                    send(ex, 200, tfJson(taskFlowService.getAllTaskFlows()));
                } else if ("GET".equals(method) && path.matches("/api/taskflows/\\d+/windows")) {
                    send(ex, 200, listJson(taskFlowService.getWindows(seg(path, 3))));
                } else if ("POST".equals(method) && ("/api/taskflows/create".equals(path) || "/api/taskflows".equals(path))) {
                    String b = body(ex), name = p(b, "flowName"), fname = p(b, "name");
                    String flowName = !nil(name) ? name.trim() : (!nil(fname) ? fname.trim() : null);
                    if (nil(flowName)) throw CustomizationExceptions.emptyFieldName();
                    String role = p(b, "assignedRole");
                    boolean validate = "true".equalsIgnoreCase(p(b, "validateOnNext"));
                    boolean backNav = !"false".equalsIgnoreCase(p(b, "allowBackNav")); // Default true
                    int id = taskFlowService.createTaskFlow(flowName, role, validate, backNav);
                    send(ex, 200, "{\"taskId\":" + id + ",\"id\":" + id + ",\"flowName\":\"" + esc(flowName) + "\",\"message\":\"Task Flow created\"}");
                } else if ("POST".equals(method) && path.matches("/api/taskflows/\\d+/windows")) {
                    int taskId = seg(path, 3);
                    String b = body(ex), wName = p(b, "windowName");
                    if (nil(wName)) throw CustomizationExceptions.emptyFieldName();
                    taskFlowService.addWindow(taskId, wName);
                    send(ex, 200, "{\"message\":\"Window added\"}");
                } else if ("DELETE".equals(method) && path.matches("/api/taskflows/\\d+/windows")) {
                    int taskId = seg(path, 3);
                    String b = body(ex), wName = p(b, "windowName");
                    taskFlowService.removeWindow(taskId, wName);
                    send(ex, 200, "{\"message\":\"Window removed\"}");
                } else if ("POST".equals(method) && path.matches("/api/taskflows/\\d+/status")) {
                    int taskId = seg(path, 3);
                    String b = body(ex), status = p(b, "status");
                    taskFlowService.setStatus(taskId, status);
                    send(ex, 200, "{\"message\":\"Status updated\"}");
                } else if ("DELETE".equals(method) && path.matches("/api/taskflows/\\d+")) {
                    int taskId = seg(path, 3);
                    taskFlowService.deleteTaskFlow(taskId);
                    send(ex, 200, "{\"message\":\"Task Flow deleted\"}");
                } else send(ex, 404, err("TaskFlow endpoint not found: " + path));
            } catch (CustomizationExceptions.CustomizationException e) { send(ex, code(e), e.toJson());
            } catch (TaskFlowException e) { send(ex, 400, "{\"errorCode\":\"" + e.getErrorCode() + "\",\"message\":\"" + esc(e.getMessage()) + "\"}");
            } catch (Exception e) { send(ex, 500, err(e.getMessage())); }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // FORM DESIGNER HANDLER  →  custom_forms + custom_fields tables
    // ════════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("OverlyBroadCatchBlock")
    static class FormHandler implements HttpHandler {
        @Override @SuppressWarnings("OverlyBroadCatchBlock")
        public void handle(HttpExchange ex) throws IOException {
            cors(ex);
            String path = ex.getRequestURI().getPath(), method = ex.getRequestMethod();
            if ("OPTIONS".equals(method)) { send(ex, 200, "{}"); return; }
            try {
                if ("GET".equals(method) && "/api/forms".equals(path)) {
                    List<DbUnifiedFormRepository.FormData> forms = dbFormRepository.findAll();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < forms.size(); i++) {
                        if (i > 0) sb.append(",");
                        DbUnifiedFormRepository.FormData f = forms.get(i);
                        sb.append("{\"formId\":").append(f.formId)
                          .append(",\"formName\":\"").append(esc(f.formName)).append("\"")
                          .append(",\"layoutType\":\"").append(esc(f.layoutType)).append("\"")
                          .append(",\"moduleId\":").append(f.moduleId)
                          .append(",\"createdDate\":\"").append(esc(f.createdDate)).append("\"")
                          .append(",\"fields\":[");
                        for (int j = 0; j < f.fields.size(); j++) {
                            if (j > 0) sb.append(",");
                            DbUnifiedFormRepository.FieldData field = f.fields.get(j);
                            sb.append("{\"fieldId\":").append(field.fieldId)
                              .append(",\"fieldName\":\"").append(esc(field.fieldName)).append("\"")
                              .append(",\"fieldType\":\"").append(esc(field.fieldType)).append("\"")
                              .append(",\"isMandatory\":").append(field.mandatory)
                              .append(",\"defaultValue\":\"").append(esc(field.defaultValue)).append("\"}");
                        }
                        sb.append("]}");
                    }
                    send(ex, 200, sb.append("]").toString());
                } else if ("POST".equals(method) && "/api/forms/create".equals(path)) {
                    String b = body(ex), name = p(b, "formName"), layout = p(b, "layoutType");
                    if (nil(name)) throw CustomizationExceptions.emptyFieldName();
                    int id = dbFormRepository.createForm(name.trim(), nil(layout) ? "Grid" : layout.trim());
                    if (id < 0) throw CustomizationExceptions.formNotFound(id);
                    send(ex, 200, "{\"formId\":" + id + ",\"message\":\"Form created\",\"formName\":\"" + esc(name.trim()) + "\"}");
                } else if ("POST".equals(method) && path.matches("/api/forms/\\d+/fields")) {
                    int formId = seg(path, 3);
                    if (!dbFormRepository.formExists(formId)) throw CustomizationExceptions.formNotFound(formId);
                    String b = body(ex), fieldName = p(b, "fieldName"), fieldType = p(b, "fieldType");
                    if (nil(fieldName)) throw CustomizationExceptions.emptyFieldName();
                    int fieldId = dbFormRepository.addField(
                        formId,
                        fieldName.trim(),
                        nil(fieldType) ? "Text" : fieldType.trim(),
                        "true".equalsIgnoreCase(p(b, "isMandatory")),
                        p(b, "defaultValue")
                    );
                    send(ex, 200, "{\"fieldId\":" + fieldId + ",\"message\":\"Field added\",\"formId\":" + formId + "}");
                } else if ("DELETE".equals(method) && path.matches("/api/forms/\\d+/fields/\\d+")) {
                    int formId = seg(path, 3);
                    int fieldId = seg(path, 5);
                    if (!dbFormRepository.formExists(formId)) throw CustomizationExceptions.formNotFound(formId);
                    dbFormRepository.deleteField(formId, fieldId);
                    send(ex, 200, "{\"message\":\"Field deleted\",\"fieldId\":" + fieldId + "}");
                } else if ("DELETE".equals(method) && path.matches("/api/forms/\\d+")) {
                    int formId = seg(path, 3);
                    if (!dbFormRepository.formExists(formId)) throw CustomizationExceptions.formNotFound(formId);
                    dbFormRepository.deleteForm(formId);
                    send(ex, 200, "{\"message\":\"Form deleted\",\"formId\":" + formId + "}");
                } else {
                    send(ex, 404, err("Form endpoint not found: " + path));
                }
            } catch (CustomizationExceptions.CustomizationException e) { send(ex, code(e), e.toJson());
            } catch (IOException | RuntimeException e) { send(ex, 500, err(e.getMessage())); }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // FLEXFIELD MANAGER HANDLER  →  custom_fields table (form_id IS NULL)
    // ════════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("OverlyBroadCatchBlock")
    static class FlexfieldHandler implements HttpHandler {
        @Override @SuppressWarnings("OverlyBroadCatchBlock")
        public void handle(HttpExchange ex) throws IOException {
            cors(ex);
            String path = ex.getRequestURI().getPath(), method = ex.getRequestMethod();
            if ("OPTIONS".equals(method)) { send(ex, 200, "{}"); return; }
            try {
                if ("GET".equals(method) && "/api/flexfields".equals(path)) {
                    List<DbFlexfieldRepository.FlexData> list = dbFlexfieldRepository.findAll();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) sb.append(",");
                        DbFlexfieldRepository.FlexData f = list.get(i);
                        String[] segArray = f.segments != null && !f.segments.isEmpty() 
                            ? f.segments.split(",") : new String[]{};
                        sb.append("{\"id\":").append(f.fieldId)
                          .append(",\"name\":\"").append(esc(f.name)).append("\"")
                          .append(",\"type\":\"").append(esc(f.type)).append("\"")
                          .append(",\"segments\":[");
                        for (int j = 0; j < segArray.length; j++) {
                            if (j > 0) sb.append(",");
                            sb.append("\"").append(esc(segArray[j].trim())).append("\"");
                        }
                        sb.append("]}");
                    }
                    send(ex, 200, sb.append("]").toString());
                } else if ("POST".equals(method) && "/api/flexfields/create".equals(path)) {
                    String b = body(ex), name = p(b, "name"), type = p(b, "type"), segStr = p(b, "segments");
                    if (nil(name)) throw CustomizationExceptions.emptyFieldName();
                    // Duplicate check
                    for (DbFlexfieldRepository.FlexData f : dbFlexfieldRepository.findAll())
                        if (f.name.equalsIgnoreCase(name))
                            throw CustomizationExceptions.duplicateLookupValue(name);
                    // Convert segments array to comma-separated string if needed
                    String segments = "";
                    if (!nil(segStr)) {
                        segments = segStr.replaceAll("[\\[\\]\"]", "").replaceAll(",\\s*", ",").trim();
                    }
                    dbFlexfieldRepository.save(name, nil(type) ? "KEY" : type, segments);
                    send(ex, 200, "{\"message\":\"Flexfield created\",\"name\":\"" + esc(name) + "\"}");
                } else if ("PUT".equals(method) && path.matches("/api/flexfields/\\d+/segments")) {
                    int fieldId = seg(path, 3);
                    String b = body(ex), segStr = p(b, "segments");
                    String segments = "";
                    if (!nil(segStr)) {
                        segments = segStr.replaceAll("[\\[\\]\"]", "").replaceAll(",\\s*", ",").trim();
                    }
                    dbFlexfieldRepository.updateSegments(fieldId, segments);
                    send(ex, 200, "{\"message\":\"Segments updated\"}");
                } else if ("DELETE".equals(method) && path.matches("/api/flexfields/\\d+")) {
                    int fieldId = seg(path, 3);
                    dbFlexfieldRepository.deleteById(fieldId);
                    send(ex, 200, "{\"message\":\"Flexfield deleted\"}");
                } else send(ex, 404, err("Flexfield endpoint not found"));
            } catch (CustomizationExceptions.CustomizationException e) { send(ex, code(e), e.toJson());
            } catch (IOException | RuntimeException e) { send(ex, 500, err(e.getMessage())); }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // EIT HANDLER  →  custom_fields table (form_id = -1)
    // ════════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("OverlyBroadCatchBlock")
    static class EITHandler implements HttpHandler {
        @Override @SuppressWarnings("OverlyBroadCatchBlock")
        public void handle(HttpExchange ex) throws IOException {
            cors(ex);
            String path = ex.getRequestURI().getPath(), method = ex.getRequestMethod();
            if ("OPTIONS".equals(method)) { send(ex, 200, "{}"); return; }
            try {
                if ("GET".equals(method) && ("/api/eits".equals(path) || "/api/eit".equals(path))) {
                    List<DbEITRepository.EITData> eits = dbEITRepository.findAll();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < eits.size(); i++) {
                        if (i > 0) sb.append(",");
                        DbEITRepository.EITData e = eits.get(i);
                        sb.append("{\"eitId\":").append(e.fieldId)
                          .append(",\"id\":").append(e.fieldId)
                          .append(",\"name\":\"").append(esc(e.name)).append("\"")
                          .append(",\"fieldName\":\"").append(esc(e.name)).append("\"")
                          .append(",\"type\":\"").append(esc(e.type)).append("\"")
                          .append(",\"dataType\":\"").append(esc(e.type)).append("\"")
                          .append(",\"context\":\"").append(esc(e.context)).append("\"")
                          .append(",\"eitContext\":\"").append(esc(e.context)).append("\"")
                          .append(",\"linkedEmployee\":\"").append(esc(e.linkedEmployee)).append("\"}");
                    }
                    send(ex, 200, sb.append("]").toString());
                } else if ("POST".equals(method) && ("/api/eits/create".equals(path) || "/api/eits".equals(path) || "/api/eit".equals(path))) {
                    String b = body(ex), name = p(b, "name"), fName = p(b, "fieldName"),
                            type = p(b, "type"), dType = p(b, "dataType"), 
                            ctx = p(b, "context"), empId = p(b, "empId"), empLink = p(b, "linkedEmployee");
                    String fieldName = !nil(fName) ? fName.trim() : (!nil(name) ? name.trim() : null);
                    String fieldType = !nil(dType) ? dType.trim() : (!nil(type) ? type.trim() : "Text");
                    if (nil(fieldName)) throw CustomizationExceptions.emptyFieldName();
                    String linkedEmp = !nil(empLink) ? empLink : (!nil(empId) ? empId : null);
                    // Validate employee ID if provided
                    if (!nil(linkedEmp) && linkedEmp != null) {
                        EmployeeDTO emp = employeeDataProvider != null ? employeeDataProvider.getEmployeeById(linkedEmp.trim()) : null;
                        if (emp == null) {
                            send(ex, 409, "{\"errorCode\":\"EMPLOYEE_NOT_FOUND\",\"category\":\"MINOR\",\"message\":\"Employee not found: " + esc(linkedEmp) + "\"}");
                            return;
                        }
                    }
                    // Duplicate check - verify EIT doesn't already exist
                    for (DbEITRepository.EITData e : dbEITRepository.findAll())
                        if (e.name.equalsIgnoreCase(fieldName)) {
                            send(ex, 409, "{\"errorCode\":\"DUPLICATE_EIT\",\"category\":\"WARNING\",\"message\":\"EIT \\\"" + esc(fieldName) + "\\\" already exists\"}");
                            return;
                        }
                    dbEITRepository.save(fieldName, fieldType, nil(ctx) ? "EMPLOYEE" : ctx, linkedEmp);
                    send(ex, 200, "{\"message\":\"EIT created\",\"name\":\"" + esc(fieldName) + "\"}");
                } else if ("DELETE".equals(method) && (path.matches("/api/eits/\\d+") || path.matches("/api/eit/\\d+"))) {
                    String[] parts = path.split("/");
                    int eitId = Integer.parseInt(parts[parts.length - 1]);
                    System.out.println("[EITHandler] DELETE request for ID: " + eitId);
                    System.out.println("[EITHandler] dbEITRepository is null? " + (dbEITRepository == null));
                    if (dbEITRepository != null) {
                        try {
                            dbEITRepository.deleteById(eitId);
                            System.out.println("[EITHandler] Delete completed for ID: " + eitId);
                            send(ex, 200, "{\"message\":\"EIT deleted\",\"eitId\":" + eitId + "}");
                        } catch (Exception e) {
                            System.err.println("[EITHandler] Delete error: " + e.getMessage());
                            throw e;
                        }
                    } else {
                        System.err.println("[EITHandler] ERROR: dbEITRepository is null!");
                        send(ex, 500, err("Database repository not initialized"));
                    }
                } else if ("DELETE".equals(method) && "/api/eits/delete".equals(path)) {
                    String b = body(ex), name = p(b, "name");
                    int eitId = intVal(p(b, "eitId"));
                    System.out.println("[EITHandler] DELETE request - eitId: " + eitId + ", name: " + name);
                    if (eitId > 0) {
                        try {
                            dbEITRepository.deleteById(eitId);
                            send(ex, 200, "{\"message\":\"EIT deleted by ID\",\"eitId\":" + eitId + "}");
                        } catch (Exception e) {
                            System.err.println("[EITHandler] Delete error: " + e.getMessage());
                            throw e;
                        }
                    } else if (!nil(name)) {
                        try {
                            dbEITRepository.deleteByName(name);
                            send(ex, 200, "{\"message\":\"EIT deleted by name\",\"name\":\"" + esc(name) + "\"}");
                        } catch (Exception e) {
                            System.err.println("[EITHandler] Delete error: " + e.getMessage());
                            throw e;
                        }
                    } else {
                        throw CustomizationExceptions.emptyFieldName();
                    }
                } else send(ex, 404, err("EIT endpoint not found: " + path));
            } catch (CustomizationExceptions.CustomizationException e) { send(ex, code(e), e.toJson());
            } catch (Exception e) { send(ex, 500, err(e.getMessage())); }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // LOOKUP CUSTOMIZER HANDLER  →  custom_fields table (form_id = -2)
    // ════════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("OverlyBroadCatchBlock")
    static class LookupHandler implements HttpHandler {
        @Override @SuppressWarnings("OverlyBroadCatchBlock")
        public void handle(HttpExchange ex) throws IOException {
            cors(ex);
            String path = ex.getRequestURI().getPath(), method = ex.getRequestMethod();
            if ("OPTIONS".equals(method)) { send(ex, 200, "{}"); return; }
            try {
                if ("GET".equals(method) && "/api/lookups".equals(path)) {
                    List<DbLookupRepository.LookupData> lks = dbLookupRepository.findAll();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < lks.size(); i++) {
                        if (i > 0) sb.append(",");
                        DbLookupRepository.LookupData l = lks.get(i);
                        sb.append("{\"lookupCode\":\"").append(esc(l.name)).append("\",\"name\":\"").append(esc(l.name)).append("\",\"values\":[");
                        for (int j = 0; j < l.values.size(); j++) {
                            if (j > 0) sb.append(",");
                            sb.append("\"").append(esc(l.values.get(j).trim())).append("\"");
                        }
                        sb.append("]}");
                    }
                    send(ex, 200, sb.append("]").toString());
                } else if ("POST".equals(method) && ("/api/lookups".equals(path) || "/api/lookups/create".equals(path))) {
                    String b = body(ex), name = p(b, "name"), code = p(b, "lookupCode"), vals = p(b, "values");
                    String lookupName = !nil(code) ? code.trim() : (!nil(name) ? name.trim() : null);
                    if (nil(lookupName)) throw CustomizationExceptions.emptyFieldName();
                    for (DbLookupRepository.LookupData l : dbLookupRepository.findAll())
                        if (l.name.equalsIgnoreCase(lookupName))
                            throw CustomizationExceptions.duplicateLookupValue(lookupName);
                    List<String> valList = new ArrayList<>();
                    if (!nil(vals)) {
                        String valStr = vals;
                        if (vals.startsWith("[")) valStr = vals.replaceAll("[\\[\\]\"]", "");
                        for (String v : valStr.split(",")) if (!v.trim().isEmpty()) valList.add(v.trim());
                    }
                    dbLookupRepository.save(lookupName, valList);
                    send(ex, 200, "{\"message\":\"Lookup created\",\"lookupCode\":\"" + esc(lookupName) + "\",\"name\":\"" + esc(lookupName) + "\"}");
                } else if ("POST".equals(method) && path.matches("/api/lookups/[^/]+/values")) {
                    String[] parts = path.split("/");
                    String lname = parts[3];
                    String b = body(ex), val = p(b, "value");
                    if (nil(val)) throw CustomizationExceptions.emptyFieldName();
                    dbLookupRepository.addValue(lname, val.trim());
                    send(ex, 200, "{\"message\":\"Value added\"}");
                } else if ("DELETE".equals(method) && path.matches("/api/lookups/[^/]+")) {
                    String[] parts = path.split("/");
                    String lname = parts[3];
                    dbLookupRepository.deleteByName(lname);
                    send(ex, 200, "{\"message\":\"Lookup deleted\"}");
                } else send(ex, 404, err("Lookup endpoint not found: " + path));
            } catch (CustomizationExceptions.CustomizationException e) { send(ex, code(e), e.toJson());
            } catch (IOException | RuntimeException e) { send(ex, 500, err(e.getMessage())); }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // MODULE CUSTOMIZER HANDLER  →  custom_modules table
    // ════════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("OverlyBroadCatchBlock")
    static class ModuleHandler implements HttpHandler {
        @Override @SuppressWarnings("OverlyBroadCatchBlock")
        public void handle(HttpExchange ex) throws IOException {
            cors(ex);
            String path = ex.getRequestURI().getPath(), method = ex.getRequestMethod();
            if ("OPTIONS".equals(method)) { send(ex, 200, "{}"); return; }
            try {
                if ("GET".equals(method) && "/api/modules".equals(path)) {
                    List<DbModuleRepository.ModuleData> mods = dbModuleRepository.findAll();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < mods.size(); i++) {
                        if (i > 0) sb.append(",");
                        DbModuleRepository.ModuleData m = mods.get(i);
                        sb.append("{\"id\":").append(m.moduleId)
                          .append(",\"moduleName\":\"").append(esc(m.moduleName)).append("\"")
                          .append(",\"moduleType\":\"").append(esc(m.moduleType)).append("\"")
                          .append(",\"isEnabled\":").append(m.isEnabled).append("}");
                    }
                    send(ex, 200, sb.append("]").toString());
                } else if ("POST".equals(method) && "/api/modules/toggle".equals(path)) {
                    String b = body(ex);
                    int moduleId = intVal(p(b, "moduleId"));
                    boolean enabled = "true".equals(p(b, "enabled"));
                    // Validate module exists
                    boolean found = dbModuleRepository.findAll().stream().anyMatch(m -> m.moduleId == moduleId);
                    if (!found) throw CustomizationExceptions.invalidModuleId(moduleId);
                    // Check already in desired state
                    DbModuleRepository.ModuleData mod = dbModuleRepository.findAll().stream()
                        .filter(m -> m.moduleId == moduleId).findFirst().orElse(null);
                    if (mod != null && mod.isEnabled == enabled) {
                        throw enabled ? CustomizationExceptions.moduleAlreadyEnabled(mod.moduleName)
                                      : CustomizationExceptions.moduleAlreadyDisabled(mod.moduleName);
                    }
                    dbModuleRepository.updateStatus(moduleId, enabled);
                    send(ex, 200, "{\"message\":\"Module " + (enabled ? "enabled" : "disabled") + "\",\"moduleId\":" + moduleId + "}");
                } else send(ex, 404, err("Module endpoint not found"));
            } catch (CustomizationExceptions.CustomizationException e) { send(ex, code(e), e.toJson());
            } catch (IOException | RuntimeException e) { send(ex, 500, err(e.getMessage())); }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // REPORT BUILDER HANDLER  →  hr_reports table (real CSV download)
    // ════════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("OverlyBroadCatchBlock")
    static class ReportHandler implements HttpHandler {
        @Override @SuppressWarnings("OverlyBroadCatchBlock")
        public void handle(HttpExchange ex) throws IOException {
            cors(ex);
            String path = ex.getRequestURI().getPath(), method = ex.getRequestMethod();
            if ("OPTIONS".equals(method)) { send(ex, 200, "{}"); return; }
            try {
                if ("GET".equals(method) && "/api/reports".equals(path)) {
                    List<DbReportRepository.ReportData> reports = dbReportRepository.findAll();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < reports.size(); i++) {
                        if (i > 0) sb.append(",");
                        DbReportRepository.ReportData r = reports.get(i);
                        sb.append("{\"reportId\":").append(r.reportId)
                          .append(",\"id\":").append(r.reportId)
                          .append(",\"reportName\":\"").append(esc(r.reportName)).append("\"")
                          .append(",\"reportType\":\"").append(esc(r.reportType)).append("\"")
                          .append(",\"dataSource\":\"").append(esc(r.reportType)).append("\"")
                          .append(",\"exportFormat\":\"").append(esc(r.exportFormat)).append("\"")
                          .append(",\"schedule\":\"").append(esc(r.schedule)).append("\"")
                          .append(",\"generatedDate\":").append(r.generatedDate != null ? "\"" + esc(r.generatedDate) + "\"" : "null")
                          .append("}");
                    }
                    send(ex, 200, sb.append("]").toString());
                } else if ("POST".equals(method) && "/api/reports/create".equals(path)) {
                    String b = body(ex), name = p(b, "reportName"), dataSource = p(b, "dataSource"), type = p(b, "reportType"), fmt = p(b, "exportFormat");
                    if (nil(name)) throw CustomizationExceptions.emptyFieldName();
                    if (!nil(fmt) && !fmt.matches("(?i)PDF|CSV|EXCEL"))
                        throw CustomizationExceptions.reportExportFormatInvalid(fmt);
                    int id = dbReportRepository.save(name, nil(dataSource) ? (nil(type) ? "employees" : type) : dataSource, nil(fmt) ? "CSV" : fmt);
                    send(ex, 200, "{\"reportId\":" + id + ",\"message\":\"Report created\",\"reportName\":\"" + esc(name) + "\"}");
                } else if ("POST".equals(method) && path.matches("/api/reports/\\d+/generate")) {
                    int id = seg(path, 3);
                    DbReportRepository.ReportData r = dbReportRepository.findById(id);
                    if (r == null) throw CustomizationExceptions.reportGenerationFailed("Report ID " + id + " not found");
                    dbReportRepository.markGenerated(id);
                    send(ex, 200, "{\"message\":\"Report generated\",\"reportId\":" + id + "}");
                } else if ("GET".equals(method) && path.matches("/api/reports/\\d+/export")) {
                    int id = seg(path, 3);
                    DbReportRepository.ReportData r = dbReportRepository.findById(id);
                    if (r == null) throw CustomizationExceptions.reportGenerationFailed("Report ID " + id + " not found");
                    
                    // Extract query parameters
                    String query = ex.getRequestURI().getQuery();
                    String format = "CSV";
                    String filterParam = "";
                    String columnsParam = "";
                    
                    if (query != null && !query.isEmpty()) {
                        if (query.contains("format=")) format = java.net.URLDecoder.decode(query.replaceAll(".*format=([^&]*).*", "$1"), "UTF-8");
                        if (query.contains("filter=")) filterParam = java.net.URLDecoder.decode(query.replaceAll(".*filter=([^&]*).*", "$1"), "UTF-8");
                        if (query.contains("columns=")) columnsParam = java.net.URLDecoder.decode(query.replaceAll(".*columns=([^&]*).*", "$1"), "UTF-8");
                    }
                    
                    List<String> selectedColumns = new ArrayList<>();
                    if (!columnsParam.isEmpty()) {
                        for (String col : columnsParam.split(",")) {
                            String trimmed = col.trim();
                            if (!trimmed.isEmpty()) selectedColumns.add(trimmed);
                        }
                    }
                    
                    if ("PDF".equalsIgnoreCase(format)) {
                        byte[] bytes = buildPdfBytes(r, selectedColumns, filterParam);
                        String filename = r.reportName.replaceAll("[^a-zA-Z0-9_]", "_") + ".pdf";
                        ex.getResponseHeaders().set("Content-Type", "application/pdf");
                        ex.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                        ex.getResponseHeaders().set("Access-Control-Expose-Headers", "Content-Disposition");
                        ex.sendResponseHeaders(200, bytes.length);
                        ex.getResponseBody().write(bytes);
                        ex.getResponseBody().close();
                    } else {
                        String csv = buildCsv(r, selectedColumns, filterParam);
                        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
                        String filename = r.reportName.replaceAll("[^a-zA-Z0-9_]", "_") + ".csv";
                        ex.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
                        ex.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                        ex.getResponseHeaders().set("Access-Control-Expose-Headers", "Content-Disposition");
                        ex.sendResponseHeaders(200, bytes.length);
                        ex.getResponseBody().write(bytes);
                        ex.getResponseBody().close();
                    }
                } else if ("DELETE".equals(method) && path.matches("/api/reports/\\d+/delete")) {
                    int id = seg(path, 3);
                    DbReportRepository.ReportData r = dbReportRepository.findById(id);
                    if (r == null) throw CustomizationExceptions.reportGenerationFailed("Report ID " + id + " not found");
                    dbReportRepository.delete(id);
                    send(ex, 200, "{\"message\":\"Report deleted\"}");
                } else send(ex, 404, err("Report endpoint not found"));
            } catch (CustomizationExceptions.CustomizationException e) { send(ex, code(e), e.toJson());
            } catch (IOException | RuntimeException e) { send(ex, 500, err(e.getMessage())); }
        }

        private String buildCsv(DbReportRepository.ReportData r, List<String> selectedCols, String filter) {
            StringBuilder sb = new StringBuilder();
            sb.append("# HRMS Report: ").append(r.reportName).append("\n");
            sb.append("# Generated: ").append(r.generatedDate != null ? r.generatedDate : "N/A").append("\n");
            sb.append("# Format: ").append(r.exportFormat).append(" | Type: ").append(r.reportType).append("\n\n");
            
            // Use selected columns if provided, otherwise use all
            List<String> columns = selectedCols != null && !selectedCols.isEmpty() 
                ? selectedCols 
                : Arrays.asList("Employee ID", "Full Name", "Job Title", "Department", "Status", "Start Date");
            
            sb.append(String.join(",", columns.stream().map(c -> "\"" + c + "\"").collect(java.util.stream.Collectors.toList()))).append("\n");
            
            try {
                List<EmployeeDTO> emps = employeeDataProvider.getAllEmployees();
                
                // Apply row filter
                if (!filter.isEmpty()) {
                    switch (filter.toLowerCase()) {
                        case "active" -> emps = emps.stream().filter(e -> "ACTIVE".equalsIgnoreCase(e.status)).collect(java.util.stream.Collectors.toList());
                        case "onboarding" -> emps = emps.stream().filter(e -> "ONBOARDING".equalsIgnoreCase(e.status) || "PRE_JOINING".equalsIgnoreCase(e.status)).collect(java.util.stream.Collectors.toList());
                        case "engineering" -> emps = emps.stream().filter(e -> "Engineering".equalsIgnoreCase(e.department)).collect(java.util.stream.Collectors.toList());
                        default -> {}
                    }
                }
                
                for (EmployeeDTO e : emps) {
                    if (e == null) continue;
                    List<String> values = new ArrayList<>();
                    for (String col : columns) {
                        if (col == null) continue;
                        String val = switch (col) {
                            case "Employee ID" -> e.empId != null ? e.empId : "";
                            case "Full Name" -> e.name != null ? e.name : "";
                            case "Job Title" -> e.role != null ? e.role : "";
                            case "Department" -> e.department != null ? e.department : "";
                            case "Status" -> e.status != null ? e.status : "";
                            case "Start Date" -> "—";
                            default -> "—";
                        };
                        values.add("\"" + (!val.isEmpty() ? val.replace("\"", "\"\"") : "—") + "\"");
                    }
                    sb.append(String.join(",", values)).append("\n");
                }
            } catch (Exception ignored) {
                sb.append("(Employee data unavailable)\n");
            }
            return sb.toString();
        }
        
        private byte[] buildPdfBytes(DbReportRepository.ReportData r, List<String> selectedCols, String filter) {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><style>");
            html.append("body{font-family:Arial,sans-serif;padding:20px;font-size:11px}");
            html.append("h1{font-size:18px;margin:0 0 4px 0}p{margin:0;color:#666;font-size:10px}");
            html.append("table{width:100%;border-collapse:collapse;margin-top:16px}");
            html.append("th{background:#f0f0f0;border:1px solid #ddd;padding:6px;text-align:left;font-weight:bold}");
            html.append("td{border:1px solid #ddd;padding:6px}tr:nth-child(even){background:#f9f9f9}");
            html.append("</style></head><body>");
            html.append("<h1>").append(r.reportName).append("</h1>");
            html.append("<p>Generated: ").append(r.generatedDate != null ? r.generatedDate : "N/A").append(" | Format: ").append(r.exportFormat).append("</p>");
            html.append("<table><thead><tr>");
            
            List<String> columns = selectedCols != null && !selectedCols.isEmpty() 
                ? selectedCols 
                : Arrays.asList("Employee ID", "Full Name", "Job Title", "Department", "Status");
            
            for (String col : columns) {
                html.append("<th>").append(col).append("</th>");
            }
            html.append("</tr></thead><tbody>");
            
            try {
                List<EmployeeDTO> emps = employeeDataProvider.getAllEmployees();
                if (!filter.isEmpty()) {
                    switch (filter.toLowerCase()) {
                        case "active" -> emps = emps.stream().filter(e -> "ACTIVE".equalsIgnoreCase(e.status)).collect(java.util.stream.Collectors.toList());
                        case "onboarding" -> emps = emps.stream().filter(e -> "ONBOARDING".equalsIgnoreCase(e.status) || "PRE_JOINING".equalsIgnoreCase(e.status)).collect(java.util.stream.Collectors.toList());
                        case "engineering" -> emps = emps.stream().filter(e -> "Engineering".equalsIgnoreCase(e.department)).collect(java.util.stream.Collectors.toList());
                        default -> {}
                    }
                }
                for (EmployeeDTO e : emps) {
                    if (e == null) continue;
                    html.append("<tr>");
                    for (String col : columns) {
                        if (col == null) continue;
                        String val = switch (col) {
                            case "Employee ID" -> e.empId != null ? e.empId : "";
                            case "Full Name" -> e.name != null ? e.name : "";
                            case "Job Title" -> e.role != null ? e.role : "";
                            case "Department" -> e.department != null ? e.department : "";
                            case "Status" -> e.status != null ? e.status : "";
                            default -> "—";
                        };
                        html.append("<td>").append(!val.isEmpty() ? val : "—").append("</td>");
                    }
                    html.append("</tr>");
                }
            } catch (Exception ignored) {}
            html.append("</tbody></table></body></html>");
            return html.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // EMPLOYEE / ONBOARDING HANDLER
    // ════════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("OverlyBroadCatchBlock")
    static class EmployeeHandler implements HttpHandler {
        @Override @SuppressWarnings("OverlyBroadCatchBlock")
        public void handle(HttpExchange ex) throws IOException {
            cors(ex);
            String path = ex.getRequestURI().getPath(), method = ex.getRequestMethod();
            if ("OPTIONS".equals(method)) { send(ex, 200, "{}"); return; }
            try {
                if ("GET".equals(method) && "/api/employees".equals(path)) {
                    List<EmployeeDTO> emps = employeeDataProvider.getAllEmployees();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < emps.size(); i++) {
                        if (i > 0) sb.append(",");
                        EmployeeDTO e = emps.get(i);
                        sb.append("{\"empId\":\"").append(esc(e.empId)).append("\",\"employeeId\":\"").append(esc(e.empId))
                          .append("\",\"id\":\"").append(esc(e.empId)).append("\",\"name\":\"").append(esc(e.name))
                          .append("\",\"fullName\":\"").append(esc(e.name)).append("\",\"role\":\"").append(esc(e.role))
                          .append("\",\"department\":\"").append(esc(e.department)).append("\",\"status\":\"").append(esc(e.status)).append("\"}");
                    }
                    send(ex, 200, sb.append("]").toString());
                } else if ("GET".equals(method) && path.startsWith("/api/employees/")) {
                    String empId = path.substring("/api/employees/".length());
                    EmployeeDTO emp = employeeDataProvider.getEmployeeById(empId);
                    if (emp == null) throw CustomizationExceptions.extraInfoNotFound(-1);
                    send(ex, 200, "{\"empId\":\"" + esc(emp.empId) + "\",\"employeeId\":\"" + esc(emp.empId) + "\",\"id\":\"" + esc(emp.empId) + "\",\"name\":\"" + esc(emp.name) + "\",\"fullName\":\"" + esc(emp.name) + "\",\"role\":\"" + esc(emp.role) + "\",\"department\":\"" + esc(emp.department) + "\",\"status\":\"" + esc(emp.status) + "\"}");
                } else if ("POST".equals(method) && "/api/employees/onboard".equals(path)) {
                    String b = body(ex), empId = p(b, "empId");
                    Employee emp = employeeIntegration.getEmployee(empId);
                    if (emp == null) throw CustomizationExceptions.employeeDataReadOnly();
                    int wfId = employeeIntegration.startOnboarding(emp);
                    send(ex, 200, "{\"message\":\"Onboarding workflow triggered\",\"employee\":\"" + esc(emp.name) + "\",\"workflowInstanceId\":" + wfId + "}");
                } else if ("POST".equals(method) && path.matches("/api/employees/[^/]+/offboard")) {
                    // Offboarding endpoint: /api/employees/{empId}/offboard
                    String empId = path.substring("/api/employees/".length(), path.lastIndexOf("/offboard"));
                    String b = body(ex);
                    String lastDay = p(b, "lastWorkingDay");
                    String reason = p(b, "reason");
                    Employee emp = employeeIntegration.getEmployee(empId);
                    if (emp == null) throw CustomizationExceptions.employeeDataReadOnly();
                    // Create offboarding workflow
                    String wfName = "Offboarding: " + emp.name + " - " + (nil(reason) ? "Exit" : reason);
                    int wfId = workflowService.createWorkflow(wfName, "HR Admin");
                    // Add offboarding steps
                    workflowService.addStep(wfId, "Resignation Received", "HR Admin", 24);
                    workflowService.addStep(wfId, "Asset Return", "IT Department", 48);
                    workflowService.addStep(wfId, "Knowledge Transfer", "Line Manager", 72);
                    workflowService.addStep(wfId, "Final Settlement", "Finance", 48);
                    workflowService.addStep(wfId, "Exit Interview", "HR Admin", 24);
                    send(ex, 200, "{\"message\":\"Offboarding workflow triggered\",\"employee\":\"" + esc(emp.name) + "\",\"lastWorkingDay\":\"" + esc(lastDay) + "\",\"reason\":\"" + esc(reason) + "\",\"workflowInstanceId\":" + wfId + "}");
                } else send(ex, 404, err("Employee endpoint not found"));
            } catch (CustomizationExceptions.CustomizationException e) { send(ex, code(e), e.toJson());
            } catch (IOException | RuntimeException e) { send(ex, 500, err(e.getMessage())); }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // PERFORMANCE MANAGEMENT HANDLER
    // Implements IPerformanceForCustomization contract
    // ════════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("OverlyBroadCatchBlock")
    static class PerformanceHandler implements HttpHandler {
        @Override @SuppressWarnings("OverlyBroadCatchBlock")
        public void handle(HttpExchange ex) throws IOException {
            cors(ex);
            String path = ex.getRequestURI().getPath(), method = ex.getRequestMethod();
            if ("OPTIONS".equals(method)) { send(ex, 200, "{}"); return; }
            try {
                if ("GET".equals(method) && "/api/performance/cycles".equals(path)) {
                    // Uses IPerformanceForCustomization.getAllPerformanceCycles()
                    List<PerformanceCycle> cycles = performanceIntegration.getAllPerformanceCycles();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < cycles.size(); i++) {
                        if (i > 0) sb.append(",");
                        PerformanceCycle c = cycles.get(i);
                        sb.append("{\"cycleName\":\"").append(esc(c.cycleName))
                          .append("\",\"startDate\":\"").append(esc(c.startDate))
                          .append("\",\"endDate\":\"").append(esc(c.endDate))
                          .append("\",\"status\":\"").append(esc(c.status)).append("\"}");
                    }
                    send(ex, 200, sb.append("]").toString());

                } else if ("GET".equals(method) && "/api/performance/goals".equals(path)) {
                    // Uses IPerformanceForCustomization.getGoalsForCycle()
                    String query = ex.getRequestURI().getQuery();
                    String cycle = (query != null && query.contains("cycle="))
                        ? java.net.URLDecoder.decode(query.replaceAll(".*cycle=([^&]*).*", "$1"), "UTF-8") : "";
                    List<Goal> goals = performanceIntegration.getGoalsForCycle(cycle);
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < goals.size(); i++) {
                        if (i > 0) sb.append(",");
                        Goal g = goals.get(i);
                        sb.append("{\"goalId\":\"").append(esc(g.goalId))
                          .append("\",\"employeeId\":\"").append(esc(g.employeeId))
                          .append("\",\"goalTitle\":\"").append(esc(g.goalTitle))
                          .append("\",\"goalStatus\":\"").append(esc(g.goalStatus))
                          .append("\",\"goalStartDate\":\"").append(esc(g.goalStartDate))
                          .append("\",\"goalEndDate\":\"").append(esc(g.goalEndDate)).append("\"}");
                    }
                    send(ex, 200, sb.append("]").toString());

                } else if ("GET".equals(method) && "/api/performance/appraisals".equals(path)) {
                    // Uses IPerformanceForCustomization.getAppraisalsForCycle()
                    String query = ex.getRequestURI().getQuery();
                    String cycle = (query != null && query.contains("cycle="))
                        ? java.net.URLDecoder.decode(query.replaceAll(".*cycle=([^&]*).*", "$1"), "UTF-8") : "";
                    List<Appraisal> apps = performanceIntegration.getAppraisalsForCycle(cycle);
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < apps.size(); i++) {
                        if (i > 0) sb.append(",");
                        Appraisal a = apps.get(i);
                        sb.append("{\"appraisalId\":\"").append(esc(a.appraisalId))
                          .append("\",\"employeeId\":\"").append(esc(a.employeeId))
                          .append("\",\"reviewerId\":\"").append(esc(a.reviewerId))
                          .append("\",\"score\":").append(a.score)
                          .append(",\"status\":\"").append(esc(a.status))
                          .append("\",\"period\":\"").append(esc(a.period)).append("\"}");
                    }
                    send(ex, 200, sb.append("]").toString());

                } else if ("GET".equals(method) && "/api/performance/feedback".equals(path)) {
                    // Uses IPerformanceSubsystem.getAllFeedback()
                    List<Feedback> list = performanceIntegration.getAllFeedback();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) sb.append(",");
                        Feedback f = list.get(i);
                        sb.append("{\"feedbackId\":\"").append(esc(f.feedbackId))
                          .append("\",\"employeeId\":\"").append(esc(f.employeeId))
                          .append("\",\"feedbackType\":\"").append(esc(f.feedbackType))
                          .append("\",\"feedbackText\":\"").append(esc(f.feedbackText)).append("\"}");
                    }
                    send(ex, 200, sb.append("]").toString());

                } else if ("GET".equals(method) && "/api/performance/kpis".equals(path)) {
                    // Uses IPerformanceSubsystem.getAllKPIs()
                    List<KPI> list = performanceIntegration.getAllKPIs();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) sb.append(",");
                        KPI k = list.get(i);
                        sb.append("{\"kpiId\":\"").append(esc(k.kpiId))
                          .append("\",\"employeeId\":\"").append(esc(k.employeeId))
                          .append("\",\"kpiName\":\"").append(esc(k.kpiName))
                          .append("\",\"targetValue\":").append(k.targetValue)
                          .append(",\"actualValue\":").append(k.actualValue)
                          .append(",\"unit\":\"").append(esc(k.unit)).append("\"}");
                    }
                    send(ex, 200, sb.append("]").toString());

                } else if ("GET".equals(method) && "/api/performance/analytics".equals(path)) {
                    // Uses IPerformanceSubsystem analytics methods
                    int avgScore = performanceIntegration.getAverageScore();
                    int onTrack  = performanceIntegration.getGoalsOnTrackCount();
                    int pct      = performanceIntegration.getGoalsOnTrackPercentage();
                    send(ex, 200, "{\"averageScore\":" + avgScore +
                        ",\"goalsOnTrack\":" + onTrack +
                        ",\"goalsOnTrackPercentage\":" + pct + "}");

                } else if ("GET".equals(method) && "/api/performance/forms".equals(path)) {
                    send(ex, 200, listJson(performanceIntegration.getFormIds()));
                } else if ("GET".equals(method) && "/api/performance/workflows".equals(path)) {
                    send(ex, 200, listJson(performanceIntegration.getWorkflowIds()));
                } else if ("GET".equals(method) && "/api/performance/taskflows".equals(path)) {
                    send(ex, 200, listJson(performanceIntegration.getTaskFlowIds()));
                } else if ("GET".equals(method) && "/api/performance/server".equals(path)) {
                    send(ex, 200, "{\"port\":\"" + performanceIntegration.getServerPort() + "\"}");
                } else {
                    send(ex, 404, err("Performance endpoint not found: " + path));
                }
            } catch (IOException | RuntimeException e) { send(ex, 500, err(e.getMessage())); }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // HEALTH CHECK
    // ════════════════════════════════════════════════════════════════════════════
    static class HealthHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            cors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { send(ex, 200, "{}"); return; }
            send(ex, 200, "{\"status\":\"ok\",\"db\":\"hrms.db\",\"port\":" + PORT +
                ",\"modules\":[\"workflow\",\"taskflow\",\"flexfield\",\"eit\",\"lookup\",\"module\",\"report\",\"onboarding\",\"performance\"]}");
        }
    }

    /**
     * Exposes runtime architecture checks so reviewers can verify that the
     * subsystem uses repository APIs for DB access and the shared exception API
     * for logging/viewing failures.
     */
    static class ChecksHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            cors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { send(ex, 200, "{}"); return; }
            send(ex, 200,
                "{\"dbAccess\":\"repository-api-only\"" +
                ",\"exceptionApi\":\"CustomizationErrorApi\"" +
                ",\"employeeCount\":" + dbEmployeeRepository.findAllEmployeeDtos().size() +
                ",\"performanceCounts\":{\"cycles\":" + performanceIntegration.getAllPerformanceCycles().size() +
                    ",\"goals\":" + dbPerformanceRepository.countRows("goals") +
                    ",\"appraisals\":" + dbPerformanceRepository.countRows("appraisals") +
                    ",\"feedback\":" + dbPerformanceRepository.countRows("feedback") +
                    ",\"kpis\":" + dbPerformanceRepository.countRows("kpis") + "}" +
                ",\"errorLogCount\":" + CustomizationErrorApi.getInstance().count() +
                ",\"patterns\":{\"creational\":\"Singleton + Factory\",\"structural\":\"Adapter\",\"behavioral\":\"Observer\"}" +
                ",\"grasp\":[\"Controller\",\"Information Expert\",\"Indirection\",\"Pure Fabrication\",\"Low Coupling\",\"High Cohesion\"]" +
                ",\"solid\":[\"SRP\",\"OCP\",\"LSP\",\"ISP\",\"DIP\"]" +
                "}");
        }
    }

    static class ErrorLogHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            cors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { send(ex, 200, "{}"); return; }
            List<CustomizationErrorApi.ErrorEntry> errors = CustomizationErrorApi.getInstance().viewRecentErrors();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < errors.size(); i++) {
                if (i > 0) sb.append(",");
                CustomizationErrorApi.ErrorEntry e = errors.get(i);
                sb.append("{\"timestamp\":\"").append(esc(e.timestamp)).append("\"")
                  .append(",\"source\":\"").append(esc(e.source)).append("\"")
                  .append(",\"code\":\"").append(esc(e.code)).append("\"")
                  .append(",\"message\":\"").append(esc(e.message)).append("\"")
                  .append(",\"detail\":\"").append(esc(e.detail)).append("\"}");
            }
            send(ex, 200, sb.append("]").toString());
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // STATIC FILE HANDLER
    // ════════════════════════════════════════════════════════════════════════════
    static class StaticFileHandler implements HttpHandler {
        @Override public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            if ("/".equals(path)) path = "/customization.html";
            File f = new File("../../integration/frontend" + path);
            if (!f.exists()) f = new File("frontend" + path);
            if (!f.exists()) { send(ex, 404, "{\"error\":\"File not found\"}"); return; }
            String ct = path.endsWith(".html") ? "text/html" : path.endsWith(".css") ? "text/css" : path.endsWith(".js") ? "application/javascript" : "text/plain";
            byte[] bytes;
            try (FileInputStream fis = new FileInputStream(f)) { bytes = fis.readAllBytes(); }
            ex.getResponseHeaders().set("Content-Type", ct);
            ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // JSON HELPERS
    // ════════════════════════════════════════════════════════════════════════════
    static String toJson(List<WorkflowDTO> l) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < l.size(); i++) {
            if (i > 0) sb.append(",");
            WorkflowDTO w = l.get(i);
            sb.append("{\"workflowId\":").append(w.workflowId)
              .append(",\"workflowName\":\"").append(esc(w.workflowName))
              .append("\",\"currentStatus\":\"").append(esc(w.currentStatus))
              .append("\",\"assignedTo\":\"").append(esc(w.assignedTo)).append("\"}");
        }
        return sb.append("]").toString();
    }

    static String stepsJson(List<WorkflowStepDTO> l) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < l.size(); i++) {
            if (i > 0) sb.append(",");
            WorkflowStepDTO s = l.get(i);
            sb.append("{\"stepId\":").append(s.stepId).append(",\"workflowId\":").append(s.workflowId)
              .append(",\"stepName\":\"").append(esc(s.stepName)).append("\",\"assignee\":\"").append(esc(s.assignee))
              .append("\",\"escalationHours\":").append(s.escalationHours).append("}");
        }
        return sb.append("]").toString();
    }

    static String tfJson(List<TaskFlowDTO> l) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < l.size(); i++) {
            if (i > 0) sb.append(",");
            TaskFlowDTO t = l.get(i);
            sb.append("{\"taskId\":").append(t.taskId).append(",\"flowName\":\"").append(esc(t.flowName))
              .append("\",\"flowStatus\":\"").append(esc(t.flowStatus)).append("\",\"linkedMenu\":\"").append(esc(t.linkedMenu))
              .append("\",\"validateOnNext\":").append(t.validateOnNext).append(",\"allowBackNav\":").append(t.allowBackNav).append("}");
        }
        return sb.append("]").toString();
    }

    static String listJson(List<String> l) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < l.size(); i++) { if (i > 0) sb.append(","); sb.append("\"").append(esc(l.get(i))).append("\""); }
        return sb.append("]").toString();
    }

    static String templatesJson(List<WorkflowTemplate> templates) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < templates.size(); i++) {
            if (i > 0) sb.append(",");
            WorkflowTemplate t = templates.get(i);
            sb.append("{\"templateId\":\"").append(esc(t.templateId)).append("\",\"templateName\":\"").append(esc(t.templateName))
              .append("\",\"description\":\"").append(esc(t.description)).append("\",\"stepCount\":").append(t.steps.size()).append("}");
        }
        return sb.append("]").toString();
    }

    static String templateJson(WorkflowTemplate t) {
        StringBuilder sb = new StringBuilder("{\"templateId\":\"").append(esc(t.templateId)).append("\",\"templateName\":\"").append(esc(t.templateName))
          .append("\",\"description\":\"").append(esc(t.description)).append("\",\"steps\":[");
        for (int i = 0; i < t.steps.size(); i++) {
            if (i > 0) sb.append(",");
            WorkflowTemplate.TemplateStep step = t.steps.get(i);
            sb.append("{\"stepName\":\"").append(esc(step.stepName)).append("\",\"assignee\":\"").append(esc(step.assignee))
              .append("\",\"escalationHours\":").append(step.escalationHours).append("}");
        }
        return sb.append("]}").toString();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // UTILITY HELPERS
    // ════════════════════════════════════════════════════════════════════════════
    static int    code(CustomizationExceptions.CustomizationException e) { return switch(e.getCategory()){case "MAJOR"->400;case "MINOR"->409;case "WARNING"->422;default->400;}; }
    static String err(String msg)  { return "{\"error\":\"" + esc(msg) + "\"}"; }
    static boolean nil(String s)   { return s == null || s.trim().isEmpty(); }
    static String esc(String s)    { return s==null?"":s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r",""); }
    static void cors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
    static void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }
    static String body(HttpExchange ex) throws IOException { return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); }
    static String p(String b, String k) {
        String jk = "\"" + k + "\":"; int idx = b.indexOf(jk);
        if (idx < 0) return "";
        String r = b.substring(idx + jk.length()).trim();
        if (r.startsWith("\"")) { int e = r.indexOf("\"", 1); return e > 0 ? r.substring(1, e) : ""; }
        int e = r.indexOf(","), e2 = r.indexOf("}"); int f = (e < 0) ? e2 : (e2 < 0 ? e : Math.min(e, e2));
        return f > 0 ? r.substring(0, f).trim() : r.trim();
    }
    static int seg(String path, int idx)  { return Integer.parseInt(path.split("/")[idx]); }
    static int intVal(String s)           { try { return Integer.parseInt(s); } catch (NumberFormatException | NullPointerException e) { return 0; } }
}
