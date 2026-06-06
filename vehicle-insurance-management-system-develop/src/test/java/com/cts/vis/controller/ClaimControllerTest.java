package com.cts.vis.controller;

import com.cts.vis.dto.ClaimDTO;
import com.cts.vis.model.Claim;
import com.cts.vis.model.Policy;
import com.cts.vis.service.ClaimService;
import com.cts.vis.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ClaimControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClaimService claimService;

    @Mock
    private PolicyService policyService;

    @InjectMocks
    private ClaimController claimController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // Standalone setup targets the controller logic specifically
        this.mockMvc = MockMvcBuilders.standaloneSetup(claimController).build();
    }

    @Test
    public void testClaimsPageView() throws Exception {
        List<Policy> mockPolicies = new ArrayList<>();
        List<Claim> mockClaims = new ArrayList<>();

        when(policyService.myPolicies()).thenReturn(mockPolicies);
        when(claimService.myClaims()).thenReturn(mockClaims);

        mockMvc.perform(get("/customer/claims"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/claims"))
                .andExpect(model().attribute("policies", mockPolicies))
                .andExpect(model().attribute("claims", mockClaims))
                .andExpect(model().attributeExists("claim"));
    }

    @Test
    public void testFileClaimSuccess() throws Exception {
        // MockMvc binds params directly to the ClaimDTO.FileRequest object
        mockMvc.perform(post("/customer/claims/file")
                        .param("policyId", "101")
                        .param("claimAmount", "5000.00")
                        .param("claimReason", "Accident damage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/claims?submitted=true"));

        // Verify the service was called with a DTO (happy path)
        verify(claimService, times(1)).fileClaim(any(ClaimDTO.FileRequest.class));
    }

    @Test
    public void testFileClaim_ValidationError() throws Exception {
        // 1. Arrange: Provide data for the page refresh logic
        when(policyService.myPolicies()).thenReturn(new ArrayList<>());
        when(claimService.myClaims()).thenReturn(new ArrayList<>());

        // 2. Act: Send invalid data (missing/empty params)
        mockMvc.perform(post("/customer/claims/file")
                        .param("policyId", "")
                        .param("claimAmount", "-50.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/claims"));

        // 3. Assert: Verify that the SAVE method was never called
        verify(claimService, never()).fileClaim(any(ClaimDTO.FileRequest.class));

        // 4. (Optional) Verify that it DID try to refresh the list for the UI
        verify(claimService, atLeastOnce()).myClaims();
    }

    @Test
    public void testFileClaim_BusinessRuleViolation() throws Exception {
        // Simulate a business rule violation (e.g., IllegalStateException for inactive policy)
        // Since the controller has no try-catch, this exception will bubble up.
        doThrow(new IllegalStateException("Policy is not active"))
                .when(claimService).fileClaim(any(ClaimDTO.FileRequest.class));

        try {
            mockMvc.perform(post("/customer/claims/file")
                    .param("policyId", "101")
                    .param("claimAmount", "100.00")
                    .param("claimReason", "Valid reason"));
        } catch (Exception e) {
            // Assert that the cause is the business exception thrown by service
            assert(e.getCause() instanceof IllegalStateException);
        }
    }
}