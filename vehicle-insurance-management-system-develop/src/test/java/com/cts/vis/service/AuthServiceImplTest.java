package com.cts.vis.service;

import com.cts.vis.dto.CustomerDTO;
import com.cts.vis.exception.BadRequestException;
import com.cts.vis.model.Customer;
import com.cts.vis.model.User;
import com.cts.vis.model.UserRole;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private CustomerDTO.RegisterRequest registerRequest;
    private final String encodedPassword = "hashed_password";

    @BeforeEach
    void setUp() {
        registerRequest = new CustomerDTO.RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPhone("9876543210");
        registerRequest.setAddress("123 Main St");
        registerRequest.setPassword("StrongPassword@123");
    }

    @Test
    void testRegisterCustomer_Success() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(encodedPassword);

        User mockSavedUser = new User();
        mockSavedUser.setId(1L);
        mockSavedUser.setEmail(registerRequest.getEmail());
        when(userRepository.save(any(User.class))).thenReturn(mockSavedUser);

        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Customer registeredCustomer = authService.registerCustomer(registerRequest);

        // Assert
        assertNotNull(registeredCustomer);
        assertEquals(registerRequest.getName(), registeredCustomer.getName());
        assertEquals(registerRequest.getEmail(), registeredCustomer.getEmail());
        assertEquals(registerRequest.getPhone(), registeredCustomer.getPhone());
        assertEquals(1L, registeredCustomer.getUser().getId());

        // Verify
        verify(userRepository, times(1)).save(any(User.class));
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testRegisterCustomer_ThrowsBadRequest_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authService.registerCustomer(registerRequest);
        });

        assertTrue(exception.getMessage().contains("Email already registered"));
        verify(userRepository, never()).save(any(User.class));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void testRegisterCustomer_VerifyEntityMapping() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(encodedPassword);

        // Capturing the objects passed to save to verify fields
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Customer result = authService.registerCustomer(registerRequest);

        // Assert User properties
        User savedUser = result.getUser();
        assertEquals(registerRequest.getEmail(), savedUser.getEmail());
        assertEquals(encodedPassword, savedUser.getPasswordHash());
        assertEquals(UserRole.ROLE_CUSTOMER, savedUser.getRole());
        assertTrue(savedUser.getIsActive());

        // Assert Customer properties
        assertEquals(registerRequest.getPhone(), result.getPhone());
        assertEquals(registerRequest.getAddress(), result.getAddress());
    }
}