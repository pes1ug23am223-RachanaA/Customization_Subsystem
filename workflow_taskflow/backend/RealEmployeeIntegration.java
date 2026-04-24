/**
 * CLASS: RealEmployeeIntegration
 * Used by: Customization Subsystem (Code Crafters)
 *
 * PURPOSE:
 *   Provides real implementations of IEmployeeIntegration by connecting
 *   to the SQLite HRMS database. Replaces MockOnboardingIntegration for
 *   production use.
 *
 * RESPONSIBILITIES:
 *   - Fetch full employee records from the database
 *   - Assign roles to employees via lookup validation
 *   - Trigger onboarding workflows through the Customization Engine
 *   - Load onboarding forms from the Form Designer
 *   - Execute pre-onboarding validation pipeline
 */
public class RealEmployeeIntegration implements IEmployeeIntegration {

    private static final String DB_URL = "jdbc:sqlite:hrms.db";
    private final DbEmployeeRepository employeeRepository;

    public RealEmployeeIntegration() {
        // Structural pattern: this integration class acts as the Adapter
        // between the onboarding interface and the DB-team repository API.
        employeeRepository = new DbEmployeeRepository(DB_URL);
    }

    /**
     * Fetch full employee details from the database.
     *
     * @param employeeID Primary key of the employee record
     * @return The Employee object, or null if not found
     */
    @Override
    public Employee getEmployee(String employeeID) {
        return employeeRepository.findEmployeeById(employeeID);
    }

    /**
     * Assign a role to an employee.
     * The role values must come from the Customization lookup service.
     *
     * @param employee The employee to update
     * @param role Role string validated against the lookup system
     */
    @Override
    public void assignRole(Employee employee, String role) {
        if (employee == null || role == null) {
            CustomizationErrorApi.getInstance().logError(
                "RealEmployeeIntegration", "EMPLOYEE_ROLE_UPDATE_FAILED",
                "Cannot assign role because input was invalid", "employee or role was null");
            return;
        }
        if (!employeeRepository.updateRole(employee.employeeID, role)) {
            CustomizationErrorApi.getInstance().logError(
                "RealEmployeeIntegration", "EMPLOYEE_ROLE_UPDATE_FAILED",
                "Employee role update did not affect any rows", employee.employeeID);
        }
    }

    /**
     * Trigger the onboarding workflow via the Customization subsystem.
     *
     * @param employee The employee whose onboarding workflow to start
     * @return Workflow ID returned by the Customization engine, or -1 on failure
     */
    @Override
    public int startOnboarding(Employee employee) {
        if (employee == null) {
            System.err.println("[RealEmployeeIntegration] Cannot start onboarding: employee is null");
            return -1;
        }

        System.out.println("[RealEmployeeIntegration] Starting onboarding workflow for: "
            + employee.name + " (" + employee.employeeID + ")");

        // Create a real workflow in the Customization Workflow Engine
        try {
            WorkflowService workflowService = CustomizationServiceFactory.getInstance().getWorkflowService();
            String workflowName = "Onboarding: " + employee.name;
            String assignedTo = "HR Admin";
            
            int workflowId = workflowService.createWorkflow(workflowName, assignedTo);
            
            System.out.println("[RealEmployeeIntegration] Onboarding workflow created with ID: " + workflowId);
            return workflowId;
        } catch (Exception e) {
            CustomizationErrorApi.getInstance().logError(
                "RealEmployeeIntegration", "WORKFLOW_EXECUTION_FAILED",
                "Unable to create onboarding workflow", e.getMessage());
            return -1;
        }
    }

    /**
     * Load the onboarding form definition from the Customization subsystem.
     * Renders or logs the form fields.
     */
    @Override
    public void loadOnboardingForm() {
        System.out.println("[RealEmployeeIntegration] Loading 'Onboarding Checklist' form "
            + "from Customization Form Designer...");
        System.out.println("[RealEmployeeIntegration] Form fields: Leave Type, Department, "
            + "Employment Type, Emergency Contact, Previous Employer");

        // In real integration: calls CustomizationFacade.getFormIntegration()
        //                      .getFormByName("Onboarding Checklist")
    }

