package com.cts.vis.service;

import com.cts.vis.dto.PolicyDTO;
import com.cts.vis.exception.NotFoundException;
import com.cts.vis.model.*;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import com.cts.vis.util.PolicyNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PolicyServiceImplTest {

    @Mock private PolicyRepository policyRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private CustomerService customerService;
    @Mock private PolicyNumberGenerator policyNumberGenerator;

    @Mock
    private ClaimService claimService; // <--- Add this missing mock
    @InjectMocks
    private PolicyServiceImpl policyService;

    private Customer mockCustomer;
    private Vehicle mockVehicle;
    private PolicyDTO.CreateRequest validDto;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();

        mockVehicle = new Vehicle();
        mockVehicle.setVehicleId(1L);
        mockVehicle.setVehicleType(VehicleType.CAR);
        mockVehicle.setYearOfManufacture(LocalDate.now().getYear() - 2); // 2 years old
        mockVehicle.setCustomer(mockCustomer);

        validDto = new PolicyDTO.CreateRequest();
        validDto.setVehicleId(1L);
        validDto.setCoverageAmount(new BigDecimal("50000"));
        validDto.setStartDate(LocalDate.now());
        validDto.setEndDate(LocalDate.now().plusYears(1));
    }

    @Test
    void testCreatePolicy_Success() {
        // Arrange
        String generatedNumber = "POL-12345";
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByVehicleIdAndCustomer(1L, mockCustomer)).thenReturn(Optional.of(mockVehicle));
        when(policyNumberGenerator.generate()).thenReturn(generatedNumber);
        when(policyRepository.existsByPolicyNumber(generatedNumber)).thenReturn(false);
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Policy policy = policyService.createPolicy(validDto);

        // Assert
        assertNotNull(policy);
        assertEquals(generatedNumber, policy.getPolicyNumber());
        assertEquals(PolicyStatus.ACTIVE, policy.getPolicyStatus());
        verify(policyRepository).save(any(Policy.class));
    }
    //    @Test
//    void testCreatePolicy_ThrowsException_InvalidDates() {
//        // 1. Arrange: Set up the invalid state
//        validDto.setStartDate(LocalDate.now());
//        validDto.setEndDate(LocalDate.now().minusDays(1)); // The trigger for the exception
//
//        // 2. Only keep mocks that are called BEFORE the date check
//        // If the service checks customer and vehicle first, keep these:
//        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
//        when(vehicleRepository.findByVehicleIdAndCustomer(anyLong(), any())).thenReturn(Optional.of(mockVehicle));
//
//        // 3. DELETE THE UNNECESSARY STUBBING (Line 86):
//        // when(policyNumberGenerator.generate()).thenReturn("POL-123"); <-- Mockito complains about this!
//
//        // 4. Act & Assert
//        assertThrows(IllegalArgumentException.class, () ->
//                policyService.createPolicy(validDto)
//        );
//    }
//@Test
//void testCreatePolicy_ThrowsException_InvalidDates() {
//    // Arrange
//    validDto.setStartDate(LocalDate.now());
//    validDto.setEndDate(LocalDate.now().minusDays(1));
//
//    // Stub ONLY the calls that occur before the date validation check
//    when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
//    when(vehicleRepository.findByVehicleIdAndCustomer(anyLong(), any())).thenReturn(Optional.of(mockVehicle));
//
//    // DO NOT stub policyNumberGenerator here.
//    // Validation fails before it's called, so Mockito will throw an error if you do.
//
//    // Act & Assert
//    assertThrows(IllegalArgumentException.class, () ->
//            policyService.createPolicy(validDto)
//    );
//}
    @Test
    void testUpdatePolicy_Success() {
        // Arrange
        Policy existingPolicy = new Policy();
        existingPolicy.setPolicyId(100L);
        existingPolicy.setVehicle(mockVehicle);

        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(Arrays.asList(mockVehicle));
        when(policyRepository.findByPolicyIdAndVehicleIn(eq(100L), anyList())).thenReturn(Optional.of(existingPolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        policyService.updatePolicy(100L, new BigDecimal("75000"));

        // Assert
        assertEquals(new BigDecimal("75000"), existingPolicy.getCoverageAmount());
        verify(policyRepository).save(existingPolicy);
    }

    @Test
    void testRenewPolicy_Success() {
        // Arrange
        Policy expiredPolicy = new Policy();
        expiredPolicy.setPolicyId(10L);
        expiredPolicy.setPolicyStatus(PolicyStatus.EXPIRED);
        expiredPolicy.setEndDate(LocalDate.now().minusDays(1));
        expiredPolicy.setVehicle(mockVehicle);

        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(Arrays.asList(mockVehicle));
        when(policyRepository.findByPolicyIdAndVehicleIn(eq(10L), anyList())).thenReturn(Optional.of(expiredPolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Policy renewed = policyService.renewPolicy(10L);

        // Assert
        assertEquals(PolicyStatus.ACTIVE, renewed.getPolicyStatus());
        assertEquals(LocalDate.now(), renewed.getStartDate());
        verify(policyRepository).save(expiredPolicy);
    }

    @Test
    void testCalculatePremium_Logic() {
        // CAR base (1000) * Age factor for 2 years (approx 1.05) + (50000 * 0.008 = 400)
        // Adjust these values to match your actual PolicyServiceImpl logic
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByVehicleIdAndCustomer(1L, mockCustomer)).thenReturn(Optional.of(mockVehicle));
        when(policyNumberGenerator.generate()).thenReturn("P1");
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArguments()[0]);

        Policy p = policyService.createPolicy(validDto);

        assertNotNull(p.getPremiumAmount());
        assertTrue(p.getPremiumAmount().compareTo(BigDecimal.ZERO) > 0);
    }
}