package backend.factory;

import backend.repository.*;

/**
 * FACTORY: RepositoryFactory
 * Subsystem: Customization — Form Designer & Flexfield Manager
 *
 * INTEGRATION STATUS:
 *   DB_INTEGRATION = true  → uses real SQLite DB (hrms.db)
 *   DB_INTEGRATION = false → uses in-memory Mock (original behaviour)
 *
 * HOW TO SWITCH:
 *   Set DB_INTEGRATION = true to activate real database persistence.
 *   Set DB_URL to the path of your hrms.db file if not in the working directory.
 *
 * WHY THIS APPROACH:
 *   - OCP: Neither MockFormRepository nor MockFlexfieldRepository was modified
 *   - DIP: Callers (CustomizationController) depend on IFormRepository /
 *          IFlexfieldRepository — they never know which impl they get
 *   - Single place to swap implementations (Factory Pattern)
 */
public class RepositoryFactory {

    // ─── Integration switch ───────────────────────────────────────────────────
    // Set to true to use the real SQLite database (hrms.db).
    // Set to false to use in-memory mock data (no persistence).
    private static final boolean DB_INTEGRATION = true;

    // Path to the shared HRMS SQLite database.
    // When running ApiServer from workflow_taskflow/backend/, use relative path:
    private static final String DB_URL = "jdbc:sqlite:hrms.db";
    // ─────────────────────────────────────────────────────────────────────────

    private RepositoryFactory() {}

    /**
     * Returns the active IFormRepository implementation.
     * Connected to hrms.db when DB_INTEGRATION = true.
     */
    public static IFormRepository createFormRepository() {
        if (DB_INTEGRATION) {
            System.out.println("[RepositoryFactory] Using DbFormRepository (hrms.db)");
            return new DbFormRepository(DB_URL);
        }
        System.out.println("[RepositoryFactory] Using MockFormRepository (in-memory)");
        return new MockFormRepository();
    }

    /**
     * Returns the active IFlexfieldRepository implementation.
     * Connected to hrms.db when DB_INTEGRATION = true.
     */
    public static IFlexfieldRepository createFlexfieldRepository() {
        if (DB_INTEGRATION) {
            System.out.println("[RepositoryFactory] Using DbFlexfieldRepository (hrms.db)");
            return new DbFlexfieldRepository(DB_URL);
        }
        System.out.println("[RepositoryFactory] Using MockFlexfieldRepository (in-memory)");
        return new MockFlexfieldRepository();
    }
}
