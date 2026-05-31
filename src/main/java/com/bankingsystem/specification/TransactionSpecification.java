package com.bankingsystem.specification;

import com.bankingsystem.dto.request.TransactionSearchRequest;
import com.bankingsystem.model.Transaction;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification cho tìm kiếm giao dịch nâng cao với nhiều bộ lọc
 * Hỗ trợ lọc theo: số tiền (min/max), loại giao dịch, ngày, địa điểm, tài
 * khoản.
 */
public final class TransactionSpecification {

    private TransactionSpecification() {
    }

    public static Specification<Transaction> withFilters(TransactionSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), request.getMinAmount()));
            }
            if (request.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), request.getMaxAmount()));
            }
            if (request.getType() != null) {
                predicates.add(cb.equal(root.get("type"), request.getType()));
            }
            if (request.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), request.getFromDate()));
            }
            if (request.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), request.getToDate()));
            }
            if (request.getLocation() != null && !request.getLocation().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")),
                        "%" + request.getLocation().toLowerCase() + "%"));
            }
            if (request.getAccountId() != null) {
                predicates.add(cb.or(
                        cb.equal(root.get("fromAccount").get("id"), request.getAccountId()),
                        cb.equal(root.get("toAccount").get("id"), request.getAccountId())));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
