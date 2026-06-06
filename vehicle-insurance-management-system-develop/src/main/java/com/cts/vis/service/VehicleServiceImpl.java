package com.cts.vis.service;

import com.cts.vis.dto.VehicleDTO;
import com.cts.vis.model.Customer;
import com.cts.vis.model.Vehicle;
import com.cts.vis.model.VehicleType;
import com.cts.vis.repository.VehicleRepository;
import com.cts.vis.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerService customerService;
    private final ClaimService claimService;

    @Override
    @Transactional
    public Vehicle addVehicle(VehicleDTO.CreateRequest dto) {
        if (vehicleRepository.existsByRegistrationNumber(dto.getRegistrationNumber())) {
            throw new IllegalArgumentException("Registration number already exists: " + dto.getRegistrationNumber());
        }

        Vehicle v = new Vehicle();
        v.setCustomer(customerService.getCurrentCustomer());
        v.setRegistrationNumber(dto.getRegistrationNumber());
        v.setMake(dto.getMake());
        v.setModel(dto.getModel());
        v.setYearOfManufacture(dto.getYearOfManufacture());
        v.setVehicleType(dto.getVehicleType());
        v.setCreatedDate(LocalDate.now());

        return vehicleRepository.save(v);
    }

    @Override
    public List<Vehicle> myVehicles() {
        return vehicleRepository.findByCustomer(customerService.getCurrentCustomer());
    }

    @Override
    public Map<Long, Boolean> getVehicleLockStatus(List<Vehicle> vehicles) {
        Map<Long, Boolean> lockMap = new HashMap<>();
        for (Vehicle v : vehicles) {
            // Business Rule: Vehicle is locked if it has an approved claim
            lockMap.put(v.getVehicleId(), claimService.hasApprovedClaimForVehicle(v.getVehicleId()));
        }
        return lockMap;
    }

    @Override
    public VehicleDTO.UpdateRequest getUpdateDto(Long id) {
        Vehicle v = getMyVehicle(id);
        VehicleDTO.UpdateRequest dto = new VehicleDTO.UpdateRequest();
        dto.setRegistrationNumber(v.getRegistrationNumber());
        dto.setMake(v.getMake());
        dto.setModel(v.getModel());
        dto.setYearOfManufacture(v.getYearOfManufacture());
        dto.setVehicleType(v.getVehicleType());
        return dto;
    }

    @Override
    public Vehicle getMyVehicle(Long id) {
        return vehicleRepository.findByVehicleIdAndCustomer(id, customerService.getCurrentCustomer())
                .orElseThrow(() -> new NotFoundException("Vehicle not found for ID: " + id));
    }

    @Override
    @Transactional
    public Vehicle updateVehicle(Long id, VehicleDTO.UpdateRequest dto) {
        // Business Rule check before update
        if (claimService.hasApprovedClaimForVehicle(id)) {
            throw new IllegalStateException("Vehicle is locked due to approved claims.");
        }

        Vehicle v = getMyVehicle(id);

        if (!v.getRegistrationNumber().equalsIgnoreCase(dto.getRegistrationNumber())) {
            if (vehicleRepository.existsByRegistrationNumber(dto.getRegistrationNumber())) {
                throw new IllegalArgumentException("Registration number already exists.");
            }
        }

        v.setRegistrationNumber(dto.getRegistrationNumber());
        v.setMake(dto.getMake());
        v.setModel(dto.getModel());
        v.setYearOfManufacture(dto.getYearOfManufacture());
        v.setVehicleType(dto.getVehicleType());

        return vehicleRepository.save(v);
    }
}