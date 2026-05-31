package com.bankingsystem.controller;

import com.bankingsystem.dto.request.AccountRequest;
import com.bankingsystem.dto.response.ApiResponse;
import com.bankingsystem.model.Account;
import com.bankingsystem.model.AccountStatusHistory;
import com.bankingsystem.model.enums.AccountStatus;
import com.bankingsystem.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        Account account = accountService.createAccount(request);
        return ResponseEntity.ok(ApiResponse.success("Account created successfully", account));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Account>> getAllAccounts(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(accountService.getAllAccounts(pageable));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Account>> getAccountsByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateAccount(@PathVariable Long id,
                                                      @Valid @RequestBody AccountRequest request) {
        Account updated = accountService.updateAccount(id, request);
        return ResponseEntity.ok(ApiResponse.success("Account updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
    }

    /**
     * Thay đổi trạng thái tài khoản (ACTIVE, LOCKED, CLOSED) và lưu lịch sử.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> changeStatus(@PathVariable Long id,
                                                     @RequestParam AccountStatus status,
                                                     Authentication authentication) {
        Account updated = accountService.changeAccountStatus(id, status, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Account status changed to " + status, updated));
    }

    /**
     * Lấy lịch sử thay đổi trạng thái tài khoản.
     */
    @GetMapping("/{id}/status-history")
    public ResponseEntity<List<AccountStatusHistory>> getStatusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountStatusHistory(id));
    }
}