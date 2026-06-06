package com.cts.vis.controller;

import com.cts.vis.dto.VehicleDTO;
import com.cts.vis.model.Vehicle;
import com.cts.vis.model.VehicleType;
import com.cts.vis.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class VehicleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(vehicleController).build();
    }

    @Test
    public void testVehiclesListPage() throws Exception {
        List<Vehicle> vehicleList = new ArrayList<>();
        Map<Long, Boolean> lockStatus = new HashMap<>();

        when(vehicleService.myVehicles()).thenReturn(vehicleList);
        when(vehicleService.getVehicleLockStatus(anyList())).thenReturn(lockStatus);

        mockMvc.perform(get("/customer/vehicles"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/vehicles"))
                .andExpect(model().attribute("vehicles", vehicleList))
                .andExpect(model().attribute("editDisabled", lockStatus))
                .andExpect(model().attributeExists("types", "vehicle"));
    }

//    @Test
//    public void testAddVehicleSuccess() throws Exception {
//        mockMvc.perform(post("/customer/vehicles/add")
//                        .param("registrationNumber", "TN01AB1234")
//                        .param("make", "Toyota")
//                        .param("model", "Camry")
//                        .param("yearOfManufacture", "2022")
//                        .param("vehicleType", "CAR"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/customer/vehicles?added=true"));
//
//        // Verify service called with the DTO
//        verify(vehicleService, times(1)).addVehicle(any(VehicleDTO.CreateRequest.class));
//    }

    @Test
    public void testAddVehicle_ValidationFailure() throws Exception {
        // 1. Arrange: Prepare data for the refreshIndex helper
        when(vehicleService.myVehicles()).thenReturn(new ArrayList<>());
        when(vehicleService.getVehicleLockStatus(anyList())).thenReturn(new HashMap<>());

        // 2. Act: Send invalid data (registrationNumber too short)
        mockMvc.perform(post("/customer/vehicles/add")
                        .param("registrationNumber", "ABC")
                        .param("make", "Toyota"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/vehicles"))
                .andExpect(model().hasErrors());

        // 3. Assert: Verify the "Write" operation was never called
        verify(vehicleService, never()).addVehicle(any(VehicleDTO.CreateRequest.class));

        // 4. Verify the "Read" operations were called to reload the page
        verify(vehicleService, atLeastOnce()).myVehicles();
        verify(vehicleService, atLeastOnce()).getVehicleLockStatus(anyList());
    }

    @Test
    public void testEditPageView() throws Exception {
        Long vehicleId = 1L;
        VehicleDTO.UpdateRequest mockDto = new VehicleDTO.UpdateRequest();

        when(vehicleService.getUpdateDto(vehicleId)).thenReturn(mockDto);

        mockMvc.perform(get("/customer/vehicles/" + vehicleId + "/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/vehicle-edit"))
                .andExpect(model().attribute("id", vehicleId))
                .andExpect(model().attribute("vehicle", mockDto))
                .andExpect(model().attributeExists("types"));
    }

    @Test
    public void testUpdateVehicleSuccess() throws Exception {
        Long vehicleId = 1L;

        mockMvc.perform(post("/customer/vehicles/" + vehicleId + "/edit")
                        .param("registrationNumber", "NEWREG123")
                        .param("make", "Ford")
                        .param("model", "Mustang")
                        .param("yearOfManufacture", "2023")
                        .param("vehicleType", "CAR"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/vehicles?updated=true"));

        verify(vehicleService).updateVehicle(eq(vehicleId), any(VehicleDTO.UpdateRequest.class));
    }

    @Test
    public void testUpdate_BusinessLogicError() throws Exception {
        // If the vehicle is locked (due to claims), service throws exception
        doThrow(new IllegalStateException("Vehicle locked"))
                .when(vehicleService).updateVehicle(anyLong(), any());

        try {
            mockMvc.perform(post("/customer/vehicles/1/edit")
                    .param("registrationNumber", "VALIDREG1"));
        } catch (Exception e) {
            // Standalone setup bubbles the exception to the test
            assert(e.getCause() instanceof IllegalStateException);
        }
    }
}