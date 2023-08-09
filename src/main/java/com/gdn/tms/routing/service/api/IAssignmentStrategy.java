package com.gdn.tms.routing.service.api;

import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.RoutingSolution;
import com.gdn.tms.routing.pojo.VehicleInfo;
import java.util.List;
interface IAssignmentStrategy {
    public List<RoutingSolution> algorithm(List<RoutingDetails> points,
                                    List<VehicleInfo> vehicleInfos, String solutionFilePath);
}