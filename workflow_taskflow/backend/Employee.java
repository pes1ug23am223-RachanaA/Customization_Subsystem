/**
 * STUB: Employee
 * This is a minimal stub provided by the Customization Subsystem
 * to allow IEmployeeIntegration to compile without the full
 * Onboarding subsystem model classes.
 *
 * At integration time, replace this stub with the real Employee class
 * provided by the Onboarding & Offboarding team.
 *
 * Fields exposed here match what IEmployeeIntegration uses
 * and what EmployeeDTO already describes.
 */
public class Employee {
    public String employeeID;
    public String name;
    public String role;
    public String department;
    public String status;

    public Employee() {}

    public Employee(String employeeID, String name, String role,
                    String department, String status) {
        this.employeeID = employeeID;
        this.name       = name;
        this.role       = role;
        this.department = department;
        this.status     = status;
    }

    @Override
    public String toString() {
        return "[Employee: " + employeeID + " | " + name + " | " + department + "]";
    }
}
