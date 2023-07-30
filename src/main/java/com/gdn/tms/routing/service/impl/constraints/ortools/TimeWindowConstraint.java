package com.gdn.tms.routing.service.impl.constraints.ortools;

import com.gdn.tms.routing.pojo.ORRoutingContext;
import com.gdn.tms.routing.service.api.IConstraint;
import com.google.ortools.constraintsolver.RoutingDimension;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(value="routing.constraint.window", havingValue = "True")
public class TimeWindowConstraint implements IConstraint {

    private static final Logger logger = Logger.getLogger(TimeWindowConstraint.class.getName());

    @Override
    public void addConstraint(ORRoutingContext context) {
        // TODO: make this configurable
        context.getRoutingModel().addDimension(context.getArcCostCallBackIndex(), // transit callback
                3600, // allow waiting time of 1 hour
                86400, // vehicle maximum capacities
                false, // start cumul to zero
                "Time");

        List<long[]> timeWindows = context.getRoutingDetails().stream()
                .map(p -> new long[]{p.getTimeWindow().getStartTime(), p.getTimeWindow().getEndTime()})
                .collect(Collectors.toList());

        // Add time window constraints for each location except depot.
        RoutingDimension timeDimension = context.getRoutingModel().getMutableDimension("Time");
        for (int i = 0; i < timeWindows.size(); ++i) {
            long index = context.getManager().nodeToIndex(i);
            long[] timeWindow = timeWindows.get(i);
            timeDimension.cumulVar(index).setRange(timeWindow[0] * 3600, timeWindow[1] * 3600);
        }
        // Add time window constraints for each vehicle start node.
        long index = context.getRoutingModel().start(0);
        timeDimension.cumulVar(index).setRange(timeWindows.get(0)[0] * 3600, timeWindows.get(0)[1] * 3600);

        // Instantiate route start and end times to produce feasible times.
        context.getRoutingModel().addVariableMinimizedByFinalizer(timeDimension.cumulVar(context.getRoutingModel().start(0)));
        context.getRoutingModel().addVariableMinimizedByFinalizer(timeDimension.cumulVar(context.getRoutingModel().end(0)));
        logger.info("Added pickup delivery constraint");
    }

}
