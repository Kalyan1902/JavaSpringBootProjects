package com.cts.vis.controller;

import com.cts.vis.model.Customer;
import com.cts.vis.model.Policy;
import com.cts.vis.model.Vehicle;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import com.cts.vis.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    public void testDashboard() throws Exception {
        // Arrange
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", 10L);
        stats.put("totalRevenue", 50000.0);

        when(reportService.adminDashboardStats()).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("totalCustomers", 10L))
                .andExpect(model().attribute("totalRevenue", 50000.0));
    }

    @Test
    public void testCustomersPage() throws Exception {
        // Arrange
        when(customerRepository.findAll()).thenReturn(Collections.singletonList(new Customer()));

        // Act & Assert
        mockMvc.perform(get("/admin/customers"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/customers"))
                .andExpect(model().attributeExists("customers"));
    }

    @Test
    public void testVehiclesPage() throws Exception {
        // Arrange
        when(vehicleRepository.findAll()).thenReturn(Collections.singletonList(new Vehicle()));

        // Act & Assert
        mockMvc.perform(get("/admin/vehicles"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vehicles"))
                .andExpect(model().attributeExists("vehicles"));
    }

    @Test
    public void testPoliciesPage() throws Exception {
        // Arrange
        when(policyRepository.findAll()).thenReturn(Collections.singletonList(new Policy()));

        // Act & Assert
        mockMvc.perform(get("/admin/policies"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/policies"))
                .andExpect(model().attributeExists("policies"));
    }
}