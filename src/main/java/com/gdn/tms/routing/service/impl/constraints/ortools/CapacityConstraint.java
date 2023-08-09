package com.gdn.tms.routing.service.impl.constraints.ortools;

import com.gdn.tms.routing.pojo.ORRoutingContext;
import com.gdn.tms.routing.service.api.IConstraint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(value="routing.constraint.capacity", havingValue = "True", matchIfMissing = true)
public class CapacityConstraint implements IConstraint {
    private static final Logger logger = Logger.getLogger(CapacityConstraint.class.getName());

    @Override
    public void addConstraint(ORRoutingContext context) {
        //volumetric weights for each node
        final Long[] volumetricWeights = context.getRoutingDetails().stream()
                .map(awbDetails -> awbDetails.getVolumetricWeight()).toArray(Long[]::new);
        final int demandCallbackIndex = context.getRoutingModel().registerUnaryTransitCallback((long fromIndex) -> {
            int fromNode = context.getManager().indexToNode(fromIndex);
            logger.info("node: "+fromNode);
            return volumetricWeights[fromNode];
        });
        volumetricWeights[19]++;
        //add the dimension
        long[] vehicleCapacities = context.getVehicleInfos().stream()
                .map(vehicleInfo -> vehicleInfo.getVehicleType().getCapacity()).collect(Collectors.toList())
                .stream()
                .mapToLong(Long::longValue)
                .toArray();

        context.getRoutingModel().addDimensionWithVehicleCapacity(demandCallbackIndex, 0, // null capacity slack
                vehicleCapacities, // vehicle maximum capacities
                true, // start cumul to zero
                "VEHICLE_CAPACITY");

        logger.info("Added capacity constraint");
    }
}
