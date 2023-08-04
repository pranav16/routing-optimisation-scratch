package com.gdn.tms.routing.service.simulation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.gdn.tms.routing.pojo.VehicleInfo;
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
public class Batch {

    private String batchName;
    private String csvFilePath;
    @JsonFormat(pattern = "dd/MM/yy HH:mm")
    private DateTime timeOfRun;
    private List<VehicleInfo> vehicles;

}
