package com.gdn.tms.routing.service.api;

import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.RoutingSolution;
import com.gdn.tms.routing.pojo.VehicleInfo;
import org.joda.time.DateTime;

import java.util.List;

public interface ISimulationStrategyRun {
    List<RoutingSolution> simulate(String batchName, DateTime timeOfRun, List<RoutingDetails> detailsList, List<VehicleInfo> vehicleInfos);
}
