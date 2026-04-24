package interfaces;

import models.*;
import java.util.List;

/**
 * Performance Subsystem Interface
 * This interface defines the public contract for the Performance Management Subsystem.
 * Other subsystems (like Payroll, HR, etc.) should use these methods to interact with performance data.
 */
public interface IPerformanceSubsystem {
    
    // --- Employee Data ---
    List<Employee> getAllEmployees();
    Employee getEmployeeById(String id);
    
    // --- Performance Data ---
    List<Goal> getAllGoals();
    List<Goal> getGoalsByEmployee(String employeeId);
    
    List<Feedback> getAllFeedback();
    List<Feedback> getFeedbackForEmployee(String employeeId);
    
    List<Appraisal> getAllAppraisals();
    List<Appraisal> getAppraisalsByEmployee(String employeeId);
    
    List<KPI> getAllKPIs();
    
    // --- Analytics ---
    int getAverageScore();
    int getGoalsOnTrackCount();
    int getGoalsOnTrackPercentage();
    
    // --- Actions ---
    void addEmployee(Employee emp);
    void updateEmployee(Employee emp);
    void deleteEmployee(String id);
    
    void addGoal(Goal g);
    void updateGoal(Goal g);
    void deleteGoal(String id);
    
    void addFeedback(Feedback f);
    void addAppraisal(Appraisal a);
    
    void addReward(Reward r);
    List<Reward> getAllRewards();
}
