package com.bankingsystem.repository;

import com.bankingsystem.model.ScheduledTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransaction, Long> {

    /** Tìm tất cả giao dịch định kỳ đang active và đã đến thời gian thực hiện */
    List<ScheduledTransaction> findByActiveTrueAndNextExecutionBefore(LocalDateTime dateTime);

    List<ScheduledTransaction> findByFromAccountId(Long accountId);
}
