package com.gdn.tms.routing.service.impl.algorithms.ortools;

import com.gdn.tms.routing.pojo.ORRoutingContext;
import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.RoutingSolution;
import com.gdn.tms.routing.pojo.VehicleInfo;
import com.gdn.tms.routing.service.api.*;
import com.gdn.tms.routing.service.impl.utils.ortools.ORSolutionAdapter;
import com.google.ortools.constraintsolver.*;
import com.google.protobuf.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;


@Service
@ConditionalOnProperty(value="routing.algorithm", havingValue = "ORTSPBackHaul")
public class ORTSPBackHaulService implements ITSPAssignment {
    @Autowired
    ISourceRouteDetailsGenerator awbDetailsGenerator;
    @Autowired
    ICostMatrixGenerator costMatrixGenerator;
    @Autowired
    IPenaltyGenerator penaltyGenerator;
    @Autowired
    ORSolutionAdapter solutionAdapter;
    @Autowired
    List<IConstraint> constraints;

    @Value("${routing.solution.search.time}")
    Long searchDuration;
    private static final Logger logger = Logger.getLogger(ORTSPBackHaulService.class.getName());

    public RoutingSolution run(double lat, double lon, double radius, long maxLimit, VehicleInfo vehicleInfo){
       List<RoutingDetails> points = awbDetailsGenerator.getRouteDetails(lat, lon, radius, maxLimit);
       long[][] arcCost = costMatrixGenerator.generateCostMatrix(points);
        for (int i = 1; i < points.size(); i++) {
            points.get(i).setDistanceFromDepot(arcCost[0][i]);
        }
       RoutingIndexManager manager =
                new RoutingIndexManager(arcCost.length, 1, 0);

        // Create Routing Model.
        RoutingModel routing = new RoutingModel(manager);
        // Add cost for each graph node
        final int transitCallbackIndex =
                routing.registerTransitCallback((long fromIndex, long toIndex) -> {
                    // Convert from routing variable Index to user NodeIndex.
                    int fromNode = manager.indexToNode(fromIndex);
                    int toNode = manager.indexToNode(toIndex);
                    return arcCost[fromNode][toNode];
                });

        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        List<VehicleInfo> vehicleInfos = new ArrayList<>();
        vehicleInfos.add(vehicleInfo);

        ORRoutingContext ORRoutingContext = new ORRoutingContext(routing, manager, transitCallbackIndex,
                points, vehicleInfos, null);
        // add all constraints
        for (IConstraint constraint: constraints) {
            constraint.addConstraint(ORRoutingContext);
        }

        // Add penalty for each node
        long[] penalty = penaltyGenerator.generatePenalty(ORRoutingContext);
        for (int i = 1; i < penalty.length; ++i) {
            routing.addDisjunction(new long[] {manager.nodeToIndex(i)}, penalty[i]);
        }

        RoutingSearchParameters searchParameters =
                main.defaultRoutingSearchParameters()
                        .toBuilder()
                        .setFirstSolutionStrategy(FirstSolutionStrategy.Value.LOCAL_CHEAPEST_ARC)
                        .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
                        .setTimeLimit(Duration.newBuilder().setSeconds(searchDuration).build())
                        .build();

        RoutingSolution routingSolution = RoutingSolution.builder().build();
        // Solve the problem.
        Assignment solution = routing.solveWithParameters(searchParameters);
        if(Objects.nonNull(solution)){
           List<RoutingSolution> solutionList = solutionAdapter.extractSolution(ORRoutingContext, solution);
           solutionAdapter.logSolution(routingSolution);
           if(!solutionList.isEmpty()){
               routingSolution = solutionList.get(0);
           }
        }
        return routingSolution;
    }

    public RoutingSolution extractSolution(RoutingModel routing,RoutingIndexManager manager, Assignment solution,
                                           Long[] volumetricWeights, List<RoutingDetails> details){
        final int vehicleId = 0;
        RoutingSolution routingSolution = RoutingSolution.builder().build();
        List<RoutingDetails> route = new ArrayList<>();
        long index = routing.start(vehicleId);
        long routeDistance = 0;
        long routeLoad = 0;
        routingSolution.setStart(details.get(manager.indexToNode(index)));
        while (!routing.isEnd(index)) {
            long nodeIndex = manager.indexToNode(index);
            routeLoad += volumetricWeights[(int) nodeIndex];
            route.add(details.get((int) nodeIndex));
            long previousIndex = index;
            index = solution.value(routing.nextVar(index));
            routeDistance += routing.getArcCostForVehicle(previousIndex, index, vehicleId);
        }
        route.add(details.get(manager.indexToNode(routing.end(vehicleId))));
        routingSolution.setRoute(route);
        routingSolution.setTotalDistance(routeDistance);
        routingSolution.setTotalWeight(routeLoad);
        return routingSolution;
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
}
