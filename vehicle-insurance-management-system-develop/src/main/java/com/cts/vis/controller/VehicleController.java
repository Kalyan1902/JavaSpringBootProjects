package com.cts.vis.controller;
import com.cts.vis.dto.VehicleDTO;
import com.cts.vis.model.Vehicle;
import com.cts.vis.model.VehicleType;
import com.cts.vis.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public String vehicles(Model model) {
        // We initialize the form DTO and load the list
        return refreshIndex(model, new VehicleDTO.CreateRequest());
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("vehicle") VehicleDTO.CreateRequest dto,
                      BindingResult result, Model model) {
        if (result.hasErrors()) {
            return refreshIndex(model, dto);
        }
        try {
            vehicleService.addVehicle(dto);
        } catch (IllegalArgumentException ex) {
            result.rejectValue("registrationNumber", "registration.exists", ex.getMessage());
        }
        model.addAttribute("showVehicleForm",true);
        return refreshIndex(model, dto);
    }
        // No try-catch here!
        // If registration exists, service throws IllegalArgumentException -> GlobalHandler catches it.
//        vehicleService.addVehicle(dto);
//        return "redirect:/customer/vehicles?added=true";
//    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        // If vehicle doesn't exist or is locked, service throws exception -> GlobalHandler catches it.
        model.addAttribute("id", id);
        model.addAttribute("vehicle", vehicleService.getUpdateDto(id));
        model.addAttribute("types", VehicleType.values());
        return "customer/vehicle-edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("vehicle") VehicleDTO.UpdateRequest dto,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("id", id);
            model.addAttribute("types", VehicleType.values());
            return "customer/vehicle-edit";
        }

        // Business logic (like checking for approved claims) happens in the service
        vehicleService.updateVehicle(id, dto);
        return "redirect:/customer/vehicles?updated=true";
    }

    private String refreshIndex(Model model, VehicleDTO.CreateRequest formDto) {
        List<Vehicle> vehicles = vehicleService.myVehicles();
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("editDisabled", vehicleService.getVehicleLockStatus(vehicles));
        model.addAttribute("types", VehicleType.values());
        model.addAttribute("vehicle", formDto);
        return "customer/vehicles";
    }
}