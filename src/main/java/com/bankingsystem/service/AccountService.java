package com.bankingsystem.service;

import com.bankingsystem.dto.request.AccountRequest;
import com.bankingsystem.exception.BadRequestException;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.model.Account;
import com.bankingsystem.model.AccountStatusHistory;
import com.bankingsystem.model.Customer;
import com.bankingsystem.model.enums.AccountStatus;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.AccountStatusHistoryRepository;
import com.bankingsystem.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccountStatusHistoryRepository statusHistoryRepository;

    @Transactional
    @CacheEvict(value = { "accounts", "statistics" }, allEntries = true)
    public Account createAccount(AccountRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        // Kiểm tra hạn mức theo loại khách hàng nếu có
        if (customer.getCustomerType() != null) {
            BigDecimal maxLimit = customer.getCustomerType().getMaxTransactionLimit();
            if (request.getTransactionLimit().compareTo(maxLimit) > 0) {
                throw new BadRequestException(
                        "Transaction limit exceeds maximum allowed for customer type: " + maxLimit);
            }
        }

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .customer(customer)
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .transactionLimit(request.getTransactionLimit())
                .accountOpenDate(LocalDate.now())
                .status(AccountStatus.ACTIVE)
                .build();

        return accountRepository.save(account);
    }

    @Cacheable(value = "accounts", key = "#id")
    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
    }

    public Page<Account> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    public List<Account> getAccountsByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    @Transactional
    @CacheEvict(value = { "accounts", "statistics" }, allEntries = true)
    public Account updateAccount(Long id, AccountRequest request) {
        Account account = getAccountById(id);
        account.setTransactionLimit(request.getTransactionLimit());
        if (request.getInitialBalance() != null) {
            account.setBalance(request.getInitialBalance());
        }
        return accountRepository.save(account);
    }

    @Transactional
    @CacheEvict(value = { "accounts", "statistics" }, allEntries = true)
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new ResourceNotFoundException("Account", "id", id);
        }
        accountRepository.deleteById(id);
    }

    /**
     * Thay đổi trạng thái tài khoản và lưu lịch sử
     */
    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public Account changeAccountStatus(Long id, AccountStatus newStatus, String changedBy) {
        Account account = getAccountById(id);
        AccountStatus oldStatus = account.getStatus();

        if (oldStatus == newStatus) {
            throw new BadRequestException("Account is already in status: " + newStatus);
        }

        // Lưu lịch sử thay đổi trạng thái
        AccountStatusHistory history = AccountStatusHistory.builder()
                .account(account)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .build();
        statusHistoryRepository.save(history);

        account.setStatus(newStatus);
        return accountRepository.save(account);
    }

    public List<AccountStatusHistory> getAccountStatusHistory(Long accountId) {
        return statusHistoryRepository.findByAccountIdOrderByChangedAtDesc(accountId);
    }

    /**
     * Kiểm tra hạn mức giao dịch, bao gồm cả hạn mức theo loại khách hàng
     */
    public void validateTransactionLimit(Account account, BigDecimal amount) {
        // Kiểm tra hạn mức tài khoản
        if (amount.compareTo(account.getTransactionLimit()) > 0) {
            throw new com.bankingsystem.exception.TransactionLimitExceededException(
                    "Transaction amount " + amount + " exceeds account limit " + account.getTransactionLimit());
        }

        // Kiểm tra hạn mức theo loại khách hàng
        Customer customer = account.getCustomer();
        if (customer != null && customer.getCustomerType() != null) {
            BigDecimal maxLimit = customer.getCustomerType().getMaxTransactionLimit();
            if (amount.compareTo(maxLimit) > 0) {
                throw new com.bankingsystem.exception.TransactionLimitExceededException(
                        "Transaction amount " + amount + " exceeds customer type limit " + maxLimit);
            }
        }
    }

    private String generateAccountNumber() {
        return "ACC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}