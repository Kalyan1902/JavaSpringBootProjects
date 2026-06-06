package com.cts.vis.repository;

import com.cts.vis.model.Policy;
import com.cts.vis.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByVehicleIn(List<Vehicle> vehicles);
    Optional<Policy> findByPolicyIdAndVehicleIn(Long policyId, List<Vehicle> vehicles);
    boolean existsByPolicyNumber(String policyNumber);

    List<Policy> findByStartDateBetween(LocalDate start, LocalDate end);
}