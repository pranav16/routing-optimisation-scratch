package com.gdn.tms.routing.service.simulation;

import com.gdn.tms.routing.pojo.LatLon;
import com.gdn.tms.routing.pojo.OSRMRoute;
import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.RoutingSolution;
import com.gdn.tms.routing.service.api.ISimulationStrategyRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class Simulation {
    @Autowired
    public ISimulationStrategyRun assignmentStrategy;
    @Autowired
    private CSVRouteReader reader;

    @Autowired
    private OsrmRouteGenerator routeGenerator;
    private static final Logger logger = Logger.getLogger(Simulation.class.getName());
    public void run(List<Batch> batches, LatLon hub){
        List<RoutingDetails> pool = new ArrayList<>();
        for (Batch batch: batches) {
            List<RoutingDetails> batchPoints = reader.getRouteDetails(batch.getCsvFilePath(), pool);
            logger.info("Running batch: " + batch.getBatchName() + " with pool " + batchPoints);

            List<RoutingSolution> solutions = assignmentStrategy.simulate(batch.getBatchName(), hub, batch.getTimeOfRun(),
                    batchPoints, batch.getVehicles());
            for(RoutingSolution solution : solutions){
                List<RoutingDetails> doneShipments = solution.getRoute();
                logSolution(solution);
                generateRoutePoly(solution, hub);
                batchPoints.removeAll(doneShipments);
            }
            pool = batchPoints;
            logger.info("Batch End: " + batch.getBatchName() +  " left for pool: " + pool.toString());
        }
    }

    private void logSolution(RoutingSolution solution){
        logger.info("Vehicle: " + solution.getVehicleId());
        double deadWeight = 0.0, volumetricWeight = 0.0;
        for (RoutingDetails detail: solution.getRoute()) {
            deadWeight += detail.getDeadWeight();
            volumetricWeight += detail.getVolumetricWeight();
            logger.info("Picked: " + detail.getIdentifier() + " sla in mins: " + detail.getSlaInMins() + " priority: " + detail.getPriority());
        }
        logger.info("dead weight: " + deadWeight + " , volumetric weight:" + volumetricWeight);
    }

    private void generateRoutePoly(RoutingSolution solution, LatLon hub){
        logger.info("Vehicle: " + solution.getVehicleId());
        logger.info("=========Route: =========" );
        List<LatLon> locations = new ArrayList<>();
        locations.add(hub);
        for (RoutingDetails detail: solution.getRoute()) {
            locations.add(new LatLon(detail.getLat(), detail.getLon()));
        }
        locations.add(hub);
        List<OSRMRoute> routes = routeGenerator.getRoute(locations);
        for (OSRMRoute route: routes) {
            logger.info(route.getGeometry());
        }
        logger.info("=========Route: =========" );
    }
}
