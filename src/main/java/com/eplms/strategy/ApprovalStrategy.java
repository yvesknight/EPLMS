package com.eplms.strategy;

import com.eplms.model.LeaveRequest;

public interface ApprovalStrategy {
    void process(LeaveRequest request);
    String getStrategyName();
}
