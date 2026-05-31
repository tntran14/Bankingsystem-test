package com.bankingsystem.controller;

import com.bankingsystem.dto.response.ApiResponse;
import com.bankingsystem.model.Alert;
import com.bankingsystem.model.enums.AlertStatus;
import com.bankingsystem.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý cảnh báo giao dịch bất thường
 */
@RestController
@RequestMapping("/api/alerts")
@PreAuthorize("hasRole('ADMIN')")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Alert>> getAlertsByStatus(@PathVariable AlertStatus status) {
        return ResponseEntity.ok(alertService.getAlertsByStatus(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlert(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    /**
     * Cập nhật trạng thái xử lý cảnh báo (PENDING → REVIEWED → RESOLVED).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateAlertStatus(@PathVariable Long id,
            @RequestParam AlertStatus status) {
        Alert updated = alertService.updateAlertStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Alert status updated to " + status, updated));
    }
}
