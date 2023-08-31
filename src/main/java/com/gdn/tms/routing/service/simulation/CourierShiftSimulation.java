package com.gdn.tms.routing.service.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.tms.routing.pojo.*;
import com.gdn.tms.routing.service.ISimulationRunner;
import com.gdn.tms.routing.service.api.ISimulationStrategyRun;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(value="simulation.runner", havingValue = "CourierShiftSimulation")
public class CourierShiftSimulation implements ISimulationRunner {
    private final ObjectMapper mapper;
    private final CourierManager courierManager;
    public final ISimulationStrategyRun assignmentStrategy;
    private final CSVRouteReader reader;
    @Value("${courier.shift.pickup.handover:2}")
    private Integer pickUpHandOver;
    @Value("${courier.shift.delivery.handover:5}")
    private Integer deliveryHandOver;

    public CourierShiftSimulation(ObjectMapper mapper, CourierManager courierManager, ISimulationStrategyRun assignmentStrategy, CSVRouteReader reader) {
        this.mapper = mapper;
        this.courierManager = courierManager;
        this.assignmentStrategy = assignmentStrategy;
        this.reader = reader;
    }

    private static final Logger logger = Logger.getLogger(CourierShiftSimulation.class.getName());
    public void run(final CourierShiftInfo courierShiftInfo, List<Batch> batches, LatLon hub){
        courierManager.setCourierPool(courierShiftInfo.getCourierList());
        List<RoutingDetails> masterShipments = new ArrayList<>();
        List<RoutingDetails> pool = new ArrayList<>();
        DateTime timeOfRun = null;
        for (Batch batch: batches) {
            List<RoutingDetails> batchPoints = reader.getRouteDetails(batch.getCsvFilePath(), masterShipments);
            if(timeOfRun == null){
                pool = batchPoints;
                timeOfRun = batch.getTimeOfRun();
            }
            masterShipments = batchPoints;
        }
        logger.info("Starting simulation with couriers: " + courierShiftInfo.getCourierList()
                + " all shipments: " + masterShipments.toString());

        Collections.sort(masterShipments);
        int run = 1;
        while(!pool.isEmpty()){
            List<Courier> availableCouriers = courierManager.getAvailableCouriers(timeOfRun, 30);
            logger.info("running with pool " + pool + " with couriers: " + availableCouriers.toString());
            logger.info("Time of Run: " + timeOfRun);
            if(availableCouriers.isEmpty()){
                logger.info("Shipment arrived before courier, " +
                        "moving ahead in time to match the first courier arrival");
                final DateTime prev = timeOfRun;
                timeOfRun = courierManager.getNextAvailableCourierTime();
                final DateTime cutOffTime = timeOfRun.plusMinutes(1);
                pool.addAll(masterShipments.stream().filter(details -> details.getBookingTime().isBefore(cutOffTime) &&
                                details.getBookingTime().isAfter(prev))
                        .collect(Collectors.toList()));
                continue;
            }
            List<VehicleInfo> info = availableCouriers.stream().map(courier -> new VehicleInfo(courier.getIdentifier(), courier.getType(), hub))
                    .collect(Collectors.toList());
            List<RoutingSolution> solutions = assignmentStrategy.
                    simulate(String.valueOf(run++), hub ,timeOfRun, pool, info, new HashMap<>());

            for (RoutingSolution solution : solutions) {
                final int shipmentCount = solution.getRoute().size();
                double intervalForReturn = solution.getTotalDistance() /
                        courierManager.getVehicleTypeForCourier(solution.getVehicleId()).getMaxVelocity();
                intervalForReturn += pickUpHandOver * shipmentCount;
                intervalForReturn += deliveryHandOver * shipmentCount;
                DateTime timeOfReturn = timeOfRun.plusMinutes((int)intervalForReturn);
                courierManager.updateCourier(solution.getVehicleId(), timeOfReturn, solution.getRoute());
                masterShipments.removeAll(solution.getRoute());
            }
            pool.clear();
            DateTime minTimeForShipment = timeOfRun;
            Collections.sort(masterShipments);
            if(!masterShipments.isEmpty()){
               minTimeForShipment = masterShipments.get(0).getBookingTime();
            }
            DateTime minTimeOfReturn = courierManager.getNextAvailableCourierTime();
            timeOfRun = minTimeOfReturn.isAfter(minTimeForShipment)? minTimeOfReturn : minTimeForShipment;
            logger.info("next time of run is based on courier return: " + minTimeOfReturn.isAfter(minTimeForShipment));
            final DateTime cutOffTime = timeOfRun.plusMinutes(15);
            pool.addAll(masterShipments.stream().filter(details -> details.getBookingTime().isBefore(cutOffTime))
                    .collect(Collectors.toList()));
        }
    }
}
