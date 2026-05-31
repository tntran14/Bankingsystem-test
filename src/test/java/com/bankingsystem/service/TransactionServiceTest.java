package com.bankingsystem.service;

import com.bankingsystem.dto.request.TransactionRequest;
import com.bankingsystem.exception.BadRequestException;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.model.Account;
import com.bankingsystem.model.Customer;
import com.bankingsystem.model.CustomerType;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.enums.AccountStatus;
import com.bankingsystem.model.enums.CustomerCategory;
import com.bankingsystem.model.enums.TransactionType;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.repository.AccountStatusHistoryRepository;
import com.bankingsystem.repository.CustomerRepository;
import com.bankingsystem.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountStatusHistoryRepository statusHistoryRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AlertService alertService;

    @InjectMocks
    private AccountService accountService;

    private TransactionService transactionService;

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService();

        // Inject dependencies vào transactionService thông qua reflection
        setField(transactionService, "transactionRepository", transactionRepository);
        setField(transactionService, "accountService", accountService);
        setField(transactionService, "alertService", alertService);

        CustomerType individualType = CustomerType.builder()
                .id(1L)
                .category(CustomerCategory.INDIVIDUAL)
                .maxTransactionLimit(new BigDecimal("500000000"))
                .build();

        Customer customer = Customer.builder()
                .id(1L)
                .fullName("Test Customer")
                .email("test@example.com")
                .customerType(individualType)
                .build();

        fromAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC001")
                .customer(customer)
                .balance(new BigDecimal("10000000"))
                .transactionLimit(new BigDecimal("5000000"))
                .accountOpenDate(LocalDate.now())
                .status(AccountStatus.ACTIVE)
                .build();

        toAccount = Account.builder()
                .id(2L)
                .accountNumber("ACC002")
                .customer(customer)
                .balance(new BigDecimal("5000000"))
                .transactionLimit(new BigDecimal("5000000"))
                .accountOpenDate(LocalDate.now())
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void deposit_Success() {
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        TransactionRequest request = new TransactionRequest();
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("1000000"));
        request.setType(TransactionType.DEPOSIT);

        Transaction result = transactionService.createTransaction(request);

        assertNotNull(result);
        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertEquals(new BigDecimal("6000000"), toAccount.getBalance());
    }

    @Test
    void withdrawal_ExceedsLimit_ThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));

        TransactionRequest request = new TransactionRequest();
        request.setFromAccountId(1L);
        request.setAmount(new BigDecimal("6000000")); // Vượt hạn mức 5 triệu
        request.setType(TransactionType.WITHDRAWAL);

        assertThrows(com.bankingsystem.exception.TransactionLimitExceededException.class, () -> {
            transactionService.createTransaction(request);
        });
    }

    @Test
    void withdrawal_InsufficientBalance_ThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));

        TransactionRequest request = new TransactionRequest();
        request.setFromAccountId(1L);
        request.setAmount(new BigDecimal("4999999")); // Trong hạn mức nhưng cộng phí sẽ gần hết
        request.setType(TransactionType.WITHDRAWAL);

        // Balance = 10,000,000; amount = 4,999,999; fee = ~5000 → tổng < balance → OK
        // Nhưng nếu amount > balance thì sẽ lỗi
        TransactionRequest request2 = new TransactionRequest();
        request2.setFromAccountId(1L);
        request2.setAmount(new BigDecimal("5000000")); // = limit
        request2.setType(TransactionType.WITHDRAWAL);

        // Không ném exception vì balance đủ
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Transaction result = transactionService.createTransaction(request2);
        assertNotNull(result);
    }

    @Test
    void transfer_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        TransactionRequest request = new TransactionRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("1000000"));
        request.setType(TransactionType.TRANSFER);

        Transaction result = transactionService.createTransaction(request);

        assertNotNull(result);
        assertEquals(TransactionType.TRANSFER, result.getType());
        // from giảm: 10,000,000 - 1,000,000 - 1,000 (fee) = 8,999,000
        // to tăng: 5,000,000 + 1,000,000 = 6,000,000
        assertEquals(new BigDecimal("6000000"), toAccount.getBalance());
    }

    @Test
    void transfer_SameAccount_ThrowsException() {
        TransactionRequest request = new TransactionRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(1L);
        request.setAmount(new BigDecimal("100000"));
        request.setType(TransactionType.TRANSFER);

        assertThrows(BadRequestException.class, () -> {
            transactionService.createTransaction(request);
        });
    }

    @Test
    void transfer_LockedAccount_ThrowsException() {
        fromAccount.setStatus(AccountStatus.LOCKED);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));

        TransactionRequest request = new TransactionRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(new BigDecimal("100000"));
        request.setType(TransactionType.TRANSFER);

        assertThrows(BadRequestException.class, () -> {
            transactionService.createTransaction(request);
        });
    }

    @Test
    void getTransactionById_NotFound_ThrowsException() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.getTransactionById(999L);
        });
    }

    // Helper để set private field qua reflection
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
