package com.bankingsystem.repository;

import com.bankingsystem.model.CustomerType;
import com.bankingsystem.model.enums.CustomerCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerTypeRepository extends JpaRepository<CustomerType, Long> {

    Optional<CustomerType> findByCategory(CustomerCategory category);
}
