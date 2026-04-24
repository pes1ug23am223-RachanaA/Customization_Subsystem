import java.util.List;

/**
 * INTERFACE: WorkflowTemplateService
 * Provides predefined workflow templates for common business scenarios.
 * Users can select a template when creating a workflow to auto-populate standard steps.
 */
public interface WorkflowTemplateService {
    /**
     * Get all available predefined workflow templates
     */
    List<WorkflowTemplate> getAllTemplates();

    /**
     * Get a specific template by ID
     */
    WorkflowTemplate getTemplateById(String templateId);

    /**
     * Get template by name (e.g., "leave", "appraisal", "onboarding")
     */
    WorkflowTemplate getTemplateByName(String templateName);
}
