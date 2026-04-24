import java.util.List;

/**
 * IDbService — DB Team's Unified Database Access API
 * 
 * All subsystems MUST access the database ONLY through this interface.
 * The DB team implements this interface with actual JDBC code.
 * 
 * Design: Dependency Inversion Principle
 * - Modules depend on this interface, NOT on JDBC
 * - Only DB team's DbServiceImpl uses java.sql.*
 * - Easy to swap implementations (mock, real DB, cache layer, etc.)
 */
public interface IDbService {

    // ═══════════════════════════════════════════════════════════════════════════
    // WORKFLOW OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════
    
    WorkflowDTO getWorkflowById(int id);
    List<WorkflowDTO> getAllWorkflows();
    int saveWorkflow(WorkflowDTO wf);
    void updateWorkflowStatus(int id, String status);
    void deleteWorkflow(int id);
    String getWorkflowStatus(int id);

    // ─────────────────────────────────────────────────────────────────────────
    // Workflow Steps
    // ─────────────────────────────────────────────────────────────────────────
    List<WorkflowStepDTO> getWorkflowSteps(int wfId);
    void addWorkflowStep(int wfId, String name, String assignee, int escHours);
    void removeWorkflowStep(int stepId);
    void assignUserToStep(int stepId, String userId);

    // ═══════════════════════════════════════════════════════════════════════════
    // TASKFLOW OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════
    
    TaskFlowDTO getTaskFlowById(int id);
    List<TaskFlowDTO> getAllTaskFlows();
    int saveTaskFlow(TaskFlowDTO tf);
    void updateTaskFlowStatus(int id, String status);
    void deleteTaskFlow(int id);

    // ─────────────────────────────────────────────────────────────────────────
    // TaskFlow Windows
    // ─────────────────────────────────────────────────────────────────────────
    List<String> getTaskFlowWindows(int taskId);
    void addTaskFlowWindow(int taskId, String windowName);
    void removeTaskFlowWindow(int taskId, String windowName);

    // ═══════════════════════════════════════════════════════════════════════════
    // FORM OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════
    
    int saveForm(String formName, String layoutType);
    void addFormField(int formId, String fieldName, String fieldType, boolean isMandatory);
    List<Object> getAllForms();
    Object getFormById(int formId);
    void deleteForm(int formId);

    // ═══════════════════════════════════════════════════════════════════════════
    // FLEXFIELD OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════
    
    void saveFlexfield(String name, String type, int segments);
    List<Object> getAllFlexfields();
    void deleteFlexfield(int fieldId);

    // ═══════════════════════════════════════════════════════════════════════════
    // EIT OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════
    
    void saveEIT(String name, String type, String context);
    List<Object> getAllEITs();
    void deleteEIT(int eitId);
    void deleteEITByName(String name);

    // ═══════════════════════════════════════════════════════════════════════════
    // LOOKUP OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════
    
    void saveLookup(String name, List<String> values);
    List<Object> getAllLookups();
    void addLookupValue(String lookupName, String value);
    void deleteLookup(int lookupId);

    // ═══════════════════════════════════════════════════════════════════════════
    // MODULE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════
    
    int saveModule(String moduleName);
    List<Object> getAllModules();
    void updateModuleStatus(int moduleId, boolean enabled);
    void deleteModule(int moduleId);

    // ═══════════════════════════════════════════════════════════════════════════
    // REPORT OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════
    
    int saveReport(String name, String inputType, String format);
    Object getReportById(int reportId);
    List<Object> getAllReports();
    void customizeReportType(int reportId, String type);
    void exportReportFormat(int reportId, String format);
    void generateReport(int reportId);
    void deleteReport(int reportId);

    // ═══════════════════════════════════════════════════════════════════════════
    // EMPLOYEE OPERATIONS (for Onboarding integration)
    // ═══════════════════════════════════════════════════════════════════════════
    
    void saveEmployee(EmployeeDTO emp);
    List<EmployeeDTO> getAllEmployees();
    EmployeeDTO getEmployeeById(String empId);
    void deleteEmployee(String empId);

    // ═══════════════════════════════════════════════════════════════════════════
    // PERFORMANCE OPERATIONS (for Performance integration)
    // ═══════════════════════════════════════════════════════════════════════════
    
    void savePerformanceGoal(int empId, String goal, String period);
    List<Object> getPerformanceGoals(int empId);
    void saveAppraisal(int empId, String rating, String comments);
    List<Object> getAppraisals(int empId);
}
