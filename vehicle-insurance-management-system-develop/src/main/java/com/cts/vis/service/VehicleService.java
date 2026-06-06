
package com.cts.vis.service;

import com.cts.vis.dto.VehicleDTO;
import com.cts.vis.model.Vehicle;
import com.cts.vis.model.VehicleType;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;

public interface VehicleService {
    Vehicle addVehicle(VehicleDTO.CreateRequest dto);
    List<Vehicle> myVehicles();
    Vehicle getMyVehicle(Long id);
    Vehicle updateVehicle(Long id, VehicleDTO.UpdateRequest dto);

    // Logic extracted from Controller
    Map<Long, Boolean> getVehicleLockStatus(List<Vehicle> vehicles);
    VehicleDTO.UpdateRequest getUpdateDto(Long id);
}