package com.cts.vis.service;

import com.cts.vis.dto.CustomerDTO;
import com.cts.vis.model.Customer;
public interface AuthService {
    // Cleaner signature using the DTO
    Customer registerCustomer(CustomerDTO.RegisterRequest dto);
}