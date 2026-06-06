package com.cts.vis.service;

import com.cts.vis.model.ReportType;

import java.time.LocalDate;
import java.util.Map;
public interface AdminReportService {
    Map<String, Object> generate(ReportType type, LocalDate start, LocalDate end);
    byte[] exportPdf(ReportType type, LocalDate start, LocalDate end);
    byte[] exportExcel(ReportType type, LocalDate start, LocalDate end);
}