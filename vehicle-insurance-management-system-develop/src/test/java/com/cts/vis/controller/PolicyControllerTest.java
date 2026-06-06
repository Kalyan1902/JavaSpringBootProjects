package com.cts.vis.controller;

import com.cts.vis.dto.PolicyDTO;
import com.cts.vis.model.Policy;
import com.cts.vis.model.Vehicle;
import com.cts.vis.service.PolicyService;
import com.cts.vis.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PolicyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PolicyService policyService;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private PolicyController policyController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(policyController).build();
    }

    @Test
    public void testPoliciesPage() throws Exception {
        List<Policy> mockPolicies = new ArrayList<>();
        List<Vehicle> mockVehicles = new ArrayList<>();
        Map<Long, Boolean> lockStatus = new HashMap<>();

        when(policyService.myPolicies()).thenReturn(mockPolicies);
        when(vehicleService.myVehicles()).thenReturn(mockVehicles);
        when(policyService.getPolicyLockStatus(mockPolicies)).thenReturn(lockStatus);

        mockMvc.perform(get("/customer/policies"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/policies"))
                .andExpect(model().attribute("policies", mockPolicies))
                .andExpect(model().attribute("vehicles", mockVehicles))
                .andExpect(model().attributeExists("editDisabled", "today", "policy"));
    }

//    @Test
//    public void testCreatePolicySuccess() throws Exception {
//        // MockMvc binds parameters to PolicyDTO.CreateRequest
//        mockMvc.perform(post("/customer/policies/create")
//                        .param("vehicleId", "1")
//                        .param("coverageAmount", "10000")
//                        .param("startDate", "2026-02-06")
//                        .param("endDate", "2027-02-06"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/customer/policies?created=true"));
//
//        verify(policyService, times(1)).createPolicy(any(PolicyDTO.CreateRequest.class));
//    }
@Test
public void testCreatePolicySuccess() throws Exception {
    // We use LocalDate.now() to ensure the dates pass any @Future or @CurrentDate validation
    String futureStart = LocalDate.now().plusDays(1).toString();
    String futureEnd = LocalDate.now().plusYears(1).toString();

    mockMvc.perform(post("/customer/policies/create")
                    .param("vehicleId", "1")
                    .param("coverageAmount", "10000.0") // Added decimal for Double/BigDecimal
                    .param("startDate", futureStart)
                    .param("endDate", futureEnd))
            .andExpect(status().is3xxRedirection()) // This matches your controller's 'redirect:'
            .andExpect(redirectedUrl("/customer/policies?created=true"));

    // This should now be invoked because validation passed
    verify(policyService, times(1)).createPolicy(any(PolicyDTO.CreateRequest.class));
}

    @Test
    public void testCreatePolicy_ValidationFailure() throws Exception {
        // 1. Arrange: Mock the calls inside refreshDashboard so they don't return null
        when(policyService.myPolicies()).thenReturn(new ArrayList<>());
        when(policyService.getPolicyLockStatus(anyList())).thenReturn(new HashMap<>());
        when(vehicleService.myVehicles()).thenReturn(new ArrayList<>());

        // 2. Act: Send invalid data (e.g., missing coverageAmount)
        mockMvc.perform(post("/customer/policies/create")
                        .param("vehicleId", "1")
                        .param("coverageAmount", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/policies"))
                .andExpect(model().attributeExists("policies", "vehicles"));

        // 3. Assert: Verify the CREATE method was NEVER called
        verify(policyService, never()).createPolicy(any(PolicyDTO.CreateRequest.class));

        // 4. Verification: You can optionally verify the "Read" methods WERE called
        verify(policyService, atLeastOnce()).myPolicies();
        verify(policyService, atLeastOnce()).getPolicyLockStatus(anyList());
    }

    @Test
    public void testRenewPolicy() throws Exception {
        Long policyId = 1L;

        mockMvc.perform(post("/customer/policies/" + policyId + "/renew"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/policies?renewed=true"));

        verify(policyService).renewPolicy(policyId);
    }

    @Test
    public void testEditPageView() throws Exception {
        Policy mockPolicy = new Policy();
        when(policyService.getMyPolicy(1L)).thenReturn(mockPolicy);

        mockMvc.perform(get("/customer/policies/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/policy-edit"))
                .andExpect(model().attribute("policy", mockPolicy));
    }

    @Test
    public void testUpdatePolicySuccess() throws Exception {
        mockMvc.perform(post("/customer/policies/1/edit")
                        .param("coverageAmount", "15000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/policies?updated=true"));

        verify(policyService).updatePolicy(eq(1L), any());
    }
}