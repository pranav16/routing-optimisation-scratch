package com.gdn.tms.routing.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingDetails {
    double lat;
    double lon;
    long slaInMins;
    int priority;
    TimeWindow timeWindow;
    Long volumetricWeight;
    int deadWeight;
    Long distanceFromDepot;
    String identifier;
}
