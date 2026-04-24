package com.hrms.customization.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Report {
    private int           reportId;
    private String        reportName;
    private String        inputType;
    private String        format;
    private String        dataSource;
    private String        schedule;
    private LocalDateTime generatedDate;   // READ-ONLY: only set by service/repo layer

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Report() {}
    public Report(int reportId, String reportName, String inputType, String format,
                  String dataSource, String schedule, LocalDateTime generatedDate) {
        this.reportId      = reportId;
        this.reportName    = reportName;
        this.inputType     = inputType;
        this.format        = format;
        this.dataSource    = dataSource;
        this.schedule      = schedule;
        this.generatedDate = generatedDate;
    }

    public int    getReportId()   { return reportId; }
    public String getReportName() { return reportName; }
    public String getInputType()  { return inputType; }
    public String getFormat()     { return format; }
    public String getDataSource() { return dataSource; }
    public String getSchedule()   { return schedule; }
    public String getGeneratedDate() { return generatedDate == null ? null : generatedDate.format(FMT); }

    public void setReportId(int id)             { this.reportId   = id; }
    public void setReportName(String name)      { this.reportName = name; }
    public void setInputType(String inputType)  { this.inputType  = inputType; }
    public void setFormat(String format)        { this.format     = format; }
    public void setDataSource(String ds)        { this.dataSource = ds; }
    public void setSchedule(String schedule)    { this.schedule   = schedule; }

    // Called by InMemoryReportRepository (different package) — must be public
    public void setGeneratedDate(LocalDateTime dt)     { this.generatedDate = dt; }
    public LocalDateTime getGeneratedDateRaw()         { return generatedDate; }
}
