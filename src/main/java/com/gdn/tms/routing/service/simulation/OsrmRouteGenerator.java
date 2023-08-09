package com.gdn.tms.routing.service.simulation;

import com.gdn.tms.routing.pojo.LatLon;
import com.gdn.tms.routing.pojo.OSRMRoute;
import com.gdn.tms.routing.pojo.OSRMRouteResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OsrmRouteGenerator {

    @Autowired
    RestTemplate restTemplate;
    @Value("${osrm.route.base.url}")
    String BASE_URL;

    public List<OSRMRoute>  getRoute(List<LatLon> latLonPairs) {
        if(latLonPairs.size() == 0){
            return null;
        }
        //TODO: generate this url better
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (; i < latLonPairs.size() - 1; i++) {
            LatLon l = latLonPairs.get(i);
            builder.append(l.getLon() + "," + l.getLat() + ";");
        }
        builder.append(latLonPairs.get(i).getLon() + "," + latLonPairs.get(i).getLat());
        builder.append("?geometries=polyline");
        ResponseEntity<OSRMRouteResponse> response
                = restTemplate.getForEntity(BASE_URL + builder, OSRMRouteResponse.class);
        return response.getBody().getRoutes();
    }
}
