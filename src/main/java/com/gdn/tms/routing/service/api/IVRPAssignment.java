package com.gdn.tms.routing.service.api;

import com.gdn.tms.routing.pojo.RoutingSolution;
import com.gdn.tms.routing.pojo.VehicleInfo;

import java.util.List;

public interface IVRPAssignment extends IAssignmentStrategy{
    List<RoutingSolution> run(double lat, double lon, double radius, long maxLimit, List<VehicleInfo> vehicleInfo);
}
