package com.bankingsystem.repository;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);

    List<Account> findByStatus(AccountStatus status);

    /** Đếm tài khoản có số dư >= threshold */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.balance >= :threshold")
    long countByBalanceGreaterThanOrEqual(@Param("threshold") BigDecimal threshold);

    /** Đếm tài khoản có số dư trong khoảng [low, high) */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.balance >= :low AND a.balance < :high")
    long countByBalanceBetween(@Param("low") BigDecimal low, @Param("high") BigDecimal high);

    /** Đếm tài khoản có số dư < threshold */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.balance < :threshold")
    long countByBalanceLessThan(@Param("threshold") BigDecimal threshold);
}