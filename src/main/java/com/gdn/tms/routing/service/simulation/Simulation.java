package com.gdn.tms.routing.service.simulation;

import com.gdn.tms.routing.pojo.*;
import com.gdn.tms.routing.service.ISimulationRunner;
import com.gdn.tms.routing.service.api.ISimulationStrategyRun;
import com.gdn.tms.routing.service.impl.SLABasedAwbPrioritizer;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(value="simulation.runner", havingValue = "Simulation", matchIfMissing = true)
public class Simulation implements ISimulationRunner {
    public final ISimulationStrategyRun assignmentStrategy;
    private final CSVRouteReader reader;
    private final CourierManager manager;
    private final OsrmRouteGenerator routeGenerator;

    private final SLABasedAwbPrioritizer prioritizer;

    public Simulation(ISimulationStrategyRun assignmentStrategy, CSVRouteReader reader, CourierManager manager, OsrmRouteGenerator routeGenerator, SLABasedAwbPrioritizer prioritizer) {
        this.assignmentStrategy = assignmentStrategy;
        this.reader = reader;
        this.manager = manager;
        this.routeGenerator = routeGenerator;
        this.prioritizer = prioritizer;
    }

    @Value("${courier.shift.pickup.handover:2}")
    private Integer pickUpHandOver;
    @Value("${courier.shift.delivery.handover:5}")
    private Integer deliveryHandOver;

    private static final Logger logger = Logger.getLogger(Simulation.class.getName());
    public void run(CourierShiftInfo courierShiftInfo, List<Batch> batches, LatLon hub){
        List<RoutingDetails> pool = new ArrayList<>();
        manager.setCourierPool(courierShiftInfo.getCourierList());
        for (Batch batch: batches) {
            List<RoutingDetails> batchPoints = reader.getRouteDetails(batch.getCsvFilePath(), pool);
            logger.info("Running batch: " + batch.getBatchName() + " with pool " + batchPoints);
            List<Courier> availableCouriers = manager.getAvailableCouriers(batch.getTimeOfRun(), 30);
            logger.info("Batch run time: " + batch.getTimeOfRun() + "couriers: " + availableCouriers) ;
            List<VehicleInfo> info = availableCouriers.stream().map(courier -> new VehicleInfo(courier.getIdentifier(), courier.getType(), hub))
                    .collect(Collectors.toList());
            if(info.isEmpty()){
                logger.info("No courier available for this batch, Skipping");
                continue;
            }
            Map<String, Integer> packCounts = new HashMap<>();

            int capacity = (int) Math.ceil(batchPoints.size() * 1.0 / info.size());
            logger.info("capacity:" + capacity);
            for (VehicleInfo i : info) {
                 packCounts.put(i.getIdentifier(), capacity);
            }
            List<RoutingSolution> solutions = assignmentStrategy.simulate(batch.getBatchName(), hub, batch.getTimeOfRun(),
                    batchPoints, info, packCounts);
            for(RoutingSolution solution : solutions){
                List<RoutingDetails> doneShipments = solution.getRoute();
                final int shipmentCount = solution.getRoute().size();
                double intervalForReturn = solution.getTotalDistance() /
                        manager.getVehicleTypeForCourier(solution.getVehicleId()).getMaxVelocity();
                intervalForReturn += pickUpHandOver * shipmentCount;
                intervalForReturn += deliveryHandOver * shipmentCount;
                DateTime timeOfReturn = batch.getTimeOfRun().plusMinutes((int)intervalForReturn);
                manager.updateCourier(solution.getVehicleId(), timeOfReturn, solution.getRoute());
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
