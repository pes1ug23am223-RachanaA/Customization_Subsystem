import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exception subsystem API used by all customization-side classes to log and
 * inspect runtime failures. This keeps error handling behind one shared API
 * instead of each class printing ad-hoc messages.
 *
 * Creational pattern: Singleton
 * GRASP: Pure Fabrication for cross-cutting error management
 */
public class CustomizationErrorApi {

    private static final int MAX_ENTRIES = 100;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final CustomizationErrorApi INSTANCE = new CustomizationErrorApi();

    private final List<ErrorEntry> entries = new ArrayList<>();

    private CustomizationErrorApi() {}

    public static CustomizationErrorApi getInstance() {
        return INSTANCE;
    }

    public synchronized void logError(String source, String code, String message, String detail) {
        if (entries.size() >= MAX_ENTRIES) entries.remove(0);
        entries.add(new ErrorEntry(
            LocalDateTime.now().format(FMT),
            safe(source),
            safe(code),
            safe(message),
            safe(detail)
        ));
    }

    public synchronized List<ErrorEntry> viewRecentErrors() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public synchronized int count() {
        return entries.size();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public static class ErrorEntry {
        public final String timestamp;
        public final String source;
        public final String code;
        public final String message;
        public final String detail;

        public ErrorEntry(String timestamp, String source, String code, String message, String detail) {
            this.timestamp = timestamp;
            this.source = source;
            this.code = code;
            this.message = message;
            this.detail = detail;
        }
    }
}
