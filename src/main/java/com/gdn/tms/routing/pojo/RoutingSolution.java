package com.gdn.tms.routing.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingSolution {
    RoutingDetails start;
    List<RoutingDetails> route;
    double totalDistance;
    double totalWeight;
    String vehicleId;
}
