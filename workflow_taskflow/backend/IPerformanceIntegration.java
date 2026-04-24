import java.util.List;

/**
 * IPerformanceIntegration
 * Customization Subsystem adapter for the Performance Management Subsystem.
 *
 * Mirrors IPerformanceForCustomization provided by the Performance team.
 * Used by Workflow Engine and Form Designer to discover performance forms,
 * workflows, task flows, and read cycle/goal/appraisal data.
 *
 * DESIGN: Adapter Pattern — translates performance subsystem data
 *         into formats usable by the customization modules.
 */
public interface IPerformanceIntegration {

    /** Returns the port on which the performance subsystem is running */
    String getServerPort();

    /** Returns form IDs exposed by the performance subsystem */
    List<String> getFormIds();

    /** Returns workflow IDs exposed by the performance subsystem */
    List<String> getWorkflowIds();

    /** Returns task flow IDs exposed by the performance subsystem */
    List<String> getTaskFlowIds();

    /** Returns all performance cycles (name, start/end dates) */
    List<PerformanceCycle> getAllPerformanceCycles();

    /** Returns goals for a given cycle name */
    List<Goal> getGoalsForCycle(String cycleName);

    /** Returns appraisals for a given cycle name */
    List<Appraisal> getAppraisalsForCycle(String cycleName);

    /** Returns description for a form ID */
    String getFormDescription(String formId);

    /** Returns description for a workflow ID */
    String getWorkflowDescription(String workflowId);

    /** Returns description for a task flow ID */
    String getTaskFlowDescription(String taskFlowId);
}
