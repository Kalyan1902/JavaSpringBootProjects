
package com.cts.vis.service;
import com.cts.vis.dto.ClaimDTO;
import com.cts.vis.model.Claim;
import java.math.BigDecimal;
import java.util.List;
public interface ClaimService {
    // Service now accepts the DTO directly
    Claim fileClaim(ClaimDTO.FileRequest dto);

    List<Claim> myClaims();
    List<Claim> submittedClaims(); // Admin use
    boolean hasApprovedClaimForVehicle(Long vehicleId);
    boolean hasApprovedClaimForPolicy(Long policyId);

    Claim approve(Long claimId);
    Claim reject(Long claimId);
}