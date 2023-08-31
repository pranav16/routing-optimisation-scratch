package com.gdn.tms.routing.service.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.tms.routing.pojo.*;
import com.gdn.tms.routing.service.ISimulationRunner;
import com.gdn.tms.routing.service.api.ISimulationStrategyRun;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(value="simulation.runner", havingValue = "FixedTimeSimulation")
public class FixedTimeSimulation implements ISimulationRunner {
    private final ObjectMapper mapper;
    private final CourierManager courierManager;
    public final ISimulationStrategyRun assignmentStrategy;
    private final CSVRouteReader reader;
    private final OsrmRouteGenerator routeGenerator;
    @Value("${courier.shift.pickup.handover:2}")
    private Integer pickUpHandOver;
    @Value("${courier.shift.delivery.handover:5}")
    private Integer deliveryHandOver;

    private static final Logger logger = Logger.getLogger(FixedTimeSimulation.class.getName());

    public FixedTimeSimulation(ObjectMapper mapper, CourierManager courierManager, ISimulationStrategyRun assignmentStrategy, CSVRouteReader reader, OsrmRouteGenerator routeGenerator) {
        this.mapper = mapper;
        this.courierManager = courierManager;
        this.assignmentStrategy = assignmentStrategy;
        this.reader = reader;
        this.routeGenerator = routeGenerator;
    }

    public void run(final CourierShiftInfo courierShiftInfo, List<Batch> batches, LatLon hub){
        List<RoutingDetails> pool = new ArrayList<>();
        courierManager.setCourierPool(courierShiftInfo.getCourierList());
        List<RoutingDetails> masterShipments = new ArrayList<>();
        int run = 1;
        if(!batches.isEmpty()){
           masterShipments.addAll(reader.getRouteDetails(batches.get(0).getCsvFilePath(), pool));
        }
        for (Batch batch: batches) {
            List<Courier> availableCouriers = courierManager.getAvailableCouriers(batch.getTimeOfRun(), 30);
            logger.info("Running batch at time: " + batch.getTimeOfRun() + "with pool:" + pool + "and couriers: " + availableCouriers);
            if(availableCouriers.isEmpty()){
                continue;
            }
            final DateTime cutOffTime = batch.getTimeOfRun();
            pool.addAll(masterShipments.stream().filter(details -> details.getBookingTime().isBefore(cutOffTime))
                    .collect(Collectors.toList()));

            List<VehicleInfo> info = availableCouriers.stream().map(courier -> new VehicleInfo(courier.getIdentifier(), courier.getType(), hub))
                    .collect(Collectors.toList());
            List<RoutingSolution> solutions = assignmentStrategy.
                    simulate(String.valueOf(run++), hub ,batch.getTimeOfRun(), pool, info,  new HashMap<>());

            for (RoutingSolution solution : solutions) {
                final int shipmentCount = solution.getRoute().size();
                double intervalForReturn = solution.getTotalDistance() /
                        courierManager.getVehicleTypeForCourier(solution.getVehicleId()).getMaxVelocity();
                intervalForReturn += pickUpHandOver * shipmentCount;
                intervalForReturn += deliveryHandOver * shipmentCount;
                DateTime timeOfRun = batch.getTimeOfRun();
                DateTime timeOfReturn = timeOfRun.plusMinutes((int)intervalForReturn);
                courierManager.updateCourier(solution.getVehicleId(), timeOfReturn, solution.getRoute());
                logSolution(solution);
                generateRoutePoly(solution, hub);
                masterShipments.removeAll(solution.getRoute());
            }
            pool.clear();
        }
        logger.info("Done shipments not considered: " + masterShipments);
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
