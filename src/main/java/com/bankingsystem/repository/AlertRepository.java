package com.bankingsystem.repository;

import com.bankingsystem.model.Alert;
import com.bankingsystem.model.enums.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByStatus(AlertStatus status);

    List<Alert> findByTransactionId(Long transactionId);
}
