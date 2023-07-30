package com.gdn.tms.routing.service.impl.utils.ortools;

import com.gdn.tms.routing.pojo.ORRoutingContext;
import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.RoutingSolution;
import com.google.ortools.constraintsolver.Assignment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ORSolutionAdapter {

    private static final Logger logger = Logger.getLogger(ORSolutionAdapter.class.getName());

    public List<RoutingSolution> extractSolution(ORRoutingContext context, Assignment solution) {
        List<RoutingSolution> routingSolutions = new ArrayList<>();
        final Long[] volumetricWeights = context.getRoutingDetails().stream()
                .map(awbDetails -> awbDetails.getVolumetricWeight()).toArray(Long[]::new);
        for (int i = 0; i < context.getVehicleInfos().size(); ++i) {
            RoutingSolution vehicleSolution = RoutingSolution
                    .builder().build();
            long index = context.getRoutingModel().start(i);
            long routeDistance = 0;
            long routeLoad = 0;
            vehicleSolution.setVehicleId(context.getVehicleInfos().get(i).getIdentifier());
            vehicleSolution.setStart(context.getRoutingDetails().get(context.getManager().indexToNode(index)));
            List<RoutingDetails> route = new ArrayList<>();
            while (!context.getRoutingModel().isEnd(index)) {
                long nodeIndex = context.getManager().indexToNode(index);
                route.add(context.getRoutingDetails().get((int) nodeIndex));
                routeLoad += volumetricWeights[(int) nodeIndex];
                long previousIndex = index;
                index = solution.value(context.getRoutingModel().nextVar(index));
                routeDistance += context.getRoutingModel().getArcCostForVehicle(previousIndex, index, i);
            }
            route.add(context.getRoutingDetails().get(context.getManager()
                    .indexToNode(context.getRoutingModel().end(i))));
            vehicleSolution.setRoute(route);
            vehicleSolution.setTotalDistance(routeDistance);
            vehicleSolution.setTotalWeight(routeLoad);
            routingSolutions.add(vehicleSolution);
        }
        return routingSolutions;
    }
    public void logSolution(RoutingSolution solution){
        logger.info("Total weight" + solution.getTotalWeight()
                + "total distance" + solution.getTotalDistance() + " m");
        logger.info("Route: ");
        StringBuilder builder = new StringBuilder();
        for (RoutingDetails detail: solution.getRoute()) {
            builder.append(detail.getIdentifier() + "->");
        }
        logger.info(builder.toString());
    }

    public void logSolution(List<RoutingSolution> routingSolutions) {
        for (RoutingSolution solution : routingSolutions) {
            logger.info("For Vehicle: " + solution.getVehicleId());
            logSolution(solution);
        }
    }
}
