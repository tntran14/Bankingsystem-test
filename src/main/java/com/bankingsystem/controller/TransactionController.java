package com.bankingsystem.controller;

import com.bankingsystem.dto.request.ScheduledTransactionRequest;
import com.bankingsystem.dto.request.TransactionRequest;
import com.bankingsystem.dto.request.TransactionSearchRequest;
import com.bankingsystem.dto.response.ApiResponse;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.model.Account;
import com.bankingsystem.model.ScheduledTransaction;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.repository.ScheduledTransactionRepository;
import com.bankingsystem.service.AccountService;
import com.bankingsystem.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ScheduledTransactionRepository scheduledTransactionRepository;

    /**
     * Thực hiện giao dịch (nạp tiền, rút tiền, chuyển khoản).
     * Tự động kiểm tra hạn mức và tính phí.
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        Transaction tx = transactionService.createTransaction(request);
        return ResponseEntity.ok(ApiResponse.success("Transaction completed successfully", tx));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    /**
     * Tìm kiếm giao dịch nâng cao với bộ lọc (số tiền, loại, ngày, địa điểm).
     * Hỗ trợ phân trang và sắp xếp
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Transaction>> searchTransactions(
            TransactionSearchRequest searchRequest,
            @PageableDefault(size = 10, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(transactionService.searchTransactions(searchRequest, pageable));
    }

    /**
     * Lấy lịch sử giao dịch của một tài khoản (phân trang).
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<Transaction>> getTransactionsByAccount(
            @PathVariable Long accountId,
            @PageableDefault(size = 10, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountId, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully"));
    }

    /**
     * Tạo giao dịch định kỳ
     */
    @PostMapping("/scheduled")
    public ResponseEntity<ApiResponse> createScheduledTransaction(
            @Valid @RequestBody ScheduledTransactionRequest request) {

        ScheduledTransaction scheduled = ScheduledTransaction.builder()
                .amount(request.getAmount())
                .cronExpression(request.getCronExpression())
                .description(request.getDescription())
                .nextExecution(request.getNextExecution())
                .active(true)
                .build();

        if (request.getFromAccountId() != null) {
            Account from = accountService.getAccountById(request.getFromAccountId());
            scheduled.setFromAccount(from);
        }
        if (request.getToAccountId() != null) {
            Account to = accountService.getAccountById(request.getToAccountId());
            scheduled.setToAccount(to);
        }

        ScheduledTransaction saved = scheduledTransactionRepository.save(scheduled);
        return ResponseEntity.ok(ApiResponse.success("Scheduled transaction created", saved));
    }

    @GetMapping("/scheduled")
    public ResponseEntity<?> getScheduledTransactions() {
        return ResponseEntity.ok(scheduledTransactionRepository.findAll());
    }

    @DeleteMapping("/scheduled/{id}")
    public ResponseEntity<ApiResponse> cancelScheduledTransaction(@PathVariable Long id) {
        ScheduledTransaction scheduled = scheduledTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduledTransaction", "id", id));
        scheduled.setActive(false);
        scheduledTransactionRepository.save(scheduled);
        return ResponseEntity.ok(ApiResponse.success("Scheduled transaction cancelled"));
    }
}
