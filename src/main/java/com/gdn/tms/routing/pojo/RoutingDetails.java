package com.gdn.tms.routing.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.Objects;

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
    DateTime bookingTime;
    DateTime sla;
    DateTime etd;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutingDetails details = (RoutingDetails) o;
        return Objects.equals(identifier, details.identifier);
    }

    @Override
    public String toString() {
        return identifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
