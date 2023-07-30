package com.gdn.tms.routing.service.api;

import com.gdn.tms.routing.pojo.RoutingDetails;
import org.apache.commons.math3.util.Pair;

import java.util.List;

public interface IAwbPrioritizer {

    void prioritize(List<RoutingDetails> details, Pair<Integer, Integer> priorityRange);
}
