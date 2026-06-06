package com.cts.vis.controller;

import com.cts.vis.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // --- VIEW PAGES ---

    @GetMapping
    public String reportsHome() {
        return "customer/reports";
    }

    @GetMapping("/policies")
    public String policyReportPage(Model model) {
        // model.addAllAttributes expects a Map<String, Object> from the service
        model.addAllAttributes(reportService.customerPolicyReport());
        return "customer/report-policy";
    }

    @GetMapping("/claims")
    public String claimReportPage(Model model) {
        model.addAllAttributes(reportService.customerClaimReport());
        return "customer/report-claim";
    }

    // --- DOWNLOADS ---

    @GetMapping("/policies/pdf")
    public ResponseEntity<byte[]> policyPdf() {
        return export(reportService.customerPolicyPdf(), "POLICY_REPORT.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping("/policies/excel")
    public ResponseEntity<byte[]> policyExcel() {
        return export(reportService.customerPolicyExcel(), "POLICY_REPORT.xlsx",
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @GetMapping("/claims/pdf")
    public ResponseEntity<byte[]> claimPdf() {
        return export(reportService.customerClaimPdf(), "CLAIM_REPORT.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping("/claims/excel")
    public ResponseEntity<byte[]> claimExcel() {
        return export(reportService.customerClaimExcel(), "CLAIM_REPORT.xlsx",
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    /**
     * Helper method to encapsulate ResponseEntity logic.
     * Note: If 'data' is null or empty, the Service layer should throw a NotFoundException
     * before reaching this point, which is then handled by the GlobalExceptionHandler.
     */
    private ResponseEntity<byte[]> export(byte[] data, String filename, MediaType mediaType) {
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(data);
    }
}