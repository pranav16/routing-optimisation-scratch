package com.gdn.tms.routing.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OSRMRouteResponse {
List<OSRMRoute> routes;
}
