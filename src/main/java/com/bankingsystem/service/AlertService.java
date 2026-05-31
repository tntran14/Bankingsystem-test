package com.bankingsystem.service;

import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.model.Alert;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.model.enums.AlertStatus;
import com.bankingsystem.model.enums.AlertType;
import com.bankingsystem.repository.AlertRepository;
import com.bankingsystem.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service phát hiện giao dịch bất thường
 * Tiêu chí: số tiền vượt ngưỡng, giao dịch quá nhanh liên tiếp.
 */
@Service
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("50000000"); // 50 triệu
    private static final int RAPID_TRANSACTION_COUNT = 5; // 5 giao dịch
    private static final int RAPID_TRANSACTION_MINUTES = 10; // trong 10 phút

    @Autowired
    private AlertRepository alertRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Phát hiện giao dịch bất thường sau khi giao dịch được thực hiện.
     */
    @Transactional
    public void detectAnomalies(Transaction transaction) {
        // Kiểm tra số tiền vượt ngưỡng
        if (transaction.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
            Alert alert = Alert.builder()
                    .transaction(transaction)
                    .alertType(AlertType.HIGH_AMOUNT)
                    .description("Transaction amount " + transaction.getAmount()
                            + " exceeds threshold " + HIGH_AMOUNT_THRESHOLD)
                    .status(AlertStatus.PENDING)
                    .build();
            alertRepository.save(alert);
            logger.warn("HIGH_AMOUNT alert created for transaction {}", transaction.getId());
        }

        // Kiểm tra giao dịch liên tiếp quá nhanh
        if (transaction.getFromAccount() != null) {
            LocalDateTime from = LocalDateTime.now().minusMinutes(RAPID_TRANSACTION_MINUTES);
            List<Transaction> recentTxns = transactionRepository.findRecentTransactionsByAccount(
                    transaction.getFromAccount().getId(), from, LocalDateTime.now());

            if (recentTxns.size() >= RAPID_TRANSACTION_COUNT) {
                Alert alert = Alert.builder()
                        .transaction(transaction)
                        .alertType(AlertType.RAPID_TRANSACTIONS)
                        .description(recentTxns.size() + " transactions in last "
                                + RAPID_TRANSACTION_MINUTES + " minutes from account "
                                + transaction.getFromAccount().getAccountNumber())
                        .status(AlertStatus.PENDING)
                        .build();
                alertRepository.save(alert);
                logger.warn("RAPID_TRANSACTIONS alert created for account {}",
                        transaction.getFromAccount().getAccountNumber());
            }
        }
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public List<Alert> getAlertsByStatus(AlertStatus status) {
        return alertRepository.findByStatus(status);
    }

    public Alert getAlertById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", id));
    }

    @Transactional
    public Alert updateAlertStatus(Long id, AlertStatus newStatus) {
        Alert alert = getAlertById(id);
        alert.setStatus(newStatus);
        return alertRepository.save(alert);
    }
}
