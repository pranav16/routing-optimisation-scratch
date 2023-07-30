package com.gdn.tms.routing.service.impl.constraints.ortools;

import com.gdn.tms.routing.pojo.ORRoutingContext;
import com.gdn.tms.routing.pojo.PickUpDropOffPair;
import com.gdn.tms.routing.service.api.IConstraint;
import com.google.ortools.constraintsolver.RoutingDimension;
import com.google.ortools.constraintsolver.Solver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
@ConditionalOnProperty(value="routing.constraint.pickup.delivery", havingValue = "True")
public class PickUpDeliveryConstraint implements IConstraint {
    @Value("${routing.constraint.max.distance.limit}")
    private Long MAX_VEHICLE_DISTANCE;

    private static final Logger logger = Logger.getLogger(PickUpDeliveryConstraint.class.getName());

    @Override
    public void addConstraint(ORRoutingContext context) {
        Solver solver = context.getRoutingModel().solver();
        context.getRoutingModel().addDimension(context.getArcCostCallBackIndex(), // transit callback index
                0, // no slack
                MAX_VEHICLE_DISTANCE, // vehicle maximum travel distance
                true, // start cumul to zero
                "Distance");
        RoutingDimension distanceDimension = context.getRoutingModel().getMutableDimension("Distance");
        distanceDimension.setGlobalSpanCostCoefficient(0);

        for (PickUpDropOffPair pair : context.getPickUpDropOffPairList()) {
            long pickupIndex = context.getManager().nodeToIndex(pair.getPickUp());
            long deliveryIndex = context.getManager().nodeToIndex(pair.getDropOff());

            context.getRoutingModel().addPickupAndDelivery(pickupIndex, deliveryIndex);
            // pick up matches delivery
            solver.addConstraint(
                    solver.makeEquality(context.getRoutingModel().vehicleVar(pickupIndex), context.getRoutingModel().vehicleVar(deliveryIndex)));

            // make sure pick up happens before delivery
            solver.addConstraint(solver.makeLessOrEqual(
                    distanceDimension.cumulVar(pickupIndex), distanceDimension.cumulVar(deliveryIndex)));
        }
        logger.info("Added pickup delivery constraint");
    }
}
