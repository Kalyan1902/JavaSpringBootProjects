package com.cts.vis.service;

import com.cts.vis.dto.PolicyDTO;
import com.cts.vis.model.Customer;
import com.cts.vis.model.Policy;
import com.cts.vis.model.PolicyStatus;
import com.cts.vis.model.Vehicle;
import com.cts.vis.model.VehicleType;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import com.cts.vis.util.PolicyNumberGenerator;
import com.cts.vis.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerService customerService;
    private final ClaimService claimService;
    private final PolicyNumberGenerator policyNumberGenerator;

    @Override
    @Transactional
    public Policy createPolicy(PolicyDTO.CreateRequest dto) {
        Customer customer = customerService.getCurrentCustomer();

        // 1. Validate Date Logic (Business Logic moved from Controller)
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and End date are required.");
        }
        if (!dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date.");
        }

        // 2. Find Vehicle
        Vehicle vehicle = vehicleRepository.findByVehicleIdAndCustomer(dto.getVehicleId(), customer)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found for this customer."));

        // 3. Generate Unique Policy Number
        String number;
        do {
            number = policyNumberGenerator.generate();
        } while (policyRepository.existsByPolicyNumber(number));

        // 4. Calculate Premium
        BigDecimal premium = calculatePremium(vehicle.getVehicleType(), vehicle.getYearOfManufacture(), dto.getCoverageAmount());

        Policy p = new Policy();
        p.setVehicle(vehicle);
        p.setPolicyNumber(number);
        p.setCoverageAmount(dto.getCoverageAmount());
        p.setPremiumAmount(premium);
        p.setStartDate(dto.getStartDate());
        p.setEndDate(dto.getEndDate());
        p.setPolicyStatus(getStatusByEndDate(dto.getEndDate()));

        return policyRepository.save(p);
    }

    @Override
    public Map<Long, Boolean> getPolicyLockStatus(List<Policy> policies) {
        Map<Long, Boolean> lockMap = new HashMap<>();
        for (Policy p : policies) {
            lockMap.put(p.getPolicyId(), claimService.hasApprovedClaimForPolicy(p.getPolicyId()));
        }
        return lockMap;
    }

    @Override
    @Transactional
    public Policy renewPolicy(Long policyId) {
        Policy p = getMyPolicy(policyId);

        // Refresh status before checking renewal eligibility
        refreshStatus(p);

        if (p.getPolicyStatus() != PolicyStatus.EXPIRED) {
            throw new IllegalStateException("Policy is still ACTIVE. Renewal is allowed only after expiry.");
        }

        LocalDate today = LocalDate.now();
        p.setStartDate(today);
        p.setEndDate(today.plusYears(1));
        p.setPolicyStatus(PolicyStatus.ACTIVE);

        return policyRepository.save(p);
    }

    @Override
    @Transactional
    public Policy updatePolicy(Long policyId, BigDecimal coverageAmount) {
        // Business Rule: Cannot edit if claims exist
        if (claimService.hasApprovedClaimForPolicy(policyId)) {
            throw new IllegalStateException("Policy is locked due to approved claims.");
        }

        Policy p = getMyPolicy(policyId);
        Vehicle v = p.getVehicle();

        BigDecimal premium = calculatePremium(v.getVehicleType(), v.getYearOfManufacture(), coverageAmount);

        p.setCoverageAmount(coverageAmount);
        p.setPremiumAmount(premium);

        return policyRepository.save(p);
    }

    @Override
    @Transactional
    public List<Policy> myPolicies() {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);
        List<Policy> policies = policyRepository.findByVehicleIn(vehicles);

        // Sync statuses with current date
        boolean changed = false;
        for (Policy p : policies) {
            if (refreshStatus(p)) changed = true;
        }
        if (changed) policyRepository.saveAll(policies);

        return policies;
    }

    @Override
    public Policy getMyPolicy(Long policyId) {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);

        return policyRepository.findByPolicyIdAndVehicleIn(policyId, vehicles)
                .orElseThrow(() -> new NotFoundException("Policy not found."));
    }

    // Helper Methods
    private PolicyStatus getStatusByEndDate(LocalDate endDate) {
        return LocalDate.now().isAfter(endDate) ? PolicyStatus.EXPIRED : PolicyStatus.ACTIVE;
    }

    private boolean refreshStatus(Policy p) {
        if (p.getEndDate() == null) return false;
        PolicyStatus newStatus = getStatusByEndDate(p.getEndDate());
        if (p.getPolicyStatus() != newStatus) {
            p.setPolicyStatus(newStatus);
            return true;
        }
        return false;
    }

    private BigDecimal calculatePremium(VehicleType type, int yearOfManufacture, BigDecimal coverageAmount) {
        BigDecimal base = switch (type) {
            case CAR -> BigDecimal.valueOf(1000);
            case BIKE -> BigDecimal.valueOf(500);
            case TRUCK -> BigDecimal.valueOf(1500);
            default -> BigDecimal.valueOf(1000);
        };

        int age = Math.max(0, Year.now().getValue() - yearOfManufacture);
        BigDecimal ageFactor = (age <= 3) ? BigDecimal.valueOf(1.0) :
                (age <= 7) ? BigDecimal.valueOf(1.1) :
                        (age <= 12) ? BigDecimal.valueOf(1.25) : BigDecimal.valueOf(1.4);

        BigDecimal coverageFactor = coverageAmount.multiply(BigDecimal.valueOf(0.008));
        return base.multiply(ageFactor).add(coverageFactor).setScale(2, RoundingMode.HALF_UP);
    }
}