package com.gdn.tms.routing.service.impl;

import com.gdn.tms.routing.pojo.ORRoutingContext;
import com.gdn.tms.routing.service.api.IPenaltyGenerator;
import org.springframework.stereotype.Service;

//TODO come up with right distribution
@Service
public class SLABasedPenaltyGenerator implements IPenaltyGenerator {
    @Override
    public long[] generatePenalty(ORRoutingContext context) {
        long[] penalties = new long[context.getRoutingDetails().size()];
        for (int i = 1; i < context.getRoutingDetails().size(); i++) {
            penalties[i] = Integer.MAX_VALUE - context.getRoutingDetails().get(i).getSlaInMins()
                    * context.getRoutingDetails().get(i).getDistanceFromDepot();
        }
        return penalties;
    }
}
