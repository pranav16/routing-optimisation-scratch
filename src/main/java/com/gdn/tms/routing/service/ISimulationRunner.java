package com.gdn.tms.routing.service;

import com.gdn.tms.routing.pojo.CourierShiftInfo;
import com.gdn.tms.routing.pojo.LatLon;
import com.gdn.tms.routing.service.simulation.Batch;

import java.util.List;

public interface ISimulationRunner {
    void run(CourierShiftInfo courierShiftInfo, List<Batch> batches, LatLon hub);
}
