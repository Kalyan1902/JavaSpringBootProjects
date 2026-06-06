
package com.cts.vis.service;

import com.cts.vis.dto.PolicyDTO;
import com.cts.vis.model.Policy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PolicyService {
    Policy createPolicy(PolicyDTO.CreateRequest dto);
    Policy renewPolicy(Long policyId);
    Policy updatePolicy(Long policyId, BigDecimal coverageAmount);

    List<Policy> myPolicies();
    Policy getMyPolicy(Long policyId);

    // Logic extracted from Controller
    Map<Long, Boolean> getPolicyLockStatus(List<Policy> policies);
}