import java.util.List;

/**
 * INTERFACE: TaskFlowService
 * Subsystem: Customization — Component: Task Flow Builder
 *
 * GRASP: Controller — single point of control for all task flow operations.
 * SOLID: Interface Segregation — only task flow operations here.
 */
public interface TaskFlowService {
    List<TaskFlowDTO> getAllTaskFlows();
    TaskFlowDTO getTaskFlowById(int taskId);
    int createTaskFlow(String name, String linkedMenu, boolean validateOnNext, boolean allowBackNav);
    void updateTaskFlowName(int taskId, String newName);
    void updateTaskFlowSettings(int taskId, boolean validateOnNext, boolean allowBackNav);
    void setStatus(int taskId, String status);
    void deleteTaskFlow(int taskId);
    void assignToMenu(int taskId, String menuName);

    // Window management
    List<String> getWindows(int taskId);
    void addWindow(int taskId, String windowName);
    void removeWindow(int taskId, String windowName);
    void setSequence(int taskId, int sequenceOrder);
}


