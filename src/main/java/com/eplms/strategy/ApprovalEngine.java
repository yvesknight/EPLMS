package com.eplms.strategy;

import com.eplms.model.LeaveRequest;
import org.springframework.stereotype.Component;

@Component
public class ApprovalEngine {

    private static final int AUTO_APPROVAL_THRESHOLD = 2;
    private static final int STANDARD_APPROVAL_THRESHOLD = 5;

    private final AutoApprovalStrategy autoApproval;
    private final StandardApprovalStrategy standardApproval;
    private final HREscalationStrategy hrEscalation;

    public ApprovalEngine(AutoApprovalStrategy autoApproval,
                          StandardApprovalStrategy standardApproval,
                          HREscalationStrategy hrEscalation) {
        this.autoApproval = autoApproval;
        this.standardApproval = standardApproval;
        this.hrEscalation = hrEscalation;
    }

    public void evaluate(LeaveRequest request) {
        ApprovalStrategy strategy = resolveStrategy(request.getTotalDays());
        strategy.process(request);
    }

    private ApprovalStrategy resolveStrategy(int days) {
        if (days <= AUTO_APPROVAL_THRESHOLD) return autoApproval;
        if (days <= STANDARD_APPROVAL_THRESHOLD) return standardApproval;
        return hrEscalation;
    }
}
