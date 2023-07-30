package com.gdn.tms.routing.service.api;

import com.gdn.tms.routing.pojo.ORRoutingContext;

public interface IPenaltyGenerator {
    long[] generatePenalty(ORRoutingContext context);
}
