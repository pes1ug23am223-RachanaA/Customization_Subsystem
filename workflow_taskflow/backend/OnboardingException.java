/**
 * STUB: OnboardingException
 * Minimal stub to allow IEmployeeIntegration to compile.
 * Replace with the real OnboardingException from the Onboarding team at integration time.
 */
public class OnboardingException extends Exception {

    private final String step;

    public OnboardingException(String message) {
        super(message);
        this.step = "UNKNOWN";
    }

    public OnboardingException(String step, String message) {
        super(message);
        this.step = step;
    }

    /** The pipeline step that failed: e.g. "DocumentVerification", "PolicyCompliance" */
    public String getStep() { return step; }

    @Override
    public String toString() {
        return "[OnboardingException at " + step + "] " + getMessage();
    }
}
