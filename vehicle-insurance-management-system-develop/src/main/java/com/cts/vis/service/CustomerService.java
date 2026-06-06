package com.cts.vis.service;

import com.cts.vis.dto.CustomerDTO;
import com.cts.vis.model.Customer;

public interface CustomerService {
    Customer getCurrentCustomer();
    String getCurrentUserEmail();
    void updateProfile(CustomerDTO.ProfileUpdateRequest dto);

    // New helper to prepare the form
    CustomerDTO.ProfileUpdateRequest getProfileUpdateDto();
}