package com.gdn.tms.routing.service.impl.algorithms.jsprit;

import com.gdn.tms.routing.enums.JSpritDimensions;
import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.RoutingSolution;
import com.gdn.tms.routing.pojo.VehicleInfo;
import com.gdn.tms.routing.service.api.*;
import com.gdn.tms.routing.service.impl.constraints.jsprit.SlaBreachConstraint;
import com.gdn.tms.routing.service.impl.utils.jsprit.JSpritSolutionAdapter;
import com.gdn.tms.routing.service.impl.utils.jsprit.JSpritVehicleAdapter;
import com.graphhopper.jsprit.analysis.toolbox.*;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.StateUpdater;
import com.graphhopper.jsprit.core.algorithm.state.VehicleDependentTraveledDistance;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.MaxDistanceConstraint;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.RouteVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Service
@ConditionalOnProperty(value="routing.algorithm", havingValue = "JSVRPBackHaul", matchIfMissing = true)
public class JSVRPBackHaulService implements IVRPAssignment {
    @Autowired
    ISourceRouteDetailsGenerator awbDetailsGenerator;
    @Autowired
    JSpritVehicleAdapter vehicleAdapter;
    @Autowired
    ICostMatrixGenerator costMatrixGenerator;
    @Autowired
    JSpritSolutionAdapter solutionAdapter;
    @Autowired
    IAwbPrioritizer prioritizer;
    private static final Logger logger = Logger.getLogger(JSVRPBackHaulService.class.getName());



