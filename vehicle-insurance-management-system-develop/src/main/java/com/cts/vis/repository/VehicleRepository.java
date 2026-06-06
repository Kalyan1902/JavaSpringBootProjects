package com.cts.vis.repository;

import com.cts.vis.model.Customer;
import com.cts.vis.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByCustomer(Customer customer);
    Optional<Vehicle> findByVehicleIdAndCustomer(Long id, Customer customer);
    boolean existsByRegistrationNumber(String regNo);

    List<Vehicle> findByCreatedDateBetween(LocalDate start, LocalDate end);
}