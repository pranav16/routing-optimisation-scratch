package com.gdn.tms.routing.service.api;

import com.gdn.tms.routing.enums.VehicleType;
import com.gdn.tms.routing.pojo.LatLon;
import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.RoutingSolution;
import com.gdn.tms.routing.pojo.VehicleInfo;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractSimulationStrategyRun implements ISimulationStrategyRun, IAssignmentStrategy{

    @Override
    public List<RoutingSolution> simulate(String batchName, LatLon hubLoc, DateTime timeOfRun, List<RoutingDetails> detailsList, List<VehicleInfo> vehicleInfos, Map<String, Integer> packageCount) {
        if(detailsList.isEmpty()){
            return new ArrayList<>();
        }

        RoutingDetails hub = RoutingDetails.builder().identifier("HUB").lat(hubLoc.getLat()).lon(hubLoc.getLon())
                .volumetricWeight(0L)
                .deadWeight(0)
                .build();

        ArrayList<RoutingDetails> points = new ArrayList<>();
        points.add(hub);
        for (RoutingDetails details: detailsList) {
            DateTime sla = details.getSla();
            DateTime etd = details.getEtd();
            Minutes slaDiff = Minutes.minutesBetween(sla, timeOfRun);
            Minutes etdDiff = Minutes.minutesBetween(etd, timeOfRun);
            Minutes min = slaDiff.isGreaterThan(etdDiff) ? etdDiff : slaDiff;
            details.setSlaInMins(Math.abs(min.getMinutes()));
        }
        points.addAll(detailsList);
        return algorithm(points, vehicleInfos, "simulation/output/" + batchName + ".png", packageCount);
    }
}
