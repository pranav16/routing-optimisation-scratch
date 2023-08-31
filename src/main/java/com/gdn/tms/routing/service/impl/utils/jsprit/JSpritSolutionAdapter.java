package com.gdn.tms.routing.service.impl.utils.jsprit;

import com.gdn.tms.routing.pojo.ORRoutingContext;
import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.RoutingSolution;
import com.gdn.tms.routing.service.api.BaseRoutingContext;
import com.gdn.tms.routing.service.impl.utils.ortools.ORSolutionAdapter;
import com.google.ortools.constraintsolver.Assignment;
import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.VehicleIndexComparator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class JSpritSolutionAdapter {
    private static final Logger logger = Logger.getLogger(JSpritSolutionAdapter.class.getName());
    public List<RoutingSolution> extractSolution(BaseRoutingContext context,
                                                 VehicleRoutingProblemSolution solution, VehicleRoutingProblem problem) {
        List<RoutingSolution> result = new ArrayList<>();
        List<VehicleRoute> list = new ArrayList(solution.getRoutes());
        Collections.sort(list, new VehicleIndexComparator());


        for(Iterator var6 = list.iterator(); var6.hasNext();) {
            VehicleRoute route = (VehicleRoute) var6.next();
            List<String> jobIds = new ArrayList<>();
            List<RoutingDetails> solutionRoute = new ArrayList<>();
            RoutingSolution routingSolution = RoutingSolution.builder().vehicleId(route.getVehicle().getId())
                    .build();
            double costs = 0.0;
            TourActivity act;
            TourActivity prevAct = route.getStart();
            for (Iterator var11 = route.getActivities().iterator(); var11.hasNext(); prevAct = act) {
                act = (TourActivity) var11.next();
                String jobId;
                if (act instanceof TourActivity.JobActivity) {
                    jobId = ((TourActivity.JobActivity) act).getJob().getId();
                    jobIds.add(jobId);
                }
                double c = problem.getTransportCosts().getTransportCost(((TourActivity)prevAct).getLocation(), act.getLocation(), ((TourActivity)prevAct).getEndTime(), route.getDriver(), route.getVehicle());
                c += problem.getActivityCosts().getActivityCost(act, act.getArrTime(), route.getDriver(), route.getVehicle());
                costs += c;
            }
            Map<String, RoutingDetails> points = context.getRoutingDetails().stream()
                    .collect(Collectors.toMap(RoutingDetails::getIdentifier, Function.identity()));
            for (String jobId: jobIds) {
                solutionRoute.add(points.get(jobId));
            }
            double c = problem.getTransportCosts().getTransportCost(((TourActivity)prevAct).getLocation(), route.getEnd().getLocation(), ((TourActivity)prevAct).getEndTime(), route.getDriver(), route.getVehicle());
            c += problem.getActivityCosts().getActivityCost(route.getEnd(), route.getEnd().getArrTime(), route.getDriver(), route.getVehicle());
            costs += c;
            routingSolution.setTotalDistance(costs);
            routingSolution.setRoute(solutionRoute);
            result.add(routingSolution);
        }
        return result;
    }
}
