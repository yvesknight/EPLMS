package com.eplms.strategy;

import com.eplms.model.LeaveRequest;
import com.eplms.model.LeaveStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HREscalationStrategy implements ApprovalStrategy {

    private static final Logger log = LoggerFactory.getLogger(HREscalationStrategy.class);

    @Override
    public void process(LeaveRequest request) {
        request.setStatus(LeaveStatus.ESCALATED);
        request.setApproverComment("Escalated to HR: request exceeds 5 days.");
        log.info("Leave request [id={}] ESCALATED to HR for employee [id={}]",
                request.getId(), request.getEmployee().getId());
    }

    @Override
    public String getStrategyName() {
        return "HR_ESCALATION";
    }
}
