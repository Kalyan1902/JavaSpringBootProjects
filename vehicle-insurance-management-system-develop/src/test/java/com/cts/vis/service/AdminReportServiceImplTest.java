package com.cts.vis.service;

import com.cts.vis.model.*;
import com.cts.vis.repository.ClaimRepository;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminReportServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private ClaimRepository claimRepository;

    @InjectMocks
    private AdminReportServiceImpl adminReportService;

    private LocalDate start;
    private LocalDate end;

    @BeforeEach
    void setUp() {
        start = LocalDate.now().minusMonths(1);
        end = LocalDate.now();
    }


    @Test
    @SuppressWarnings("unchecked")
    void testGeneratePolicyReport_CalculatesActiveAndPremium() {
        Policy p1 = new Policy();
        p1.setPolicyStatus(PolicyStatus.ACTIVE);
        p1.setPremiumAmount(new BigDecimal("1000.00"));

        Policy p2 = new Policy();
        p2.setPolicyStatus(PolicyStatus.EXPIRED);
        p2.setPremiumAmount(new BigDecimal("500.00"));

        when(policyRepository.findByStartDateBetween(start, end))
                .thenReturn(Arrays.asList(p1, p2));

        Map<String, Object> result = adminReportService.generate(ReportType.POLICY, start, end);

        List<Policy> rows = (List<Policy>) result.get("rows");
        assertEquals(2, rows.size());
        assertEquals(1L, result.get("activeCount"));
        // Using compareTo for BigDecimal to ignore scale differences (e.g., 1500.0 vs 1500.00)
        assertEquals(0, new BigDecimal("1500.00").compareTo((BigDecimal) result.get("totalPremium")));
    }

    @Test
    void testGenerateClaimReport_CalculatesApprovedAndTotal() {
        Claim c1 = new Claim();
        c1.setClaimStatus(ClaimStatus.APPROVED);
        c1.setClaimAmount(new BigDecimal("5000.00"));

        Claim c2 = new Claim();
        c2.setClaimStatus(ClaimStatus.REJECTED);
        c2.setClaimAmount(new BigDecimal("2000.00"));

        when(claimRepository.findByClaimDateBetween(start, end))
                .thenReturn(Arrays.asList(c1, c2));

        Map<String, Object> result = adminReportService.generate(ReportType.CLAIM, start, end);

        assertEquals(2L, result.get("count"));
        assertEquals(1L, result.get("approvedCount"));
        assertEquals(0, new BigDecimal("7000.00").compareTo((BigDecimal) result.get("totalClaimed")));
    }
}

