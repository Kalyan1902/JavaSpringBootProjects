package com.cts.vis.service;

import com.cts.vis.dto.ClaimDTO;
import com.cts.vis.exception.NotFoundException;
import com.cts.vis.model.*;
import com.cts.vis.repository.ClaimRepository;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClaimServiceImplTest {

    @Mock private ClaimRepository claimRepository;
    @Mock private PolicyRepository policyRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private CustomerService customerService;

    @InjectMocks private ClaimServiceImpl claimService;

    private Customer mockCustomer;
    private Policy mockPolicy;
    private List<Vehicle> mockVehicles;
    private ClaimDTO.FileRequest validDto;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setCustomerId(1L);

        Vehicle v = new Vehicle();
        v.setVehicleId(10L);
        mockVehicles = Collections.singletonList(v);

        mockPolicy = new Policy();
        mockPolicy.setPolicyId(100L);
        mockPolicy.setPolicyStatus(PolicyStatus.ACTIVE);
        mockPolicy.setCoverageAmount(new BigDecimal("5000.00"));

        validDto = new ClaimDTO.FileRequest();
        validDto.setPolicyId(100L);
        validDto.setClaimAmount(new BigDecimal("2000.00"));
        validDto.setClaimReason("Accident");
    }

    @Test
    void testFileClaim_Success() {
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(mockVehicles);
        when(policyRepository.findByPolicyIdAndVehicleIn(eq(100L), eq(mockVehicles)))
                .thenReturn(Optional.of(mockPolicy));
        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> i.getArguments()[0]);

        Claim result = claimService.fileClaim(validDto);

        assertNotNull(result);
        assertEquals(ClaimStatus.SUBMITTED, result.getClaimStatus());
        assertEquals(new BigDecimal("2000.00"), result.getClaimAmount());
        verify(claimRepository).save(any(Claim.class));
    }

    @Test
    void testFileClaim_ThrowsException_WhenAmountExceedsCoverage() {
        validDto.setClaimAmount(new BigDecimal("6000.00")); // Policy limit is 5000
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(mockVehicles);
        when(policyRepository.findByPolicyIdAndVehicleIn(100L, mockVehicles))
                .thenReturn(Optional.of(mockPolicy));

        assertThrows(IllegalArgumentException.class, () -> claimService.fileClaim(validDto));
        verify(claimRepository, never()).save(any());
    }

    // ... approve and reject tests remain largely the same as they use IDs
}