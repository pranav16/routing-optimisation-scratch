package com.gdn.tms.routing.service.impl.constraints.ortools;

import com.gdn.tms.routing.pojo.ORRoutingContext;
import com.gdn.tms.routing.service.api.IConstraint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
@Service
@ConditionalOnProperty(value="routing.constraint.package.count", havingValue = "True", matchIfMissing = true)
public class PackageCountConstraint implements IConstraint {

    private static final Logger logger = Logger.getLogger(PackageCountConstraint.class.getName());

    @Override
    public void addConstraint(ORRoutingContext context) {

        // TODO: In case of pick up delivery change this to transit callback with cost only in case of pickup
        // each node has 1 package except the depot

        int packageCountIndex = 0;
        if(Objects.nonNull(context.getPickUpDropOffPairList())){

          Set<Integer> pickUps = context.getPickUpDropOffPairList().stream()
                    .map(pickUpDropOffPair -> pickUpDropOffPair.getPickUp())
                    .collect(Collectors.toSet());
            packageCountIndex = context.getRoutingModel().registerPositiveTransitCallback((long fromIndex, long toIndex) -> {
                int fromNode = context.getManager().indexToNode(fromIndex);
                int toNode = context.getManager().indexToNode(toIndex);
                if(pickUps.contains(fromNode) && pickUps.contains(toNode)){
                    return 1; //depot
                }
                return 0;
            });
        }else{
            packageCountIndex = context.getRoutingModel().registerUnaryTransitCallback((long fromIndex) -> {
                int fromNode = context.getManager().indexToNode(fromIndex);
                if(fromNode == 0){
                    return 0; //depot
                }
                return 1;
            });
        }



        long[] courierCapacities = context.getVehicleInfos().stream()
                .map(vehicleInfo -> vehicleInfo.getVehicleType().getPackageCount()).collect(Collectors.toList())
                .stream()
                .mapToLong(Long::longValue)
                .toArray();
        context.getRoutingModel().addDimensionWithVehicleCapacity(packageCountIndex, 0, // null capacity slack
                courierCapacities, // vehicle maximum capacities
                true, // start cumul to zero
                "PACKAGE_COUNT");
        logger.info("Added package count constraint");
    }
}
