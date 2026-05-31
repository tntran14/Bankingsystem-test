package com.bankingsystem.repository;

import com.bankingsystem.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Customer> findByFullNameContainingIgnoreCase(String name);

    /** Đếm khách hàng theo địa điểm (address) */
    @Query("SELECT c.address, COUNT(c) FROM Customer c GROUP BY c.address")
    List<Object[]> countCustomersByLocation();
}
