package com.gdn.tms.routing.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gdn.tms.routing.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Courier {
    private String identifier;
    @JsonFormat(pattern = "dd/MM/yy HH:mm")
    private DateTime timeAtHub;
    private List<RoutingDetails> routingDetails;
    private VehicleType type;
    private int runs;
    private int breaks;

    @Override
    public String toString() {
        return "Courier{" +
                "identifier='" + identifier + '\'' +
                ", timeAtHub=" + timeAtHub +
                '}';
    }
}
