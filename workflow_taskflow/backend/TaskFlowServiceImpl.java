import java.util.List;

public class TaskFlowServiceImpl implements TaskFlowService {

    private final ITaskFlowRepository repo;

    public TaskFlowServiceImpl(ITaskFlowRepository repo) {
        this.repo = repo;
    }

    @Override public List<TaskFlowDTO> getAllTaskFlows() { return repo.getAllTaskFlows(); }

    @Override public TaskFlowDTO getTaskFlowById(int taskId) {
        TaskFlowDTO tf = repo.getTaskFlowById(taskId);
        if (tf == null) throw new TaskFlowException("TASKFLOW_NOT_FOUND","Task Flow ID "+taskId+" does not exist.");
        return tf;
    }

    @Override public int createTaskFlow(String name, String linkedMenu, boolean validateOnNext, boolean allowBackNav) {
        if (name == null || name.trim().isEmpty())
            throw new TaskFlowException("INVALID_TASKFLOW_NAME","Task flow name cannot be empty.");
        boolean exists = repo.getAllTaskFlows().stream().anyMatch(t -> t.flowName.equalsIgnoreCase(name.trim()));
        if (exists) throw new TaskFlowException("DUPLICATE_TASKFLOW_NAME","A task flow named '"+name+"' already exists.");
        int id = repo.defineTaskFlow(name.trim(), "Active");
        repo.assignFlowToMenu(id, linkedMenu != null ? linkedMenu : "");
        repo.updateFlowSettings(id, validateOnNext, allowBackNav);
        return id;
    }

    @Override public void updateTaskFlowName(int taskId, String newName) {
        getTaskFlowById(taskId);
        if (newName == null || newName.trim().isEmpty())
            throw new TaskFlowException("INVALID_TASKFLOW_NAME","Task flow name cannot be empty.");
        repo.updateTaskFlow(taskId, newName.trim());
    }

    @Override public void updateTaskFlowSettings(int taskId, boolean validateOnNext, boolean allowBackNav) {
        getTaskFlowById(taskId);
        repo.updateFlowSettings(taskId, validateOnNext, allowBackNav);
    }

    @Override public void setStatus(int taskId, String status) {
        TaskFlowDTO tf = getTaskFlowById(taskId);
        if (tf.flowStatus.equals(status))
            throw new TaskFlowException("TASKFLOW_STATUS_UNCHANGED","Task Flow '"+tf.flowName+"' is already "+status+".");
        // Persist status change to DB via repo
        if (repo instanceof DbTaskFlowRepository) {
            ((DbTaskFlowRepository) repo).setStatus(taskId, status);
        }
    }

    @Override public void deleteTaskFlow(int taskId) { getTaskFlowById(taskId); repo.deleteTaskFlow(taskId); }
    @Override public void assignToMenu(int taskId, String menuName) { getTaskFlowById(taskId); repo.assignFlowToMenu(taskId, menuName); }

    @Override public List<String> getWindows(int taskId) { getTaskFlowById(taskId); return repo.getWindowsForFlow(taskId); }

    @Override public void addWindow(int taskId, String windowName) {
        getTaskFlowById(taskId);
        if (windowName == null || windowName.trim().isEmpty())
            throw new TaskFlowException("INVALID_WINDOW_NAME","Window name cannot be empty.");
        repo.addWindowToFlow(taskId, windowName.trim());
    }

    @Override public void removeWindow(int taskId, String windowName) { getTaskFlowById(taskId); repo.removeWindowFromFlow(taskId, windowName); }

    @Override public void setSequence(int taskId, int sequenceOrder) {
        getTaskFlowById(taskId);
        if (sequenceOrder < 1) throw new TaskFlowException("INVALID_SEQUENCE","Sequence order must be >= 1.");
        repo.setSequence(taskId, sequenceOrder);
    }
}
