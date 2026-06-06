package com.cts.vis.service;

import com.cts.vis.dto.CustomerDTO;
import com.cts.vis.model.Customer;
import com.cts.vis.model.User;
import com.cts.vis.model.UserRole;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.UserRepository;
import com.cts.vis.exception.BadRequestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Customer registerCustomer(CustomerDTO.RegisterRequest dto) {
        // 1. Business Logic: Check uniqueness
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already registered: " + dto.getEmail());
        }

        // 2. Create User Entity (Security/Auth data)
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.ROLE_CUSTOMER);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        // 3. Create Customer Entity (Profile data)
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setUser(savedUser);

        return customerRepository.save(customer);
    }
}