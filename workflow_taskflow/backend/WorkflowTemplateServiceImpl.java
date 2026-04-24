import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CLASS: WorkflowTemplateServiceImpl
 * Provides predefined workflow templates for common HRMS business scenarios.
 * Each template contains standard approval steps that can be applied to new workflows.
 */
public class WorkflowTemplateServiceImpl implements WorkflowTemplateService {
    private final Map<String, WorkflowTemplate> templates;

    public WorkflowTemplateServiceImpl() {
        this.templates = new HashMap<>();
        initializeTemplates();
    }

    /**
     * Initialize all predefined workflow templates
     */
    private void initializeTemplates() {
        // Leave Approval Template
        templates.put("leave", new WorkflowTemplate(
            "leave",
            "Leave Approval (3-step)",
            "Standard leave request approval process",
            Arrays.asList(
                new WorkflowTemplate.TemplateStep("Employee Submission", "Employee", 24),
                new WorkflowTemplate.TemplateStep("Line Manager Review", "Line Manager", 48),
                new WorkflowTemplate.TemplateStep("HR Final Approval", "HR Admin", 24)
            )
        ));

        // Performance Appraisal Template
        templates.put("appraisal", new WorkflowTemplate(
            "appraisal",
            "Performance Appraisal (6-step)",
            "Comprehensive annual performance review process",
            Arrays.asList(
                new WorkflowTemplate.TemplateStep("Self Assessment", "Employee", 48),
                new WorkflowTemplate.TemplateStep("Manager Review", "Reporting Manager", 72),
                new WorkflowTemplate.TemplateStep("Peer Feedback", "Peers", 48),
                new WorkflowTemplate.TemplateStep("HR Evaluation", "HR", 48),
                new WorkflowTemplate.TemplateStep("Final Rating Approval", "Senior Manager", 72),
                new WorkflowTemplate.TemplateStep("Employee Acknowledgement", "Employee", 24)
            )
        ));

        // Onboarding Template
        templates.put("onboarding", new WorkflowTemplate(
            "onboarding",
            "Onboarding Checklist (4-step)",
            "New employee onboarding and integration process",
            Arrays.asList(
                new WorkflowTemplate.TemplateStep("Document Submission", "New Employee", 48),
                new WorkflowTemplate.TemplateStep("IT Setup", "IT Department", 24),
                new WorkflowTemplate.TemplateStep("Manager Induction", "Line Manager", 24),
                new WorkflowTemplate.TemplateStep("HR Confirmation", "HR Admin", 24)
            )
        ));

        // Offboarding Template
        templates.put("offboarding", new WorkflowTemplate(
            "offboarding",
            "Offboarding (5-step)",
            "Employee offboarding and exit process",
            Arrays.asList(
                new WorkflowTemplate.TemplateStep("Resignation Received", "HR Admin", 24),
                new WorkflowTemplate.TemplateStep("Asset Return", "IT Department", 48),
                new WorkflowTemplate.TemplateStep("Knowledge Transfer", "Line Manager", 72),
                new WorkflowTemplate.TemplateStep("Final Settlement", "Finance", 48),
                new WorkflowTemplate.TemplateStep("Exit Interview", "HR Admin", 24)
            )
        ));

        // Promotion Template
        templates.put("promotion", new WorkflowTemplate(
            "promotion",
            "Promotion Process (5-step)",
            "Employee promotion approval and execution workflow",
            Arrays.asList(
                new WorkflowTemplate.TemplateStep("Application Submission", "Employee", 24),
                new WorkflowTemplate.TemplateStep("Line Manager Recommendation", "Line Manager", 72),
                new WorkflowTemplate.TemplateStep("Department Head Review", "Department Head", 48),
                new WorkflowTemplate.TemplateStep("HR Approval", "HR Admin", 48),
                new WorkflowTemplate.TemplateStep("Executive Sign-off", "Executive", 24)
            )
        ));

        // Expense Claim Template
        templates.put("expense", new WorkflowTemplate(
            "expense",
            "Expense Claim (3-step)",
            "Standard expense claim submission and approval",
            Arrays.asList(
                new WorkflowTemplate.TemplateStep("Claim Submission", "Employee", 24),
                new WorkflowTemplate.TemplateStep("Manager Approval", "Line Manager", 48),
                new WorkflowTemplate.TemplateStep("Finance Processing", "Finance", 72)
            )
        ));
    }

    @Override
    public List<WorkflowTemplate> getAllTemplates() {
        return Arrays.asList(templates.values().toArray(new WorkflowTemplate[0]));
    }

    @Override
    public WorkflowTemplate getTemplateById(String templateId) {
        return templates.get(templateId);
    }

    @Override
    public WorkflowTemplate getTemplateByName(String templateName) {
        return templates.get(templateName.toLowerCase());
    }
}
