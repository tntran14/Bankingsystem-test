package com.bankingsystem.service;

import com.bankingsystem.dto.request.TransactionRequest;
import com.bankingsystem.dto.request.TransactionSearchRequest;
import com.bankingsystem.exception.BadRequestException;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.model.Account;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.enums.AccountStatus;
import com.bankingsystem.model.enums.TransactionType;
import com.bankingsystem.repository.TransactionRepository;
import com.bankingsystem.specification.TransactionSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
public class TransactionService {

    private static final BigDecimal DEFAULT_FEE_RATE = new BigDecimal("0.001"); // 0.1% phí giao dịch

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AlertService alertService;

    /**
     * Thực hiện giao dịch: NẠP TIỀN, RÚT TIỀN, CHUYỂN KHOẢN.
     * Kiểm tra hạn mức, tính phí, và phát hiện bất thường.
     */
    @Transactional
    @CacheEvict(value = { "accounts", "transactions", "statistics" }, allEntries = true)
    public Transaction createTransaction(TransactionRequest request) {
        BigDecimal amount = request.getAmount();
        BigDecimal fee = amount.multiply(DEFAULT_FEE_RATE);

        Transaction transaction;

        switch (request.getType()) {
            case DEPOSIT:
                transaction = processDeposit(request, fee);
                break;
            case WITHDRAWAL:
                transaction = processWithdrawal(request, fee);
                break;
            case TRANSFER:
                transaction = processTransfer(request, fee);
                break;
            default:
                throw new BadRequestException("Invalid transaction type: " + request.getType());
        }

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Phát hiện giao dịch bất thường
        alertService.detectAnomalies(savedTransaction);

        return savedTransaction;
    }

    private Transaction processDeposit(TransactionRequest request, BigDecimal fee) {
        if (request.getToAccountId() == null) {
            throw new BadRequestException("Target account is required for deposit");
        }
        Account toAccount = accountService.getAccountById(request.getToAccountId());
        validateAccountActive(toAccount);

        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        return Transaction.builder()
                .toAccount(toAccount)
                .amount(request.getAmount())
                .transactionFee(fee)
                .type(TransactionType.DEPOSIT)
                .location(request.getLocation())
                .description(request.getDescription())
                .transactionDate(LocalDateTime.now())
                .build();
    }

    private Transaction processWithdrawal(TransactionRequest request, BigDecimal fee) {
        if (request.getFromAccountId() == null) {
            throw new BadRequestException("Source account is required for withdrawal");
        }
        Account fromAccount = accountService.getAccountById(request.getFromAccountId());
        validateAccountActive(fromAccount);

        // Kiểm tra hạn mức
        accountService.validateTransactionLimit(fromAccount, request.getAmount());

        BigDecimal totalDebit = request.getAmount().add(fee);
        if (fromAccount.getBalance().compareTo(totalDebit) < 0) {
            throw new BadRequestException("Insufficient balance. Required: " + totalDebit
                    + ", Available: " + fromAccount.getBalance());
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(totalDebit));

        return Transaction.builder()
                .fromAccount(fromAccount)
                .amount(request.getAmount())
                .transactionFee(fee)
                .type(TransactionType.WITHDRAWAL)
                .location(request.getLocation())
                .description(request.getDescription())
                .transactionDate(LocalDateTime.now())
                .build();
    }

    private Transaction processTransfer(TransactionRequest request, BigDecimal fee) {
        if (request.getFromAccountId() == null || request.getToAccountId() == null) {
            throw new BadRequestException("Both source and target accounts are required for transfer");
        }
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new BadRequestException("Cannot transfer to the same account");
        }

        Account fromAccount = accountService.getAccountById(request.getFromAccountId());
        Account toAccount = accountService.getAccountById(request.getToAccountId());
        validateAccountActive(fromAccount);
        validateAccountActive(toAccount);

        // Kiểm tra hạn mức
        accountService.validateTransactionLimit(fromAccount, request.getAmount());

        BigDecimal totalDebit = request.getAmount().add(fee);
        if (fromAccount.getBalance().compareTo(totalDebit) < 0) {
            throw new BadRequestException("Insufficient balance. Required: " + totalDebit
                    + ", Available: " + fromAccount.getBalance());
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(totalDebit));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        return Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .transactionFee(fee)
                .type(TransactionType.TRANSFER)
                .location(request.getLocation())
                .description(request.getDescription())
                .transactionDate(LocalDateTime.now())
                .build();
    }

    private void validateAccountActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account " + account.getAccountNumber() + " is not active");
        }
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
    }

    /**
     * Tìm kiếm giao dịch nâng cao với bộ lọc, phân trang và sắp xếp
     */
    public Page<Transaction> searchTransactions(TransactionSearchRequest searchRequest, Pageable pageable) {
        return transactionRepository.findAll(
                TransactionSpecification.withFilters(searchRequest), pageable);
    }

    public Page<Transaction> getTransactionsByAccount(Long accountId, Pageable pageable) {
        return transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId, pageable);
    }

    @Transactional
    @CacheEvict(value = { "transactions", "statistics" }, allEntries = true)
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction", "id", id);
        }
        transactionRepository.deleteById(id);
    }

    /**
     * Xử lý giao dịch bất đồng bộ
     */
    @Async("taskExecutor")
    public CompletableFuture<Transaction> createTransactionAsync(TransactionRequest request) {
        Transaction transaction = createTransaction(request);
        return CompletableFuture.completedFuture(transaction);
    }
}
