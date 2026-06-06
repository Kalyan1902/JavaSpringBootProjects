package com.cts.vis.repository;

import com.cts.vis.model.Customer;
import com.cts.vis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUser(User user);

    List<Customer> findByCreatedDateBetween(LocalDate start, LocalDate end);
}