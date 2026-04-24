

/**
 * Integration interface exposed by the Employee Onboarding &amp; Offboarding subsystem.
 *
 * <p>External subsystems (Customization workflow engine, Benefits, etc.) interact
 * with this system exclusively through this interface — never through concrete
 * service classes directly.
 *
 * <p><strong>Existing methods</strong> (required by the Customization team) are
 * preserved unchanged below. <strong>New methods</strong> added for the
 * Pre-Onboarding module ownership are clearly marked.
 *
 * SOLID:
 * <ul>
 *   <li>ISP  — callers depend only on operations they actually use.</li>
 *   <li>DIP  — other subsystems depend on this abstraction, not concrete services.</li>
 *   <li>OCP  — extended with new methods; existing contracts not altered.</li>
 * </ul>
 */
public interface IEmployeeIntegration {

    // ═══════════════════════════════════════════════════════════════════════
    // EXISTING METHODS — required by Customization subsystem. DO NOT REMOVE.
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Fetch full employee details from the database.
     *
     * @param employeeID  Primary key of the employee record.
     * @return The {@link Employee}, or {@code null} if not found.
     */
    Employee getEmployee(String employeeID);

    /**
     * Assign a role to an employee.
     * Valid role values come from the Customization lookup service
     * ({@code lookup.getValues("DEPARTMENT")}).
     *
     * @param employee  The employee to update.
     * @param role      Role string validated against the lookup system.
     */
    void assignRole(Employee employee, String role);

    /**
     * Trigger the onboarding workflow via the Customization subsystem.
     *
     * @param employee  The employee whose onboarding workflow to start.
     * @return Workflow ID returned by the Customization engine, or {@code -1} on
     *         failure.
     */
    int startOnboarding(Employee employee);

    /**
     * Load the onboarding form definition from the Customization subsystem
     * and render/log it.
     */
    void loadOnboardingForm();

    // ═══════════════════════════════════════════════════════════════════════
    // NEW METHODS — Pre-Onboarding module (Chain of Responsibility pipeline)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Entry point for external subsystems to trigger the full pre-onboarding
     * validation pipeline for a candidate.
     *
     * <p>Internally runs the Chain of Responsibility:
     * <pre>
     *   DocumentVerification → PolicyCompliance → ReferenceCheck → EmployeeCreation
     * </pre>
     *
     * @param candidateID  Unique identifier of the candidate to onboard.
     * @return {@code true} if the full pipeline succeeded and an employee record
     *         was created; {@code false} otherwise.
     * @throws OnboardingException if a specific pipeline step fails.
     */
    boolean startPreOnboarding(String candidateID) throws OnboardingException;

    /**
     * Validates a candidate through document, policy, and reference checks
     * <em>without</em> creating the employee record.
     *
     * <p>Useful for partial validation (e.g. real-time form checks) or dry-run
     * flows that surface issues before committing to employee creation.
     *
     * @param candidateID  Unique identifier of the candidate to validate.
     * @return {@code true} if all validation steps pass.
     * @throws OnboardingException if any validation step fails.
     */
    boolean validateCandidate(String candidateID) throws OnboardingException;

    /**
     * Converts a fully-validated {@link Candidate} into an {@link Employee}
     * using the {@code EmployeeFactory}.
     *
     * <p>Exposed here so external subsystems can trigger conversion after they
     * have independently validated the candidate, without re-running the
     * complete pipeline.
     *
     * @param candidate  A non-null, validated candidate object.
     * @return The newly created {@link Employee}.
     * @throws OnboardingException if conversion fails due to missing/invalid data.
     */
    Employee createEmployeeFromCandidate(Candidate candidate) throws OnboardingException;
}
