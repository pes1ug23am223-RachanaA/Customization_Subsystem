package interfaces;

import models.Appraisal;
import models.Goal;
import models.PerformanceCycle;
import java.util.List;

/**
 * Customization adapter used by external workflow, form, and task flow consumers
 * to discover subsystem support and read goal/appraisal cycle data.
 */
public interface IPerformanceForCustomization {

    String getServerPort();

    List<String> getFormIds();

    List<String> getWorkflowIds();

    List<String> getTaskFlowIds();

    List<PerformanceCycle> getAllPerformanceCycles();

    List<Goal> getGoalsForCycle(String cycleName);

    List<Appraisal> getAppraisalsForCycle(String cycleName);

    String getFormDescription(String formId);

    String getWorkflowDescription(String workflowId);

    String getTaskFlowDescription(String taskFlowId);
}
