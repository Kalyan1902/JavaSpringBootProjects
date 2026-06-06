package com.cts.vis.service;

import com.cts.vis.dto.CustomerDTO;
import com.cts.vis.model.Customer;
import com.cts.vis.model.User;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.UserRepository;
import com.cts.vis.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        String name = auth.getName();
        if (name == null || name.trim().isEmpty() || "anonymousUser".equalsIgnoreCase(name)) {
            return null;
        }
        return name;
    }

    @Override
    public Customer getCurrentCustomer() {
        String email = getCurrentUserEmail();
        if (email == null) {
            throw new IllegalStateException("No authenticated customer found");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        return customerRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Customer profile not found for: " + email));
    }

    @Override
    public CustomerDTO.ProfileUpdateRequest getProfileUpdateDto() {
        Customer c = getCurrentCustomer();
        CustomerDTO.ProfileUpdateRequest dto = new CustomerDTO.ProfileUpdateRequest();
        dto.setName(c.getName());
        dto.setPhone(c.getPhone());
        dto.setAddress(c.getAddress());
        return dto;
    }

    @Override
    @Transactional
    public void updateProfile(CustomerDTO.ProfileUpdateRequest dto) {
        Customer c = getCurrentCustomer();

        // Basic validation can happen here
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        c.setName(dto.getName());
        c.setPhone(dto.getPhone());
        c.setAddress(dto.getAddress());

        customerRepository.save(c);
    }
}