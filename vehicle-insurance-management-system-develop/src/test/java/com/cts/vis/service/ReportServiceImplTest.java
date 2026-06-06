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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceImplTest {

    @Mock private CustomerService customerService;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private PolicyRepository policyRepository;
    @Mock private ClaimRepository claimRepository;
    @Mock private CustomerRepository customerRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Customer mockCustomer;
    private Vehicle mockVehicle;
    private Policy activePolicy;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setCustomerId(1L);

        mockVehicle = new Vehicle();
        mockVehicle.setVehicleId(10L);

        activePolicy = new Policy();
        activePolicy.setPolicyStatus(PolicyStatus.ACTIVE);
        activePolicy.setPremiumAmount(new BigDecimal("1200.00"));
        activePolicy.setVehicle(mockVehicle);
    }

    @Test
    void testCustomerPolicyReport_Aggregation() {
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(Collections.singletonList(mockVehicle));
        when(policyRepository.findByVehicleIn(anyList())).thenReturn(Collections.singletonList(activePolicy));

        Map<String, Object> report = reportService.customerPolicyReport();

        assertNotNull(report);
        assertTrue(report.containsKey("policies"));
        assertEquals(new BigDecimal("1200.00"), report.get("totalPremium"));
    }

//    @Test
//    void testCustomerClaimReport_Aggregation() {
//        Claim claim = new Claim();
//        claim.setClaimAmount(new BigDecimal("500.00"));
//        claim.setClaimStatus(ClaimStatus.APPROVED);
//
//        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
//        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(Collections.singletonList(mockVehicle));
//        when(policyRepository.findByVehicleIn(anyList())).thenReturn(Collections.singletonList(activePolicy));
//        when(claimRepository.findByPolicyIn(anyList())).thenReturn(Collections.singletonList(claim));
//
//        Map<String, Object> report = reportService.customerClaimReport();
//
//        assertNotNull(report);
//        // We assert based on what the service is strictly expected to return
//        assertTrue(report.containsKey("totalClaimAmount"), "Map should contain 'totalClaimAmount'");
//
//        BigDecimal actualTotal = (BigDecimal) report.get("totalClaimAmount");
//        assertNotNull(actualTotal);
//        assertEquals(0, new BigDecimal("500.00").compareTo(actualTotal));
//    }

    @Test
    void testCustomerPolicyPdf_HeaderCheck() {
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(Collections.singletonList(mockVehicle));
        when(policyRepository.findByVehicleIn(anyList())).thenReturn(Collections.singletonList(activePolicy));

        byte[] pdfBytes = reportService.customerPolicyPdf();

        assertNotNull(pdfBytes);
        // Verifying PDF magic number: %PDF
        assertTrue(pdfBytes.length > 4);
        assertEquals((byte) '%', pdfBytes[0]);
        assertEquals((byte) 'P', pdfBytes[1]);
    }

    @Test
    void testAdminDashboardStats_Counts() {
        when(customerRepository.count()).thenReturn(5L);
        when(policyRepository.count()).thenReturn(10L);
        when(claimRepository.findByClaimStatus(ClaimStatus.SUBMITTED)).thenReturn(Collections.emptyList());
        when(claimRepository.findByClaimStatus(ClaimStatus.APPROVED)).thenReturn(Collections.emptyList());

        Map<String, Object> stats = reportService.adminDashboardStats();

        assertEquals(5L, stats.get("totalCustomers"));
        assertEquals(10L, stats.get("totalPolicies"));
        assertEquals(0L, stats.get("pendingClaims"));
    }
}