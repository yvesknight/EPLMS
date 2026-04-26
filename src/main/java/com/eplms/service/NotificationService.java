package com.eplms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void notifyLeaveStatusChange(String employeeName, String status, String comment) {
        log.info("[NOTIFICATION] Leave Update → Employee: '{}' | Status: {} | Comment: {}",
                employeeName, status, comment);
    }

    public void notifyReviewSubmitted(String employeeName, int year) {
        log.info("[NOTIFICATION] Self-review submitted → Employee: '{}' | Year: {}", employeeName, year);
    }

    public void notifyManagerReviewCompleted(String employeeName, Double finalRating) {
        log.info("[NOTIFICATION] Manager review completed → Employee: '{}' | Final Rating: {}",
                employeeName, finalRating);
    }

    public void notifyLeaveEscalated(String employeeName, int days) {
        log.info("[NOTIFICATION] Leave escalated to HR → Employee: '{}' | Days: {}", employeeName, days);
    }
}
