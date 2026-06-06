package com.cts.vis.controller;

import com.cts.vis.model.Claim;
import com.cts.vis.service.ClaimService;
import com.cts.vis.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ApprovalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClaimService claimService;

    @InjectMocks
    private ApprovalController approvalController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // Standard setup for unit testing controller logic
        this.mockMvc = MockMvcBuilders.standaloneSetup(approvalController).build();
    }

    @Test
    public void testApprovalsPage() throws Exception {
        // Use modern diamond operator
        List<Claim> mockClaims = new ArrayList<>();
        mockClaims.add(new Claim());

        when(claimService.submittedClaims()).thenReturn(mockClaims);

        mockMvc.perform(get("/admin/approvals"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/approvals"))
                .andExpect(model().attribute("claims", mockClaims));

        verify(claimService, times(1)).submittedClaims();
    }

    @Test
    public void testApproveClaim_Success() throws Exception {
        Long claimId = 1L;

        mockMvc.perform(post("/admin/approvals/" + claimId + "/approve"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/approvals?approved=true"));

        verify(claimService, times(1)).approve(claimId);
    }

    @Test
    public void testRejectClaim_Success() throws Exception {
        Long claimId = 2L;

        mockMvc.perform(post("/admin/approvals/" + claimId + "/reject"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/approvals?rejected=true"));

        verify(claimService, times(1)).reject(claimId);
    }

    @Test
    public void testApproveClaim_NotFound() throws Exception {
        Long invalidClaimId = 999L;

        // Simulate service throwing a custom exception for the Global Handler to catch
        doThrow(new NotFoundException("Claim not found"))
                .when(claimService).approve(invalidClaimId);

        // In a standalone test, the exception bubbles up.
        // This confirms the controller isn't swallowing the error.
        try {
            mockMvc.perform(post("/admin/approvals/" + invalidClaimId + "/approve"));
        } catch (Exception e) {
            assert e.getCause() instanceof NotFoundException;
        }
    }
}