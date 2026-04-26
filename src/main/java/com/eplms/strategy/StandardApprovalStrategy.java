package com.eplms.strategy;

import com.eplms.model.LeaveRequest;
import com.eplms.model.LeaveStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StandardApprovalStrategy implements ApprovalStrategy {

    private static final Logger log = LoggerFactory.getLogger(StandardApprovalStrategy.class);

    @Override
    public void process(LeaveRequest request) {
        request.setStatus(LeaveStatus.PENDING);
        request.setApproverComment("Awaiting Team Lead approval (3–5 days).");
        log.info("Leave request [id={}] routed to TEAM_LEAD for employee [id={}]",
                request.getId(), request.getEmployee().getId());
    }

    @Override
    public String getStrategyName() {
        return "STANDARD_APPROVAL";
    }
}
