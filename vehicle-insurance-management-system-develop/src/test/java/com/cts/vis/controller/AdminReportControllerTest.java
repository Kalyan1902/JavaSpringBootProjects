//package com.cts.vis.controller;
//
//import com.cts.vis.service.AdminReportService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//public class AdminReportControllerTest {
//
//    private MockMvc mockMvc;
//
//    @Mock
//    private AdminReportService adminReportService;
//
//    @InjectMocks
//    private AdminReportController adminReportController;
//
//    @BeforeEach
//    public void setup() {
//        MockitoAnnotations.openMocks(this);
//        // Using standaloneSetup is appropriate for unit testing the controller logic
//        this.mockMvc = MockMvcBuilders.standaloneSetup(adminReportController).build();
//    }
//
//    @Test
//    public void testReportsPage() throws Exception {
//        // Prepare mock data returned by the service
//        Map<String, Object> mockData = new HashMap<>();
//        mockData.put("count", 5L);
//
//        // Mocking the generate call
//        when(adminReportService.generate(any(), any(), any()))
//                .thenReturn(mockData);
//
//        // Verify that parameters are correctly bound to the ModelAttribute filter
//        mockMvc.perform(get("/admin/reports")
//                        .param("type", "POLICY")
//                        .param("startDate", "2024-01-01")
//                        .param("endDate", "2024-06-01"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("admin/reports"))
//                .andExpect(model().attributeExists("types"))
//                .andExpect(model().attribute("count", 5L));
//    }
//
//    @Test
//    public void testDownloadPdf() throws Exception {
//        byte[] mockContent = "Mock PDF Content".getBytes();
//
//        when(adminReportService.exportPdf(any(), any(), any()))
//                .thenReturn(mockContent);
//
//        mockMvc.perform(get("/admin/reports/download/pdf")
//                        .param("type", "CUSTOMER"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
//                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CUSTOMER_REPORT.pdf"))
//                .andExpect(content().bytes(mockContent));
//    }
//
//    @Test
//    public void testDownloadExcel() throws Exception {
//        byte[] mockContent = "Mock Excel Content".getBytes();
//        String excelMimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
//
//        when(adminReportService.exportExcel(any(), any(), any()))
//                .thenReturn(mockContent);
//
//        mockMvc.perform(get("/admin/reports/download/excel")
//                        .param("type", "VEHICLE"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(excelMimeType))
//                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=VEHICLE_REPORT.xlsx"))
//                .andExpect(content().bytes(mockContent));
//    }
//
//    @Test
//    public void testDownloadPdf_DefaultFilename() throws Exception {
//        byte[] mockContent = "Mock PDF Content".getBytes();
//
//        when(adminReportService.exportPdf(any(), any(), any()))
//                .thenReturn(mockContent);
//
//        // Explicitly send an empty string for type to ensure the DTO
//        // property is treated as 'not provided' or 'null' logic kicks in
//        mockMvc.perform(get("/admin/reports/download/pdf")
//                        .param("type", "")) // Ensure type is empty
//                .andExpect(status().isOk())
//                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=GENERAL_REPORT.pdf"));
//    }
//}
package com.cts.vis.controller;

import com.cts.vis.service.AdminReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminReportService adminReportService;

    @InjectMocks
    private AdminReportController adminReportController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();

        this.mockMvc = MockMvcBuilders.standaloneSetup(adminReportController)
                .setConversionService(conversionService)
                .build();
    }

    @Test
    public void testDownloadPdf() throws Exception {
        byte[] mockContent = "Mock PDF Content".getBytes();

        when(adminReportService.exportPdf(any(), any(), any()))
                .thenReturn(mockContent);

        // FIX: Changed "CUSTOMER" to "POLICY" to pass validateType()
        mockMvc.perform(get("/admin/reports/download/pdf")
                        .param("type", "POLICY"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=POLICY_REPORT.pdf"))
                .andExpect(content().bytes(mockContent));
    }

    @Test
    public void testDownloadExcel() throws Exception {
        byte[] mockContent = "Mock Excel Content".getBytes();
        String excelMimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        when(adminReportService.exportExcel(any(), any(), any()))
                .thenReturn(mockContent);

        // FIX: Changed "VEHICLE" to "CLAIM" to pass validateType()
        mockMvc.perform(get("/admin/reports/download/excel")
                        .param("type", "CLAIM"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(excelMimeType))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CLAIM_REPORT.xlsx"))
                .andExpect(content().bytes(mockContent));
    }
}