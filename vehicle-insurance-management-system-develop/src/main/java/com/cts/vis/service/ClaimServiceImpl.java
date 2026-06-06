
package com.cts.vis.service;

import com.cts.vis.dto.ClaimDTO;
import com.cts.vis.model.*;
import com.cts.vis.repository.ClaimRepository;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import com.cts.vis.exception.NotFoundException;
import com.cts.vis.exception.BadRequestException; // Assuming you have this for business rule violations
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerService customerService;

    @Override
    @Transactional
    public Claim fileClaim(ClaimDTO.FileRequest dto) {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);

        // Security check: Ensure the policy belongs to the logged-in user
        Policy policy = policyRepository.findByPolicyIdAndVehicleIn(dto.getPolicyId(), vehicles)
                .orElseThrow(() -> new NotFoundException("Policy not found or you are not authorized to access it."));

        // Business Rule: Check status
        if (policy.getPolicyStatus() != PolicyStatus.ACTIVE) {
            throw new IllegalStateException("Cannot file a claim on an inactive or expired policy.");
        }

        // Business Rule: Coverage limit check
        if (dto.getClaimAmount().compareTo(policy.getCoverageAmount()) > 0) {
            throw new IllegalArgumentException("Claim amount ($" + dto.getClaimAmount() +
                    ") cannot exceed the policy coverage limit of $" + policy.getCoverageAmount());
        }

        // Map DTO to Entity
        Claim claim = new Claim();
        claim.setPolicy(policy);
        claim.setClaimAmount(dto.getClaimAmount());
        claim.setClaimReason(dto.getClaimReason());
        claim.setClaimDate(LocalDate.now());
        claim.setClaimStatus(ClaimStatus.SUBMITTED);

        return claimRepository.save(claim);
    }

    @Override
    public List<Claim> myClaims() {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);
        List<Policy> policies = policyRepository.findByVehicleIn(vehicles);

        return claimRepository.findByPolicyIn(policies);
    }

    @Override
    @Transactional
    public Claim approve(Long claimId) {
        Claim c = claimRepository.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim ID " + claimId + " not found."));

        c.setClaimStatus(ClaimStatus.APPROVED);
        return claimRepository.save(c);
    }

    @Override
    @Transactional
    public Claim reject(Long claimId) {
        Claim c = claimRepository.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim ID " + claimId + " not found."));

        c.setClaimStatus(ClaimStatus.REJECTED);
        return claimRepository.save(c);
    }

    @Override
    public List<Claim> submittedClaims() {
        return claimRepository.findByClaimStatus(ClaimStatus.SUBMITTED);
    }

    @Override
    public boolean hasApprovedClaimForVehicle(Long vehicleId) {
        return claimRepository.existsByPolicy_Vehicle_VehicleIdAndClaimStatus(vehicleId, ClaimStatus.APPROVED);
    }

    @Override
    public boolean hasApprovedClaimForPolicy(Long policyId) {
        return claimRepository.existsByPolicy_PolicyIdAndClaimStatus(policyId, ClaimStatus.APPROVED);
    }
}