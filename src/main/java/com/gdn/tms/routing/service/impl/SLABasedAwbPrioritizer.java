package com.gdn.tms.routing.service.impl;

import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.service.api.IAwbPrioritizer;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

/*
Normalizes the sla into the specified priority range.
Assumes unix style priority => low > higher priority.
low sla higher priority.
*/
@Service
public class SLABasedAwbPrioritizer implements IAwbPrioritizer {

    private static final Logger logger = Logger.getLogger(SLABasedAwbPrioritizer.class.getName());

    @Override
    public void prioritize(List<RoutingDetails> details, Pair<Integer, Integer> priorityRange) {
       final Pair<Integer, Integer> slaRange = getSlaRange(details);
       details.parallelStream().forEach(detail -> {detail.setPriority(getPriorityInRange(detail.getSlaInMins(), slaRange, priorityRange));});
    }

    private int getPriorityInRange(long slaInMins, Pair<Integer, Integer> bounds, Pair<Integer, Integer> priorityRange){
        double weight = (slaInMins - bounds.getFirst()) * 1.0 / (bounds.getSecond() - bounds.getFirst());
        int priority = (int)(weight * priorityRange.getSecond());
        return Math.max(priorityRange.getFirst(), priority);
    }

    private Pair<Integer, Integer> getSlaRange(List<RoutingDetails> details){
        int minSla = Integer.MAX_VALUE, maxSla = Integer.MIN_VALUE;
        for (int i = 1; i < details.size(); i++) {
            RoutingDetails detail = details.get(i);
            if(minSla > detail.getSlaInMins()){
                minSla = (int)detail.getSlaInMins();
            }

            if(maxSla < detail.getSlaInMins()){
                maxSla = (int)detail.getSlaInMins();
            }
        }
      logger.info("sla in range: [ " + minSla + ", " + maxSla + "]");
      return Pair.create(minSla, maxSla);
    }
}
