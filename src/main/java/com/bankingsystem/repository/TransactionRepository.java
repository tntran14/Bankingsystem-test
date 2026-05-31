package com.bankingsystem.repository;

import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Page<Transaction> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId, Pageable pageable);

    List<Transaction> findByFromAccountId(Long accountId);

    /** Đếm giao dịch theo loại */
    @Query("SELECT t.type, COUNT(t) FROM Transaction t GROUP BY t.type")
    List<Object[]> countTransactionsByType();

    /** Báo cáo giao dịch trong khoảng thời gian */
    @Query("SELECT COUNT(t), SUM(t.amount), AVG(t.amount), MAX(t.amount), MIN(t.amount), SUM(t.transactionFee) " +
           "FROM Transaction t WHERE t.transactionDate BETWEEN :from AND :to")
    List<Object[]> getTransactionReport(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Phát hiện giao dịch số tiền lớn bất thường */
    @Query("SELECT t FROM Transaction t WHERE t.amount > :threshold AND t.transactionDate BETWEEN :from AND :to")
    List<Transaction> findHighAmountTransactions(@Param("threshold") BigDecimal threshold,
                                                  @Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to);

    /** Phát hiện giao dịch liên tiếp quá nhanh từ cùng 1 tài khoản */
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.id = :accountId " +
           "AND t.transactionDate BETWEEN :from AND :to ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactionsByAccount(@Param("accountId") Long accountId,
                                                       @Param("from") LocalDateTime from,
                                                       @Param("to") LocalDateTime to);
}
