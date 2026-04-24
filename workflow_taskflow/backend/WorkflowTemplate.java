import java.util.List;

/**
 * CLASS: WorkflowTemplate
 * Represents a predefined workflow template with standard steps.
 * Used by TemplateService to provide users with common workflow patterns.
 */
public class WorkflowTemplate {
    public String templateId;
    public String templateName;
    public String description;
    public List<TemplateStep> steps;

    public WorkflowTemplate(String templateId, String templateName, String description, List<TemplateStep> steps) {
        this.templateId = templateId;
        this.templateName = templateName;
        this.description = description;
        this.steps = steps;
    }

    public static class TemplateStep {
        public String stepName;
        public String assignee;
        public int escalationHours;

        public TemplateStep(String stepName, String assignee, int escalationHours) {
            this.stepName = stepName;
            this.assignee = assignee;
            this.escalationHours = escalationHours;
        }
    }
}
