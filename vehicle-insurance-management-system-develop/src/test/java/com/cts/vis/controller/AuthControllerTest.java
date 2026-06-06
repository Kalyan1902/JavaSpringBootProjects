package com.cts.vis.controller;

import com.cts.vis.dto.CustomerDTO;
import com.cts.vis.service.AuthService;
import com.cts.vis.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // Standalone setup is perfect for testing navigation and binding
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void testRootRedirect() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/login"));
    }

    @Test
    public void testCustomerLoginView() throws Exception {
        mockMvc.perform(get("/customer/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/login"));
    }

    @Test
    public void testAdminLoginView() throws Exception {
        mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/login"));
    }

    @Test
    public void testRegisterFormView() throws Exception {
        mockMvc.perform(get("/customer/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/register"))
                .andExpect(model().attributeExists("customer"));
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        // We perform the POST with params that MockMvc will bind to the RegisterRequest DTO
        mockMvc.perform(post("/customer/register")
                        .param("name", "John Doe")
                        .param("email", "john@example.com")
                        .param("phone", "9876543210")
                        .param("address", "123 Main Street")
                        .param("password", "Strong@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/login?registered=true"));

        // Verify the service was called with a DTO containing the right data
        verify(authService, times(1)).registerCustomer(any(CustomerDTO.RegisterRequest.class));
    }

    @Test
    public void testRegisterValidationFailure() throws Exception {
        // Leave out the name and provide an invalid email to trigger @Valid
        mockMvc.perform(post("/customer/register")
                        .param("name", "")
                        .param("email", "not-an-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/register"))
                .andExpect(model().hasErrors());

        // Verify service was NEVER called because validation failed at the controller level
        verifyNoInteractions(authService);
    }

    @Test
    public void testRegisterFailure_BusinessLogicError() throws Exception {
        // Simulate the service throwing a BadRequestException (e.g., Email already exists)
        doThrow(new BadRequestException("Email already registered."))
                .when(authService).registerCustomer(any(CustomerDTO.RegisterRequest.class));

        // Since the controller has no try-catch, the exception bubbles up in this standalone test.
        try {
            mockMvc.perform(post("/customer/register")
                    .param("name", "John Doe")
                    .param("email", "john@example.com")
                    .param("phone", "9876543210")
                    .param("address", "123 Main Street")
                    .param("password", "Strong@123"));
        } catch (Exception e) {
            // Assert that the exception thrown is the one from the service
            assert(e.getCause() instanceof BadRequestException);
        }
    }
}