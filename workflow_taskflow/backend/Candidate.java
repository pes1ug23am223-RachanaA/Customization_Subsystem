/**
 * STUB: Candidate
 * Minimal stub to allow IEmployeeIntegration to compile.
 * Replace with the real Candidate class from the Onboarding team at integration time.
 */
public class Candidate {
    public String candidateID;
    public String name;
    public String appliedRole;
    public String department;

    public Candidate() {}

    public Candidate(String candidateID, String name,
                     String appliedRole, String department) {
        this.candidateID  = candidateID;
        this.name         = name;
        this.appliedRole  = appliedRole;
        this.department   = department;
    }

    @Override
    public String toString() {
        return "[Candidate: " + candidateID + " | " + name + " | " + appliedRole + "]";
    }
}
