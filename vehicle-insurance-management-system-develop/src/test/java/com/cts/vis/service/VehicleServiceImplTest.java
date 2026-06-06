package com.cts.vis.service;

import com.cts.vis.dto.VehicleDTO;
import com.cts.vis.exception.NotFoundException;
import com.cts.vis.model.Customer;
import com.cts.vis.model.Vehicle;
import com.cts.vis.model.VehicleType;
import com.cts.vis.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceImplTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private CustomerService customerService;

    @Mock
    private ClaimService claimService;
    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Customer mockCustomer;
    private Vehicle mockVehicle;
    private VehicleDTO.CreateRequest createDto;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setCustomerId(1L);

        mockVehicle = new Vehicle();
        mockVehicle.setVehicleId(100L);
        mockVehicle.setRegistrationNumber("TN-01-AB-1234");
        mockVehicle.setMake("Honda");
        mockVehicle.setCustomer(mockCustomer);

        createDto = new VehicleDTO.CreateRequest();
        createDto.setRegistrationNumber("TN-01-AB-1234");
        createDto.setMake("Honda");
        createDto.setModel("Civic");
        createDto.setYearOfManufacture(2022);
        createDto.setVehicleType(VehicleType.CAR);
    }

    @Test
    void testAddVehicle_Success() {
        // Arrange
        when(vehicleRepository.existsByRegistrationNumber(createDto.getRegistrationNumber())).thenReturn(false);
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Vehicle result = vehicleService.addVehicle(createDto);

        // Assert
        assertNotNull(result);
        assertEquals(createDto.getRegistrationNumber(), result.getRegistrationNumber());
        assertEquals(mockCustomer, result.getCustomer());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void testGetUpdateDto_Mapping() {
        // Arrange
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByVehicleIdAndCustomer(100L, mockCustomer)).thenReturn(Optional.of(mockVehicle));

        // Act
        VehicleDTO.UpdateRequest dto = vehicleService.getUpdateDto(100L);

        // Assert
        assertNotNull(dto);
        assertEquals(mockVehicle.getRegistrationNumber(), dto.getRegistrationNumber());
        assertEquals("Honda", dto.getMake());
    }

    @Test
    void testUpdateVehicle_Success() {
        // Arrange
        VehicleDTO.UpdateRequest updateDto = new VehicleDTO.UpdateRequest();
        updateDto.setRegistrationNumber("KA-01-ZZ-9999");
        updateDto.setMake("Toyota");
        updateDto.setModel("Corolla");
        updateDto.setYearOfManufacture(2023); // <--- Add this to prevent NPE on line 97
        updateDto.setVehicleType(VehicleType.CAR);

        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByVehicleIdAndCustomer(100L, mockCustomer)).thenReturn(Optional.of(mockVehicle));

        // Ensure the claim check doesn't block the update
        when(claimService.hasApprovedClaimForVehicle(100L)).thenReturn(false);

        // Since we are changing the RegNo, we must mock the uniqueness check
        when(vehicleRepository.existsByRegistrationNumber("KA-01-ZZ-9999")).thenReturn(false);

        // Act
        vehicleService.updateVehicle(100L, updateDto);

        // Assert
        assertEquals("KA-01-ZZ-9999", mockVehicle.getRegistrationNumber());
        assertEquals("Toyota", mockVehicle.getMake());
        assertEquals(2023, mockVehicle.getYearOfManufacture());
        verify(vehicleRepository).save(mockVehicle);
    }

    @Test
    void testUpdateVehicle_ThrowsException_DuplicateRegNo() {
        // Arrange
        VehicleDTO.UpdateRequest updateDto = new VehicleDTO.UpdateRequest();
        updateDto.setRegistrationNumber("EXISTING-123");

        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByVehicleIdAndCustomer(100L, mockCustomer)).thenReturn(Optional.of(mockVehicle));
        when(vehicleRepository.existsByRegistrationNumber("EXISTING-123")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> vehicleService.updateVehicle(100L, updateDto));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void testGetMyVehicle_ThrowsNotFound() {
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByVehicleIdAndCustomer(anyLong(), eq(mockCustomer))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> vehicleService.getMyVehicle(999L));
    }
}