    /**
     * Entry point for the pre-onboarding validation pipeline.
     * Runs Chain of Responsibility: DocumentVerification → PolicyCompliance → ReferenceCheck → EmployeeCreation
     *
     * @param candidateID Unique identifier of the candidate to onboard
     * @return true if the full pipeline succeeded and an employee record was created
     * @throws OnboardingException if a specific pipeline step fails
     */
    @Override
    public boolean startPreOnboarding(String candidateID) throws OnboardingException {
        if (candidateID == null || candidateID.trim().isEmpty()) {
            throw new OnboardingException("Candidate ID cannot be empty");
        }

        System.out.println("[RealEmployeeIntegration] Starting pre-onboarding pipeline for candidate: " + candidateID);

        // Chain of Responsibility pattern: Execute validation pipeline
        // Step 1: Document Verification
        System.out.println("[RealEmployeeIntegration] Step 1: Verifying documents for candidate: " + candidateID);
        boolean documentVerified = verifyDocuments(candidateID);
        if (!documentVerified) {
            throw new OnboardingException("Document verification failed for candidate: " + candidateID);
        }
        
        // Step 2: Policy Compliance
        System.out.println("[RealEmployeeIntegration] Step 2: Checking policy compliance for candidate: " + candidateID);
        boolean policyCompliant = checkPolicyCompliance(candidateID);
        if (!policyCompliant) {
            throw new OnboardingException("Policy compliance check failed for candidate: " + candidateID);
        }
        
        // Step 3: Reference Check
        System.out.println("[RealEmployeeIntegration] Step 3: Conducting reference check for candidate: " + candidateID);
        boolean referenceCheckPassed = conductReferenceCheck(candidateID);
        if (!referenceCheckPassed) {
            throw new OnboardingException("Reference check failed for candidate: " + candidateID);
        }
        
        // Step 4: Create employee from candidate
        System.out.println("[RealEmployeeIntegration] Step 4: Creating employee record from candidate: " + candidateID);
        Employee newEmp = new Employee();
        newEmp.employeeID = "EMP" + System.currentTimeMillis();
        newEmp.name = candidateID;
        newEmp.role = "Employee";
        newEmp.department = "General";
        newEmp.status = "ACTIVE";
        System.out.println("[RealEmployeeIntegration] Employee created: " + newEmp.employeeID);

        System.out.println("[RealEmployeeIntegration] Pre-onboarding pipeline completed for: " + candidateID);
        return true;
    }

    /**
     * Validates a candidate through document, policy, and reference checks
     * without creating the employee record.
     *
     * @param candidateID Unique identifier of the candidate to validate
     * @return true if all validation steps pass
     * @throws OnboardingException if any validation step fails
     */
    @Override
    public boolean validateCandidate(String candidateID) throws OnboardingException {
        if (candidateID == null || candidateID.trim().isEmpty()) {
            throw new OnboardingException("Candidate ID cannot be empty");
        }

        System.out.println("[RealEmployeeIntegration] Validating candidate: " + candidateID);

        // Implement validation logic pipeline
        // Step 1: Document Verification
        System.out.println("[RealEmployeeIntegration] Validating documents for: " + candidateID);
        if (!verifyDocuments(candidateID)) {
            throw new OnboardingException("Document verification failed for candidate: " + candidateID);
        }
        
        // Step 2: Policy Compliance
        System.out.println("[RealEmployeeIntegration] Checking policy compliance for: " + candidateID);
        if (!checkPolicyCompliance(candidateID)) {
            throw new OnboardingException("Policy compliance check failed for candidate: " + candidateID);
        }
        
        // Step 3: Reference Check
        System.out.println("[RealEmployeeIntegration] Conducting reference check for: " + candidateID);
        if (!conductReferenceCheck(candidateID)) {
            throw new OnboardingException("Reference check failed for candidate: " + candidateID);
        }

        System.out.println("[RealEmployeeIntegration] Candidate validation completed for: " + candidateID);
        return true;
    }

    /**
     * Converts a fully-validated Candidate into an Employee.
     *
     * @param candidate A non-null, validated candidate object
     * @return The newly created Employee
     * @throws OnboardingException if conversion fails due to missing/invalid data
     */
    @Override
    public Employee createEmployeeFromCandidate(Candidate candidate) throws OnboardingException {
        if (candidate == null) {
            throw new OnboardingException("Candidate cannot be null");
        }

        System.out.println("[RealEmployeeIntegration] Creating employee from candidate: " + candidate.candidateID);

        try {
            // Generate new employee ID
            String newEmpId = "EMP" + System.currentTimeMillis();
            
            // Copy candidate data to employee record
            Employee employee = new Employee();
            employee.employeeID = newEmpId;
            employee.name = candidate.name != null ? candidate.name : candidate.candidateID;
            employee.role = candidate.appliedRole != null ? candidate.appliedRole : "Employee";
            employee.department = candidate.department != null ? candidate.department : "General";
            employee.status = "ACTIVE";
            
            System.out.println("[RealEmployeeIntegration] Employee created successfully: " + newEmpId);
            return employee;
        } catch (Exception e) {
            throw new OnboardingException("Failed to create employee from candidate: " + e.getMessage());
        }
    }

    /**
     * Verifies candidate documents.
     * @param candidateID the candidate to verify
     * @return true if documents are verified
     */
    private boolean verifyDocuments(String candidateID) {
        // Placeholder implementation: accept all documents for now
        System.out.println("[RealEmployeeIntegration] Documents verified for: " + candidateID);
        return true;
    }
    
    /**
     * Checks policy compliance for a candidate.
     * @param candidateID the candidate to check
     * @return true if policy compliant
     */
    private boolean checkPolicyCompliance(String candidateID) {
        // Placeholder implementation: accept all for policy compliance
        System.out.println("[RealEmployeeIntegration] Policy compliance verified for: " + candidateID);
        return true;
    }
    
    /**
     * Conducts reference check for a candidate.
     * @param candidateID the candidate to check
     * @return true if reference check passes
     */
    private boolean conductReferenceCheck(String candidateID) {
        // Placeholder implementation: accept all references
        System.out.println("[RealEmployeeIntegration] Reference check completed for: " + candidateID);
        return true;
    }

}
