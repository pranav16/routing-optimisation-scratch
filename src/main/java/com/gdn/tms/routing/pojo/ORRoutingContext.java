package com.gdn.tms.routing.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gdn.tms.routing.service.api.BaseRoutingContext;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import lombok.Data;

import java.util.List;


@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties
@Data
public class ORRoutingContext extends BaseRoutingContext {
    RoutingModel routingModel;
    RoutingIndexManager manager;
    int arcCostCallBackIndex;

    public ORRoutingContext(RoutingModel routingModel, RoutingIndexManager manager, int arcCostCallBackIndex,
                            List<RoutingDetails> details, List<VehicleInfo> vehicleInfo,
                            List<PickUpDropOffPair> pickUpDropOffPairList) {
        super(pickUpDropOffPairList, details, vehicleInfo);
        this.routingModel = routingModel;
        this.manager = manager;
        this.arcCostCallBackIndex = arcCostCallBackIndex;
    }
}
