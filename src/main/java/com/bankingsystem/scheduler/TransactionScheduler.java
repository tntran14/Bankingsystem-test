package com.bankingsystem.scheduler;

import com.bankingsystem.dto.request.TransactionRequest;
import com.bankingsystem.model.ScheduledTransaction;
import com.bankingsystem.model.enums.TransactionType;
import com.bankingsystem.repository.ScheduledTransactionRepository;
import com.bankingsystem.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Xử lý giao dịch định kỳ
 * Chạy mỗi phút để kiểm tra và thực hiện các giao dịch đã đến hạn.
 */
@Component
public class TransactionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TransactionScheduler.class);

    @Autowired
    private ScheduledTransactionRepository scheduledTransactionRepository;
    @Autowired
    private TransactionService transactionService;

    @Scheduled(fixedRate = 60000) // Chạy mỗi 60 giây
    @Transactional
    public void processScheduledTransactions() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledTransaction> dueTransactions = scheduledTransactionRepository
                .findByActiveTrueAndNextExecutionBefore(now);

        for (ScheduledTransaction scheduled : dueTransactions) {
            try {
                TransactionRequest request = new TransactionRequest();
                request.setAmount(scheduled.getAmount());
                request.setDescription("Scheduled: " + scheduled.getDescription());

                if (scheduled.getFromAccount() != null && scheduled.getToAccount() != null) {
                    request.setFromAccountId(scheduled.getFromAccount().getId());
                    request.setToAccountId(scheduled.getToAccount().getId());
                    request.setType(TransactionType.TRANSFER);
                } else if (scheduled.getToAccount() != null) {
                    request.setToAccountId(scheduled.getToAccount().getId());
                    request.setType(TransactionType.DEPOSIT);
                } else if (scheduled.getFromAccount() != null) {
                    request.setFromAccountId(scheduled.getFromAccount().getId());
                    request.setType(TransactionType.WITHDRAWAL);
                }

                transactionService.createTransaction(request);

                // Tính nextExecution tiếp theo (đơn giản: cộng thêm 30 ngày cho monthly)
                scheduled.setNextExecution(calculateNextExecution(scheduled));

                logger.info("Executed scheduled transaction {} successfully", scheduled.getId());
            } catch (Exception e) {
                logger.error("Failed to execute scheduled transaction {}: {}", scheduled.getId(), e.getMessage());
                // Không deactivate — sẽ retry lần sau
            }
        }

        if (!dueTransactions.isEmpty()) {
            scheduledTransactionRepository.saveAll(dueTransactions);
        }
    }

    private LocalDateTime calculateNextExecution(ScheduledTransaction scheduled) {
        String cron = scheduled.getCronExpression().trim().toLowerCase();

        if (cron.contains("daily")) {
            return scheduled.getNextExecution().plusDays(1);
        } else if (cron.contains("weekly")) {
            return scheduled.getNextExecution().plusWeeks(1);
        } else if (cron.contains("monthly")) {
            return scheduled.getNextExecution().plusMonths(1);
        } else if (cron.contains("yearly")) {
            return scheduled.getNextExecution().plusYears(1);
        }
        // Mặc định: monthly
        return scheduled.getNextExecution().plusMonths(1);
    }
}
