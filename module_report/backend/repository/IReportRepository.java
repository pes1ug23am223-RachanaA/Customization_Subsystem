package com.hrms.customization.repository;

import com.hrms.customization.model.Report;
import java.util.List;

public interface IReportRepository {
    int          saveReport(String name, String inputType, String format);
    Report       getReportById(int reportId);
    List<Report> getAllReports();
    void         customizeReportType(int reportId, String type);
    void         exportReportFormat(int reportId, String format);
    void         generateReport(int reportId);
    void         deleteReport(int reportId);
}
