package com.gdn.tms.routing.service.api;

import com.gdn.tms.routing.pojo.RoutingSolution;
import com.gdn.tms.routing.pojo.VehicleInfo;
import com.google.ortools.constraintsolver.Assignment;

public interface ITSPAssignment extends IAssignmentStrategy {
    RoutingSolution run(double lat, double lon, double radius, long maxLimit, VehicleInfo info);
}
