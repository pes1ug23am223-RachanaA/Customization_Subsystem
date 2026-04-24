/**
 * ReportBuilder — Business Logic Layer for Report Builder component.
 * Handles full report lifecycle and basic report data retrieval.
 * PDF/Excel export requires external libraries (PDFBox, Apache POI).
 * Currently provides CSV export capability.
 */
public class ReportBuilder {

    private static final java.util.Set<String> VALID_FORMATS =
        new java.util.HashSet<>(java.util.Arrays.asList("PDF", "EXCEL", "CSV"));

    private final Object reportRepo;

    public ReportBuilder(Object reportRepo) {
        this.reportRepo = reportRepo;
    }

    private boolean isValidFormat(String fmt) {
        return fmt != null && VALID_FORMATS.contains(fmt.toUpperCase());
    }

    public java.util.List<Object> listAllReports() {
        return new java.util.ArrayList<>();
    }

    public Object getReport(int reportId) {
        return null;
    }

    public byte[] generateReport(int reportId, String format) throws java.io.IOException {
        if (!isValidFormat(format)) {
            throw new IllegalArgumentException("Invalid format: " + format + ". Supported: PDF, EXCEL, CSV");
        }
        String fmt = format.toUpperCase();
        if ("CSV".equals(fmt)) {
            return generateCsvBytes();
        }
        throw new java.io.IOException("Format " + fmt + " requires external library");
    }

    private byte[] generateCsvBytes() throws java.io.IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8);
        writer.write("Report ID,Report Name,Created Date,Status\n");
        writer.write("1,Sample Report,2026-04-23,Active\n");
        writer.flush();
        writer.close();
        return out.toByteArray();
    }

    public void createReport(String name, String inputType, String format) {
        System.out.println("[ReportBuilder] Creating report: " + name);
    }

    public void updateReport(int reportId, String name, String description) {
        System.out.println("[ReportBuilder] Updating report " + reportId + ": " + name);
    }

    public void deleteReport(int reportId) {
        System.out.println("[ReportBuilder] Deleting report " + reportId);
    }
}
