package com.cts.vis.service;

import com.cts.vis.dto.CustomerDTO;
import com.cts.vis.exception.NotFoundException;
import com.cts.vis.model.Customer;
import com.cts.vis.model.User;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private final String testEmail = "customer@test.com";

    @BeforeEach
    void setupSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testEmail);
    }

    @Test
    void testGetCurrentCustomer_Success() {
        mockAuthentication();
        User mockUser = new User();
        mockUser.setEmail(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockUser));

        Customer mockCustomer = new Customer();
        mockCustomer.setName("Alice");
        when(customerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCustomer));

        Customer result = customerService.getCurrentCustomer();

        assertNotNull(result);
        assertEquals("Alice", result.getName());
    }

    @Test
    void testGetProfileUpdateDto() {
        // Arrange
        mockAuthentication();
        User mockUser = new User();
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockUser));

        Customer mockCustomer = new Customer();
        mockCustomer.setName("Alice");
        mockCustomer.setPhone("1234567890");
        mockCustomer.setAddress("Wonderland");
        when(customerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCustomer));

        // Act
        CustomerDTO.ProfileUpdateRequest dto = customerService.getProfileUpdateDto();

        // Assert
        assertEquals("Alice", dto.getName());
        assertEquals("1234567890", dto.getPhone());
        assertEquals("Wonderland", dto.getAddress());
    }

    @Test
    void testUpdateProfile_Success() {
        // Arrange
        mockAuthentication();
        User mockUser = new User();
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockUser));

        Customer mockCustomer = new Customer();
        mockCustomer.setName("Old Name");
        when(customerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCustomer));

        CustomerDTO.ProfileUpdateRequest dto = new CustomerDTO.ProfileUpdateRequest();
        dto.setName("New Name");
        dto.setPhone("9999999999");
        dto.setAddress("New Address");

        // Act
        customerService.updateProfile(dto);

        // Assert
        assertEquals("New Name", mockCustomer.getName());
        assertEquals("9999999999", mockCustomer.getPhone());
        verify(customerRepository, times(1)).save(mockCustomer);
    }

    @Test
    void testUpdateProfile_ThrowsException_WhenNameEmpty() {
        // Arrange
        mockAuthentication();
        User mockUser = new User();
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockUser));

        Customer mockCustomer = new Customer();
        when(customerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCustomer));

        CustomerDTO.ProfileUpdateRequest dto = new CustomerDTO.ProfileUpdateRequest();
        dto.setName(""); // Empty name should trigger the validation in service

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> customerService.updateProfile(dto));
        verify(customerRepository, never()).save(any());
    }
}