    public List<RoutingSolution> run(double lat, double lon, double radius, long maxLimit,
                                     List<VehicleInfo> vehicleInfos){
        List<VehicleImpl> vehicles = vehicleAdapter.buildVehiclesOnWeightAndCount(vehicleInfos);
        Map<Vehicle, Double> maxDistancePerVehicleMap = new HashMap<>();
        for (Vehicle vehicle: vehicles) {
            Map<String, Object> vehicleProperties = (Map<String, Object>)vehicle.getType().getUserData();
            maxDistancePerVehicleMap.put(vehicle, (Double) vehicleProperties.get("max_distance"));
        }
        List<RoutingDetails> points = awbDetailsGenerator.getRouteDetails(lat, lon, radius, maxLimit);
        prioritizer.prioritize(points, Pair.create(1, 10));
        long[][] arcCost = costMatrixGenerator.generateCostMatrix(points);
        List <com.graphhopper.jsprit.core.problem.job.Service> services = new ArrayList<>();
        Location.Builder locationBuilder = Location.Builder.newInstance();
        for (int i =1; i < points.size(); i++) {
            RoutingDetails point = points.get(i);
            locationBuilder
                    .setId(String.valueOf(i))
                    .setCoordinate(Coordinate.newInstance(point.getLat(), point.getLon()));
            services.add(com.graphhopper.jsprit.core.problem.job.Service.Builder.newInstance(point.getIdentifier())
                    .addSizeDimension(JSpritDimensions.WEIGHT_CAPACITY.getIndex(), point.getVolumetricWeight().intValue())
                    .addSizeDimension(JSpritDimensions.DEAD_WEIGHT_CAPACITY.getIndex(), point.getDeadWeight())
                    .setPriority(point.getPriority())
                    .setLocation(locationBuilder.build()).build());
        }
        BaseRoutingContext context = new BaseRoutingContext();
        context.setVehicleInfos(vehicleInfos);
        context.setRoutingDetails(points);

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addAllVehicles(vehicles);
        vrpBuilder.addAllJobs(services);
        vrpBuilder.setRoutingCost(new VehicleRoutingTransportCosts() {
            @Override
            public double getBackwardTransportCost(Location location, Location location1, double v, Driver driver, Vehicle vehicle) {
                return getDistance(location1, location, v, vehicle);
            }
            @Override
            public double getBackwardTransportTime(Location location, Location location1, double v, Driver driver, Vehicle vehicle) {
                return getDistance(location1, location, v, vehicle) / vehicle.getType().getMaxVelocity();
            }
            @Override
            public double getTransportCost(Location location, Location location1, double v, Driver driver, Vehicle vehicle) {
                return getDistance(location, location1, v, vehicle);
            }
            @Override
            public double getTransportTime(Location location, Location location1, double v, Driver driver, Vehicle vehicle) {
                return getDistance(location, location1, v, vehicle)/ vehicle.getType().getMaxVelocity();
            }
            @Override
            public double getDistance(Location location, Location location1, double v, Vehicle vehicle) {
                Integer locationIndex = Integer.parseInt(location.getId());
                Integer location1Index = Integer.parseInt(location1.getId());
                return arcCost[locationIndex][location1Index];
            }
        });
       Map<String, Long> slaMap =  points.stream().collect(Collectors.toMap(RoutingDetails::getIdentifier, RoutingDetails::getSlaInMins));
       vrpBuilder.setActivityCosts(new VehicleRoutingActivityCosts() {
            @Override
            public double getActivityCost(TourActivity tourActivity, double v, Driver driver, Vehicle vehicle) {
                if (tourActivity instanceof TourActivity.JobActivity) {
                   String jobId = ((TourActivity.JobActivity) tourActivity).getJob().getId();
                   return slaMap.get(jobId);
                }
                return 0;
            }
            @Override
            public double getActivityDuration(TourActivity tourActivity, double v, Driver driver, Vehicle vehicle) {
                return 10; // can be taken as service time.
            }
        });

        VehicleRoutingProblem problem = vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();
        VehicleRoutingAlgorithm algorithm = createAlgorithmWithMaxDistanceConstraint(problem, maxDistancePerVehicleMap,  slaMap);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);
        new Plotter(problem, bestSolution).plot("solution.png", "solution");
        return solutionAdapter.extractSolution(context, bestSolution);
    }

    public VehicleRoutingAlgorithm createAlgorithmWithMaxDistanceConstraint(VehicleRoutingProblem vrp, Map<Vehicle, Double> maxDistancePerVehicleMap, Map<String, Long> slaMap) {
        int radialShare = (int)((double)vrp.getJobs().size() * 0.3);
        int randomShare = (int)((double)vrp.getJobs().size() * 0.5);
        Jsprit.Builder builder = Jsprit.Builder.newInstance(vrp);
        builder.setProperty(Jsprit.Parameter.THRESHOLD_ALPHA, "0.0");
        builder.setProperty(Jsprit.Strategy.RADIAL_BEST, "0.5");
        builder.setProperty(Jsprit.Strategy.RADIAL_REGRET, "0.0");
        builder.setProperty(Jsprit.Strategy.RANDOM_BEST, "0.5");
        builder.setProperty(Jsprit.Strategy.RANDOM_REGRET, "0.0");
        builder.setProperty(Jsprit.Strategy.WORST_BEST, "0.0");
        builder.setProperty(Jsprit.Strategy.WORST_REGRET, "0.0");
        builder.setProperty(Jsprit.Strategy.CLUSTER_BEST, "0.0");
        builder.setProperty(Jsprit.Strategy.CLUSTER_REGRET, "0.0");
        builder.setProperty(Jsprit.Parameter.RADIAL_MIN_SHARE, String.valueOf(radialShare));
        builder.setProperty(Jsprit.Parameter.RADIAL_MAX_SHARE, String.valueOf(radialShare));
        builder.setProperty(Jsprit.Parameter.RANDOM_BEST_MIN_SHARE, String.valueOf(randomShare));
        builder.setProperty(Jsprit.Parameter.RANDOM_BEST_MAX_SHARE, String.valueOf(randomShare));
        builder.setProperty(Jsprit.Parameter.THREADS.toString(), "120");

        // example to add state based hard constraint
        StateManager stateManager = new StateManager(vrp);
        StateId distanceStateId = stateManager.createStateId("Distance");
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        stateManager.addStateUpdater(new VehicleDependentTraveledDistance(vrp.getTransportCosts(), stateManager, distanceStateId, maxDistancePerVehicleMap.keySet()));
        MaxDistanceConstraint distanceConstraint = new MaxDistanceConstraint(stateManager, distanceStateId, vrp.getTransportCosts(), maxDistancePerVehicleMap);
        constraintManager.addConstraint(distanceConstraint, ConstraintManager.Priority.HIGH);

//        SlaBreachConstraint slaBreachConstraint = new SlaBreachConstraint(stateManager, distanceStateId, vrp.getTransportCosts(), slaMap);
//        constraintManager.addConstraint(slaBreachConstraint, ConstraintManager.Priority.HIGH);
        builder.setStateAndConstraintManager(stateManager, constraintManager);
        return builder.buildAlgorithm();
    }
}
