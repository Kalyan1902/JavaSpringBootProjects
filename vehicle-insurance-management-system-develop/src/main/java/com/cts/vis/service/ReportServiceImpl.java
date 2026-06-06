package com.cts.vis.service;

import com.cts.vis.model.*;
import com.cts.vis.repository.ClaimRepository;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final CustomerService customerService;
    private final VehicleRepository vehicleRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public Map<String, Object> customerDashboardStats() {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);
        List<Policy> policies = policyRepository.findByVehicleIn(vehicles);
        List<Claim> claims = claimRepository.findByPolicyIn(policies);

        long activePolicies = 0;
        BigDecimal totalPremium = BigDecimal.ZERO;

        for (Policy p : policies) {
            if (p.getPolicyStatus() == PolicyStatus.ACTIVE) {
                activePolicies++;
            }
            if (p.getPremiumAmount() != null) {
                totalPremium = totalPremium.add(p.getPremiumAmount());
            }
        }

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("totalVehicles", vehicles.size());
        m.put("activePolicies", activePolicies);
        m.put("totalClaims", claims.size());
        m.put("totalPremium", totalPremium);
        return m;
    }

    @Override
    @Transactional
    public Map<String, Object> customerPolicyReport() {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);
        List<Policy> policies = policyRepository.findByVehicleIn(vehicles);

        long activeCount = 0;
        BigDecimal totalPremium = BigDecimal.ZERO;

        for (Policy p : policies) {
            if (p.getPolicyStatus() == PolicyStatus.ACTIVE) {
                activeCount++;
            }
            if (p.getPremiumAmount() != null) {
                totalPremium = totalPremium.add(p.getPremiumAmount());
            }
        }

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("policies", policies);
        m.put("totalPolicies", policies.size());
        m.put("activePolicies", activeCount);
        m.put("totalPremium", totalPremium);
        return m;
    }

    @Override
    @Transactional
    public Map<String, Object> customerClaimReport() {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);
        List<Policy> policies = policyRepository.findByVehicleIn(vehicles);
        List<Claim> claims = claimRepository.findByPolicyIn(policies);

        long approvedCount = 0;
        BigDecimal totalClaimed = BigDecimal.ZERO;

        for (Claim c : claims) {
            if (c.getClaimStatus() == ClaimStatus.APPROVED) {
                approvedCount++;
            }
            if (c.getClaimAmount() != null) {
                totalClaimed = totalClaimed.add(c.getClaimAmount());
            }
        }

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("claims", claims);
        m.put("totalClaims", claims.size());
        m.put("approvedClaims", approvedCount);
        m.put("totalClaimed", totalClaimed);
        return m;
    }

    @Override
    public Map<String, Object> adminDashboardStats() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("totalCustomers", customerRepository.count());
        m.put("totalVehicles", vehicleRepository.count());
        m.put("totalPolicies", policyRepository.count());
        m.put("pendingClaims", (long) claimRepository.findByClaimStatus(ClaimStatus.SUBMITTED).size());
        m.put("approvedClaims", (long) claimRepository.findByClaimStatus(ClaimStatus.APPROVED).size());
        return m;
    }

    // ------------------ EXPORT LOGIC (NO STREAMS) ------------------

    @Override
    @Transactional
    public byte[] customerPolicyPdf() {
        Map<String, Object> m = customerPolicyReport();
        List<Policy> policies = (List<Policy>) m.get("policies");
        List<String> headers = Arrays.asList("Policy No", "Vehicle", "Premium", "Start", "End", "Status");

        List<List<String>> rows = new ArrayList<List<String>>();
        for (Policy p : policies) {
            List<String> row = new ArrayList<String>();
            row.add(safe(p.getPolicyNumber()));
            row.add(p.getVehicle() != null ? safe(p.getVehicle().getMake()) + " " + safe(p.getVehicle().getModel()) : "");
            row.add(s(p.getPremiumAmount()));
            row.add(s(p.getStartDate()));
            row.add(s(p.getEndDate()));
            row.add(p.getPolicyStatus() != null ? p.getPolicyStatus().name() : "");
            rows.add(row);
        }

        return buildPdf("My Policy Report", headers, rows, getSummary(m, "totalPolicies", "activePolicies", "totalPremium"));
    }

    @Override
    @Transactional
    public byte[] customerPolicyExcel() {
        Map<String, Object> m = customerPolicyReport();
        List<Policy> policies = (List<Policy>) m.get("policies");
        List<String> headers = Arrays.asList("Policy No", "Vehicle", "Premium", "Start", "End", "Status");

        List<List<String>> rows = new ArrayList<List<String>>();
        for (Policy p : policies) {
            List<String> row = new ArrayList<String>();
            row.add(safe(p.getPolicyNumber()));
            row.add(p.getVehicle() != null ? safe(p.getVehicle().getMake()) + " " + safe(p.getVehicle().getModel()) : "");
            row.add(s(p.getPremiumAmount()));
            row.add(s(p.getStartDate()));
            row.add(s(p.getEndDate()));
            row.add(p.getPolicyStatus() != null ? p.getPolicyStatus().name() : "");
            rows.add(row);
        }

        return buildExcel("My Policies", headers, rows, getSummary(m, "totalPolicies", "activePolicies", "totalPremium"));
    }

    @Override
    @Transactional
    public byte[] customerClaimPdf() {
        Map<String, Object> m = customerClaimReport();
        List<Claim> claims = (List<Claim>) m.get("claims");
        List<String> headers = Arrays.asList("Claim ID", "Policy No", "Amount", "Date", "Status");

        List<List<String>> rows = new ArrayList<List<String>>();
        for (Claim c : claims) {
            List<String> row = new ArrayList<String>();
            row.add(s(c.getClaimId()));
            row.add(c.getPolicy() != null ? safe(c.getPolicy().getPolicyNumber()) : "");
            row.add(s(c.getClaimAmount()));
            row.add(s(c.getClaimDate()));
            row.add(c.getClaimStatus() != null ? c.getClaimStatus().name() : "");
            rows.add(row);
        }

        return buildPdf("My Claim Report", headers, rows, getSummary(m, "totalClaims", "approvedClaims", "totalClaimed"));
    }

    @Override
    @Transactional
    public byte[] customerClaimExcel() {
        Map<String, Object> m = customerClaimReport();
        List<Claim> claims = (List<Claim>) m.get("claims");
        List<String> headers = Arrays.asList("Claim ID", "Policy No", "Amount", "Date", "Status");

        List<List<String>> rows = new ArrayList<List<String>>();
        for (Claim c : claims) {
            List<String> row = new ArrayList<String>();
            row.add(s(c.getClaimId()));
            row.add(c.getPolicy() != null ? safe(c.getPolicy().getPolicyNumber()) : "");
            row.add(s(c.getClaimAmount()));
            row.add(s(c.getClaimDate()));
            row.add(c.getClaimStatus() != null ? c.getClaimStatus().name() : "");
            rows.add(row);
        }

        return buildExcel("My Claims", headers, rows, getSummary(m, "totalClaims", "approvedClaims", "totalClaimed"));
    }

    // ------------------ BUILDERS ------------------

    private Map<String, String> getSummary(Map<String, Object> m, String k1, String k2, String k3) {
        Map<String, String> summary = new LinkedHashMap<String, String>();
        summary.put(k1, s(m.get(k1)));
        summary.put(k2, s(m.get(k2)));
        summary.put(k3, s(m.get(k3)));
        return summary;
    }

    private byte[] buildPdf(String title, List<String> headers, List<List<String>> rows, Map<String, String> summary) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(new Paragraph(title, new Font(Font.HELVETICA, 16, Font.BOLD)));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
            for (int i = 0; i < headers.size(); i++) {
                PdfPCell cell = new PdfPCell(new Phrase(safe(headers.get(i)), headerFont));
                cell.setBackgroundColor(new Color(15, 23, 42));
                cell.setPadding(8);
                table.addCell(cell);
            }

            for (int i = 0; i < rows.size(); i++) {
                List<String> r = rows.get(i);
                for (int j = 0; j < r.size(); j++) {
                    PdfPCell cell = new PdfPCell(new Phrase(safe(r.get(j))));
                    cell.setPadding(6);
                    table.addCell(cell);
                }
            }

            doc.add(table);
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph("Summary", new Font(Font.HELVETICA, 13, Font.BOLD)));

            for (Map.Entry<String, String> e : summary.entrySet()) {
                doc.add(new Paragraph(e.getKey() + ": " + e.getValue()));
            }

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF export failed", e);
        }
    }

    private byte[] buildExcel(String sheetName, List<String> headers, List<List<String>> rows, Map<String, String> summary) {
        try {
            Workbook wb = new XSSFWorkbook();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Sheet sheet = wb.createSheet(sheetName);

            CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font hf = wb.createFont();
            hf.setBold(true);
            headerStyle.setFont(hf);

            int r = 0;
            Row headerRow = sheet.createRow(r++);
            for (int c = 0; c < headers.size(); c++) {
                Cell cell = headerRow.createCell(c);
                cell.setCellValue(safe(headers.get(c)));
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < rows.size(); i++) {
                List<String> data = rows.get(i);
                Row row = sheet.createRow(r++);
                for (int c = 0; c < data.size(); c++) {
                    row.createCell(c).setCellValue(safe(data.get(c)));
                }
            }

            r++;
            Row sumTitle = sheet.createRow(r++);
            Cell sumCell = sumTitle.createCell(0);
            sumCell.setCellValue("Summary");
            sumCell.setCellStyle(headerStyle);

            for (Map.Entry<String, String> e : summary.entrySet()) {
                Row sr = sheet.createRow(r++);
                sr.createCell(0).setCellValue(e.getKey());
                sr.createCell(1).setCellValue(e.getValue());
            }

            for (int c = 0; c < headers.size(); c++) {
                sheet.autoSizeColumn(c);
            }

            wb.write(out);
            wb.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel export failed", e);
        }
    }

    private String safe(String s) { return s == null ? "" : s; }
    private String s(Object o) { return o == null ? "" : String.valueOf(o); }
}