package com.gdn.tms.routing.service.api;

import com.gdn.tms.routing.pojo.MultiPickUpDeliveryDetails;
import com.gdn.tms.routing.pojo.RoutingDetails;

import java.util.List;

public interface ISourceRouteDetailsGenerator {
    List<RoutingDetails> getRouteDetails(double lat, double lon, double radius, long maxLimit);
    MultiPickUpDeliveryDetails getMultiPickUpDeliveryDetails(double lat, double lon, double radius, long maxLimit);
}
