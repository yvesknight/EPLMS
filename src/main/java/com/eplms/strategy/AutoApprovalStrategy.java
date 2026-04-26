package com.eplms.strategy;

import com.eplms.model.LeaveRequest;
import com.eplms.model.LeaveStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AutoApprovalStrategy implements ApprovalStrategy {

    private static final Logger log = LoggerFactory.getLogger(AutoApprovalStrategy.class);

    @Override
    public void process(LeaveRequest request) {
        request.setStatus(LeaveStatus.AUTO_APPROVED);
        request.setApproverComment("Auto-approved: request is 2 days or fewer.");
        log.info("Leave request [id={}] AUTO_APPROVED for employee [id={}]",
                request.getId(), request.getEmployee().getId());
    }

    @Override
    public String getStrategyName() {
        return "AUTO_APPROVAL";
    }
}
