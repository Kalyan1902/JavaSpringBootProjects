//package com.cts.vis.service;
//import com.cts.vis.model.*;
//import com.cts.vis.repository.ClaimRepository;
//import com.cts.vis.repository.CustomerRepository;
//import com.cts.vis.repository.PolicyRepository;
//import com.cts.vis.repository.VehicleRepository;
//import com.lowagie.text.*;
//import com.lowagie.text.pdf.*;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.stereotype.Service;
//
//import java.awt.Color;
//import java.io.ByteArrayOutputStream;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.*;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class AdminReportServiceImpl implements AdminReportService {
//
//    private final CustomerRepository customerRepository;
//    private final VehicleRepository vehicleRepository;
//    private final PolicyRepository policyRepository;
//    private final ClaimRepository claimRepository;
//
//    @Override
//    @Transactional
//    public Map<String, Object> generate(ReportType type, LocalDate start, LocalDate end) {
//        // Normalization Logic: Provide defaults if inputs are null
//        ReportType actualType = (type == null) ? ReportType.CUSTOMER : type;
//        LocalDate actualStart = (start == null) ? LocalDate.now().minusMonths(6) : start;
//        LocalDate actualEnd = (end == null) ? LocalDate.now() : end;
//
//        Map<String, Object> model = new HashMap<String, Object>();
//        model.put("type", actualType);
//        model.put("start", actualStart);
//        model.put("end", actualEnd);
//
//        switch (actualType) {
//            case CUSTOMER:
//                List<Customer> customers = customerRepository.findByCreatedDateBetween(actualStart, actualEnd);
//                model.put("rows", customers);
//                model.put("count", (long) customers.size());
//                break;
//            case VEHICLE:
//                List<Vehicle> vehicles = vehicleRepository.findByCreatedDateBetween(actualStart, actualEnd);
//                model.put("rows", vehicles);
//                model.put("count", (long) vehicles.size());
//                break;
//            case POLICY:
//                List<Policy> policies = policyRepository.findByStartDateBetween(actualStart, actualEnd);
//                long active = 0;
//                BigDecimal totalPremium = BigDecimal.ZERO;
//                for (Policy p : policies) {
//                    if (p.getPolicyStatus() == PolicyStatus.ACTIVE) {
//                        active++;
//                    }
//                    if (p.getPremiumAmount() != null) {
//                        totalPremium = totalPremium.add(p.getPremiumAmount());
//                    }
//                }
//                model.put("rows", policies);
//                model.put("count", (long) policies.size());
//                model.put("activeCount", active);
//                model.put("totalPremium", totalPremium);
//                break;
//            case CLAIM:
//                List<Claim> claims = claimRepository.findByClaimDateBetween(actualStart, actualEnd);
//                long approved = 0;
//                BigDecimal totalClaimed = BigDecimal.ZERO;
//                for (Claim c : claims) {
//                    if (c.getClaimStatus() == ClaimStatus.APPROVED) {
//                        approved++;
//                    }
//                    if (c.getClaimAmount() != null) {
//                        totalClaimed = totalClaimed.add(c.getClaimAmount());
//                    }
//                }
//                model.put("rows", claims);
//                model.put("count", (long) claims.size());
//                model.put("approvedCount", approved);
//                model.put("totalClaimed", totalClaimed);
//                break;
//        }
//        return model;
//    }
//
//    @Override
//    public byte[] exportPdf(ReportType type, LocalDate start, LocalDate end) {
//        Map<String, Object> model = generate(type, start, end);
//        LocalDate s = (LocalDate) model.get("start");
//        LocalDate e = (LocalDate) model.get("end");
//        ReportType t = (ReportType) model.get("type");
//
//        String title = "Vehicle Insurance - " + t + " Report (" + s + " to " + e + ")";
//        return buildPdf(title, headersFor(t), rowsFor(t, model.get("rows")), summaryFor(t, model));
//    }
//
//    @Override
//    public byte[] exportExcel(ReportType type, LocalDate start, LocalDate end) {
//        Map<String, Object> model = generate(type, start, end);
//        ReportType t = (ReportType) model.get("type");
//        return buildExcel(t + " Report", headersFor(t), rowsFor(t, model.get("rows")), summaryFor(t, model));
//    }
//
//    // --- Helper Methods ---
//
//    private List<String> headersFor(ReportType type) {
//        switch (type) {
//            case CUSTOMER: return Arrays.asList("ID", "Name", "Email", "Phone", "Created");
//            case VEHICLE:  return Arrays.asList("ID", "Reg No", "Owner", "Make", "Model", "Type");
//            case POLICY:   return Arrays.asList("Policy No", "Customer", "Premium", "Start", "End", "Status");
//            case CLAIM:    return Arrays.asList("ID", "Policy", "Customer", "Amount", "Reason", "Status");
//            default:       return new ArrayList<String>();
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    private List<List<String>> rowsFor(ReportType type, Object rowsObj) {
//        List<List<String>> out = new ArrayList<List<String>>();
//        if (rowsObj == null) return out;
//
//        if (type == ReportType.CUSTOMER) {
//            List<Customer> customers = (List<Customer>) rowsObj;
//            for (Customer c : customers) {
//                out.add(Arrays.asList(s(c.getCustomerId()), safe(c.getName()), safe(c.getEmail()), safe(c.getPhone()), s(c.getCreatedDate())));
//            }
//        } else if (type == ReportType.VEHICLE) {
//            List<Vehicle> vehicles = (List<Vehicle>) rowsObj;
//            for (Vehicle v : vehicles) {
//                String owner = v.getCustomer() != null ? safe(v.getCustomer().getName()) : "N/A";
//                out.add(Arrays.asList(s(v.getVehicleId()), safe(v.getRegistrationNumber()), owner, safe(v.getMake()), safe(v.getModel()), s(v.getVehicleType())));
//            }
//        } else if (type == ReportType.POLICY) {
//            List<Policy> policies = (List<Policy>) rowsObj;
//            for (Policy p : policies) {
//                String cust = (p.getVehicle() != null && p.getVehicle().getCustomer() != null) ? p.getVehicle().getCustomer().getName() : "N/A";
//                out.add(Arrays.asList(safe(p.getPolicyNumber()), cust, s(p.getPremiumAmount()), s(p.getStartDate()), s(p.getEndDate()), s(p.getPolicyStatus())));
//            }
//        } else if (type == ReportType.CLAIM) {
//            List<Claim> claims = (List<Claim>) rowsObj;
//            for (Claim c : claims) {
//                String pol = c.getPolicy() != null ? c.getPolicy().getPolicyNumber() : "N/A";
//                String cust = (c.getPolicy() != null && c.getPolicy().getVehicle() != null && c.getPolicy().getVehicle().getCustomer() != null) ? c.getPolicy().getVehicle().getCustomer().getName() : "N/A";
//                out.add(Arrays.asList(s(c.getClaimId()), pol, cust, s(c.getClaimAmount()), safe(c.getClaimReason()), s(c.getClaimStatus())));
//            }
//        }
//        return out;
//    }
//
//    private Map<String, String> summaryFor(ReportType type, Map<String, Object> model) {
//        Map<String, String> summary = new LinkedHashMap<String, String>();
//        summary.put("Total Records", s(model.get("count")));
//        if (type == ReportType.POLICY) {
//            summary.put("Active Policies", s(model.get("activeCount")));
//            summary.put("Total Premium", "$" + s(model.get("totalPremium")));
//        } else if (type == ReportType.CLAIM) {
//            summary.put("Approved Claims", s(model.get("approvedCount")));
//            summary.put("Total Payout", "$" + s(model.get("totalClaimed")));
//        }
//        return summary;
//    }
//
//    private byte[] buildPdf(String title, List<String> headers, List<List<String>> rows, Map<String, String> summary) {
//        try {
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            Document doc = new Document(PageSize.A4.rotate());
//            PdfWriter.getInstance(doc, out);
//            doc.open();
//
//            doc.add(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
//            doc.add(Chunk.NEWLINE);
//
//            PdfPTable table = new PdfPTable(headers.size());
//            table.setWidthPercentage(100);
//
//            com.lowagie.text.Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
//            for (String header : headers) {
//                PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
//                cell.setBackgroundColor(new Color(30, 41, 59));
//                cell.setPadding(5);
//                table.addCell(cell);
//            }
//
//            for (List<String> rowData : rows) {
//                for (String cellData : rowData) {
//                    table.addCell(new Phrase(cellData, FontFactory.getFont(FontFactory.HELVETICA, 10)));
//                }
//            }
//
//            doc.add(table);
//            doc.add(Chunk.NEWLINE);
//
//            doc.add(new Paragraph("Summary Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
//            Iterator<Map.Entry<String, String>> it = summary.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry<String, String> entry = it.next();
//                doc.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
//            }
//
//            doc.close();
//            return out.toByteArray();
//        } catch (Exception e) { throw new RuntimeException("PDF Error", e); }
//    }
//
//    private byte[] buildExcel(String sheetName, List<String> headers, List<List<String>> rows, Map<String, String> summary) {
//        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
//            Sheet sheet = wb.createSheet(sheetName);
//
//            Row headerRow = sheet.createRow(0);
//            CellStyle headerStyle = wb.createCellStyle();
//            org.apache.poi.ss.usermodel.Font font = wb.createFont();
//            font.setBold(true);
//            headerStyle.setFont(font);
//
//            for (int i = 0; i < headers.size(); i++) {
//                Cell cell = headerRow.createCell(i);
//                cell.setCellValue(headers.get(i));
//                cell.setCellStyle(headerStyle);
//            }
//
//            for (int i = 0; i < rows.size(); i++) {
//                List<String> rowData = rows.get(i);
//                Row row = sheet.createRow(i + 1);
//                for (int j = 0; j < rowData.size(); j++) {
//                    row.createCell(j).setCellValue(rowData.get(j));
//                }
//            }
//
//            for (int i = 0; i < headers.size(); i++) {
//                sheet.autoSizeColumn(i);
//            }
//
//            wb.write(out);
//            return out.toByteArray();
//        } catch (Exception e) { throw new RuntimeException("Excel Error", e); }
//    }
//
//    private String safe(String s) { return s == null ? "" : s; }
//    private String s(Object o) { return o == null ? "" : String.valueOf(o); }
//}
package com.cts.vis.service;

import com.cts.vis.model.Claim;
import com.cts.vis.model.ClaimStatus;
import com.cts.vis.model.Policy;
import com.cts.vis.model.PolicyStatus;
import com.cts.vis.model.ReportType;
import com.cts.vis.repository.ClaimRepository;
import com.cts.vis.repository.PolicyRepository;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    private static final List<ReportType> ALLOWED_TYPES = List.of(ReportType.POLICY, ReportType.CLAIM);

    private ReportType normalizeAndValidateType(ReportType type) {
        ReportType actualType = (type == null) ? ReportType.POLICY : type;
        if (!ALLOWED_TYPES.contains(actualType)) {
            throw new IllegalArgumentException("Only POLICY and CLAIM reports are allowed in Admin portal.");
        }
        return actualType;
    }

    private LocalDate normalizeStart(LocalDate start, LocalDate end) {
        return (start == null) ? end.minusMonths(6) : start;
    }

    private LocalDate normalizeEnd(LocalDate end) {
        return (end == null) ? LocalDate.now() : end;
    }

    @Override
    @Transactional
    public Map<String, Object> generate(ReportType type, LocalDate start, LocalDate end) {

        ReportType actualType = normalizeAndValidateType(type);
        LocalDate actualEnd = normalizeEnd(end);
        LocalDate actualStart = normalizeStart(start, actualEnd);

        Map<String, Object> model = new HashMap<>();
        model.put("type", actualType);
        model.put("start", actualStart);
        model.put("end", actualEnd);

        if (actualType == ReportType.POLICY) {
            List<Policy> policies = policyRepository.findByStartDateBetween(actualStart, actualEnd);

            long active = 0;
            BigDecimal totalPremium = BigDecimal.ZERO;

            for (Policy p : policies) {
                if (p.getPolicyStatus() == PolicyStatus.ACTIVE) active++;
                if (p.getPremiumAmount() != null) totalPremium = totalPremium.add(p.getPremiumAmount());
            }

            model.put("rows", policies);
            model.put("count", (long) policies.size());
            model.put("activeCount", active);
            model.put("totalPremium", totalPremium);
        }

        if (actualType == ReportType.CLAIM) {
            List<Claim> claims = claimRepository.findByClaimDateBetween(actualStart, actualEnd);

            long approved = 0;
            BigDecimal totalClaimed = BigDecimal.ZERO;

            for (Claim c : claims) {
                if (c.getClaimStatus() == ClaimStatus.APPROVED) approved++;
                if (c.getClaimAmount() != null) totalClaimed = totalClaimed.add(c.getClaimAmount());
            }

            model.put("rows", claims);
            model.put("count", (long) claims.size());
            model.put("approvedCount", approved);
            model.put("totalClaimed", totalClaimed);
        }

        return model;
    }

    @Override
    public byte[] exportPdf(ReportType type, LocalDate start, LocalDate end) {
        Map<String, Object> model = generate(type, start, end);

        LocalDate s = (LocalDate) model.get("start");
        LocalDate e = (LocalDate) model.get("end");
        ReportType t = (ReportType) model.get("type");

        String title = "Vehicle Insurance - " + t + " Report (" + s + " to " + e + ")";
        return buildPdf(title, headersFor(t), rowsFor(t, model.get("rows")), summaryFor(t, model));
    }

    @Override
    public byte[] exportExcel(ReportType type, LocalDate start, LocalDate end) {
        Map<String, Object> model = generate(type, start, end);

        ReportType t = (ReportType) model.get("type");
        return buildExcel(t + " Report", headersFor(t), rowsFor(t, model.get("rows")), summaryFor(t, model));
    }

    // ---------------- Helpers ----------------

    private List<String> headersFor(ReportType type) {
        if (type == ReportType.POLICY) {
            return Arrays.asList("Policy No", "Customer", "Coverage", "Premium", "Status");
        }
        if (type == ReportType.CLAIM) {
            return Arrays.asList("ID", "Policy", "Customer", "Amount", "Status");
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<List<String>> rowsFor(ReportType type, Object rowsObj) {
        List<List<String>> out = new ArrayList<>();
        if (rowsObj == null) return out;

        if (type == ReportType.POLICY) {
            List<Policy> policies = (List<Policy>) rowsObj;
            for (Policy p : policies) {
                String cust = (p.getVehicle() != null && p.getVehicle().getCustomer() != null)
                        ? safe(p.getVehicle().getCustomer().getName())
                        : "N/A";

                out.add(Arrays.asList(
                        safe(p.getPolicyNumber()),
                        cust,
                        s(p.getCoverageAmount()),
                        s(p.getPremiumAmount()),
                        s(p.getPolicyStatus())
                ));
            }
        }

        if (type == ReportType.CLAIM) {
            List<Claim> claims = (List<Claim>) rowsObj;
            for (Claim c : claims) {
                String pol = (c.getPolicy() != null) ? safe(c.getPolicy().getPolicyNumber()) : "N/A";
                String cust = (c.getPolicy() != null
                        && c.getPolicy().getVehicle() != null
                        && c.getPolicy().getVehicle().getCustomer() != null)
                        ? safe(c.getPolicy().getVehicle().getCustomer().getName())
                        : "N/A";

                out.add(Arrays.asList(
                        s(c.getClaimId()),
                        pol,
                        cust,
                        s(c.getClaimAmount()),
                        s(c.getClaimStatus())
                ));
            }
        }

        return out;
    }

    private Map<String, String> summaryFor(ReportType type, Map<String, Object> model) {
        Map<String, String> summary = new LinkedHashMap<>();
        summary.put("Total Records", s(model.get("count")));

        if (type == ReportType.POLICY) {
            summary.put("Active Policies", s(model.get("activeCount")));
            summary.put("Total Premium", s(model.get("totalPremium"))); // no $
        }

        if (type == ReportType.CLAIM) {
            summary.put("Approved Claims", s(model.get("approvedCount")));
            summary.put("Total Claimed", s(model.get("totalClaimed"))); // no $
        }

        return summary;
    }

    private byte[] buildPdf(String title, List<String> headers, List<List<String>> rows, Map<String, String> summary) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            com.lowagie.text.Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
                cell.setBackgroundColor(new Color(30, 41, 59));
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (List<String> rowData : rows) {
                for (String cellData : rowData) {
                    table.addCell(new Phrase(cellData, FontFactory.getFont(FontFactory.HELVETICA, 10)));
                }
            }

            doc.add(table);
            doc.add(Chunk.NEWLINE);

            doc.add(new Paragraph("Summary", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            for (Map.Entry<String, String> entry : summary.entrySet()) {
                doc.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
            }

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF Error", e);
        }
    }

    private byte[] buildExcel(String sheetName, List<String> headers, List<List<String>> rows, Map<String, String> summary) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet(sheetName);

            // header style
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font poiFont = wb.createFont();
            poiFont.setBold(true);
            headerStyle.setFont(poiFont);

            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // data rows
            for (int i = 0; i < rows.size(); i++) {
                List<String> rowData = rows.get(i);
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < rowData.size(); j++) {
                    row.createCell(j).setCellValue(rowData.get(j));
                }
            }

            // autosize
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel Error", e);
        }
    }

    private String safe(String s) { return s == null ? "" : s; }
    private String s(Object o) { return o == null ? "" : String.valueOf(o); }
}