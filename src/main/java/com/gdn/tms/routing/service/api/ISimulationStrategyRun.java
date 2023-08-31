package com.gdn.tms.routing.service.api;

import com.gdn.tms.routing.enums.VehicleType;
import com.gdn.tms.routing.pojo.LatLon;
import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.RoutingSolution;
import com.gdn.tms.routing.pojo.VehicleInfo;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

public interface ISimulationStrategyRun {
    List<RoutingSolution> simulate(String batchName, LatLon hub, DateTime timeOfRun,
                                   List<RoutingDetails> detailsList, List<VehicleInfo> vehicleInfos, Map<String, Integer> packageCount);
}
