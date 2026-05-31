package com.bankingsystem.service;

import com.bankingsystem.dto.request.AccountRequest;
import com.bankingsystem.exception.BadRequestException;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.exception.TransactionLimitExceededException;
import com.bankingsystem.model.Account;
import com.bankingsystem.model.Customer;
import com.bankingsystem.model.CustomerType;
import com.bankingsystem.model.enums.AccountStatus;
import com.bankingsystem.model.enums.CustomerCategory;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.AccountStatusHistoryRepository;
import com.bankingsystem.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AccountStatusHistoryRepository statusHistoryRepository;

    @InjectMocks
    private AccountService accountService;

    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {
        CustomerType type = CustomerType.builder()
                .id(1L)
                .category(CustomerCategory.INDIVIDUAL)
                .maxTransactionLimit(new BigDecimal("500000000"))
                .build();

        customer = Customer.builder()
                .id(1L)
                .fullName("Nguyen Van A")
                .email("a@test.com")
                .phone("0901234567")
                .address("Ha Noi")
                .customerType(type)
                .build();

        account = Account.builder()
                .id(1L)
                .accountNumber("ACC001")
                .customer(customer)
                .balance(new BigDecimal("10000000"))
                .transactionLimit(new BigDecimal("5000000"))
                .accountOpenDate(LocalDate.now())
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void createAccount_Success() {
        AccountRequest request = new AccountRequest();
        request.setCustomerId(1L);
        request.setTransactionLimit(new BigDecimal("5000000"));
        request.setInitialBalance(new BigDecimal("1000000"));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        Account result = accountService.createAccount(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("1000000"), result.getBalance());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_ExceedsCustomerTypeLimit_ThrowsException() {
        AccountRequest request = new AccountRequest();
        request.setCustomerId(1L);
        request.setTransactionLimit(new BigDecimal("600000000")); // Vượt hạn mức cá nhân 500 triệu

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(BadRequestException.class, () -> {
            accountService.createAccount(request);
        });
    }

    @Test
    void getAccountById_NotFound_ThrowsException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            accountService.getAccountById(999L);
        });
    }

    @Test
    void changeAccountStatus_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(statusHistoryRepository.save(any())).thenReturn(null);

        Account result = accountService.changeAccountStatus(1L, AccountStatus.LOCKED, "admin");

        assertEquals(AccountStatus.LOCKED, result.getStatus());
        verify(statusHistoryRepository).save(any());
    }

    @Test
    void changeAccountStatus_SameStatus_ThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(BadRequestException.class, () -> {
            accountService.changeAccountStatus(1L, AccountStatus.ACTIVE, "admin");
        });
    }

    @Test
    void validateTransactionLimit_ExceedsLimit_ThrowsException() {
        assertThrows(TransactionLimitExceededException.class, () -> {
            accountService.validateTransactionLimit(account, new BigDecimal("6000000"));
        });
    }

    @Test
    void validateTransactionLimit_WithinLimit_NoException() {
        assertDoesNotThrow(() -> {
            accountService.validateTransactionLimit(account, new BigDecimal("3000000"));
        });
    }
}
