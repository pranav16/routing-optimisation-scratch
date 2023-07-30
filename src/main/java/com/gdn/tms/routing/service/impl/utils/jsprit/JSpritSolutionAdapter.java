package com.gdn.tms.routing.service.impl.utils.jsprit;

import com.gdn.tms.routing.pojo.ORRoutingContext;
import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.RoutingSolution;
import com.gdn.tms.routing.service.api.BaseRoutingContext;
import com.gdn.tms.routing.service.impl.utils.ortools.ORSolutionAdapter;
import com.google.ortools.constraintsolver.Assignment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

@Service
public class JSpritSolutionAdapter {
    private static final Logger logger = Logger.getLogger(JSpritSolutionAdapter.class.getName());

    public List<RoutingSolution> extractSolution(BaseRoutingContext context,VehicleRoutingProblemSolution solution) {
        List<RoutingSolution> result = new ArrayList<>();
        Collection<VehicleRoute>  routes = solution.getRoutes();
        for (VehicleRoute route: routes) {
           ;
            RoutingSolution routingSolution = RoutingSolution.builder().vehicleId(route.getVehicle().getId())
                    //.start(context.getRoutingDetails().get(route.getStart().getIndex()))
                    .build();
            List<TourActivity> activities = route.getActivities();
            List<RoutingDetails> solutionRoute = new ArrayList<>();
            for (TourActivity activity: activities) {
                String jobId = "";
                if (activity instanceof TourActivity.JobActivity) {
                    jobId = ((TourActivity.JobActivity)activity).getJob().getId();
                } else {
                    jobId = "-";
                }
            }
            routingSolution.setRoute(solutionRoute);
            result.add(routingSolution);
        }
        return result;
    }
}
