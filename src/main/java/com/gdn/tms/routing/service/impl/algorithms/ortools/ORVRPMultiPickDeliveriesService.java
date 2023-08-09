package com.gdn.tms.routing.service.impl.algorithms.ortools;

import com.gdn.tms.routing.pojo.*;
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
@ConditionalOnProperty(value="routing.algorithm", havingValue = "ORMultiPickUpDelivery")
public class ORVRPMultiPickDeliveriesService implements IVRPAssignment{
    @Autowired
    ISourceRouteDetailsGenerator awbDetailsGenerator;
    @Autowired
    ICostMatrixGenerator costMatrixGenerator;
    @Autowired
    IPenaltyGenerator penaltyGenerator;
    @Autowired
    List<IConstraint> constraints;
    @Autowired
    ORSolutionAdapter solutionAdapter;
    @Value("${routing.solution.search.time}")
    Long searchDuration;
    private static final Logger logger = Logger.getLogger(ORVRPMultiPickDeliveriesService.class.getName());

    MultiPickUpDeliveryDetails multiPickUpDeliveryDetails;
    public List<RoutingSolution> run(double lat, double lon, double radius, long maxLimit,
                                     List<VehicleInfo> vehicleInfos){
       multiPickUpDeliveryDetails = awbDetailsGenerator.
               getMultiPickUpDeliveryDetails(lat, lon, radius, maxLimit);
       List<RoutingDetails> points = multiPickUpDeliveryDetails.getRoutingDetails();
       return algorithm(points, vehicleInfos, null);

    }

    @Override
    public List<RoutingSolution> algorithm(List<RoutingDetails> points, List<VehicleInfo> vehicleInfos, String solutionFilePath) {
        long[][] arcCost = costMatrixGenerator.generateCostMatrix(points);
        for (int i = 1; i < points.size(); i++) {
            points.get(i).setDistanceFromDepot(arcCost[0][i]);
        }
        RoutingIndexManager manager =
                new RoutingIndexManager(arcCost.length, vehicleInfos.size(), 0);

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

        ORRoutingContext orRoutingContext = new ORRoutingContext(routing, manager, transitCallbackIndex,
                points, vehicleInfos, multiPickUpDeliveryDetails.getPickUpDropOffPairs());

        // add all constraints
        for (IConstraint constraint: constraints) {
            constraint.addConstraint(orRoutingContext);
        }

        // Add penalty for each node
        long[] penalty = penaltyGenerator.generatePenalty(orRoutingContext);
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

        List<RoutingSolution> routingSolutions = new ArrayList<>();
        // Solve the problem.
        Assignment solution = routing.solveWithParameters(searchParameters);
        if(Objects.nonNull(solution)){
            routingSolutions = solutionAdapter.extractSolution(orRoutingContext, solution);
            solutionAdapter.logSolution(routingSolutions);
        }
        return routingSolutions;
    }
}
