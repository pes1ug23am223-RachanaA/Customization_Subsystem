import java.util.List;

/**
 * INTERFACE: ITaskFlowRepository
 * Subsystem: Customization — Component: Task Flow Builder
 *
 * Formal contract with the DB Team.
 * SOLID: Dependency Inversion — Task Flow Builder depends on this abstraction.
 * GRASP: Polymorphism — allows Mock and Real DB implementations.
 *
 * NOTE: This file is provided by the DB Team.
 * Copied here only for compilation. Do NOT modify.
 */
public interface ITaskFlowRepository {
    int defineTaskFlow(String name, String flowStatus);
    TaskFlowDTO getTaskFlowById(int taskId);
    List<TaskFlowDTO> getAllTaskFlows();
    void setSequence(int taskId, int sequenceOrder);
    void updateTaskFlow(int taskId, String newName);
    void deleteTaskFlow(int taskId);
    void assignFlowToMenu(int taskId, String menuName);
    void updateFlowSettings(int taskId, boolean validateOnNext, boolean allowBackNav);
    void addWindowToFlow(int taskId, String windowName);
    void removeWindowFromFlow(int taskId, String windowName);
    List<String> getWindowsForFlow(int taskId);
}
