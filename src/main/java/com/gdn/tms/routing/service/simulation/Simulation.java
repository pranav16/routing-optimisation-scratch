package com.gdn.tms.routing.service.simulation;

import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.RoutingSolution;
import com.gdn.tms.routing.service.api.ISimulationStrategyRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class Simulation {
    @Autowired
    public ISimulationStrategyRun assignmentStrategy;
    @Autowired
    private CSVRouteReader reader;
    private static final Logger logger = Logger.getLogger(Simulation.class.getName());
    public void run(List<Batch> batches){
        List<RoutingDetails> pool = new ArrayList<>();
        for (Batch batch: batches) {
            List<RoutingDetails> batchPoints = reader.getRouteDetails(batch.getCsvFilePath(), pool);
            logger.info("Running batch: " + batch.getBatchName() + " with pool " + batchPoints);
            List<RoutingSolution> solutions = assignmentStrategy.simulate(batch.getBatchName(), batch.getTimeOfRun(),
                    batchPoints, batch.getVehicles());
            for(RoutingSolution solution : solutions){
                List<RoutingDetails> doneShipments = solution.getRoute();
                logSolution(solution);
                batchPoints.removeAll(doneShipments);
            }
            pool = batchPoints;
            logger.info("Batch End: " + batch.getBatchName() +  " left for pool: " + pool.toString());
        }
    }

    private void logSolution(RoutingSolution solution){
        logger.info("Vehicle: " + solution.getVehicleId());
        for (RoutingDetails detail: solution.getRoute()) {
            logger.info("Picked: " + detail.getIdentifier() + " sla in mins: " + detail.getSlaInMins() + " priority: " + detail.getPriority());
        }
    }
}
