package com.gdn.tms.routing.service.api;

import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.LatLon;

import java.util.List;

public interface ICostMatrixGenerator {
    long[][] generateCostMatrix(List<RoutingDetails> detailsList);
}
