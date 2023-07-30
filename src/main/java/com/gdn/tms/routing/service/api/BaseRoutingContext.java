package com.gdn.tms.routing.service.api;

import com.gdn.tms.routing.pojo.PickUpDropOffPair;
import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.VehicleInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BaseRoutingContext {
     List<PickUpDropOffPair> pickUpDropOffPairList;
     List<RoutingDetails> routingDetails;
     List<VehicleInfo> vehicleInfos;
}
