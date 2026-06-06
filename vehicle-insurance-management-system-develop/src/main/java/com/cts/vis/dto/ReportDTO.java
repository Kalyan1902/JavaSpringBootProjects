package com.cts.vis.dto;

import com.cts.vis.model.ReportType;
import lombok.Data;

import java.time.LocalDate;

public class ReportDTO {

    //ADMIN REPORT DTO

    @Data
    public static class AdminFilterRequest {
        //private ReportType type = ReportType.CUSTOMER;
        private ReportType type;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    //CUSTOMER REPORT DTO

    @Data
    public static class CustomerFilterRequest {
        private LocalDate startDate;
        private LocalDate endDate;
    }
}