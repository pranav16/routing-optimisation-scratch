package com.gdn.tms.routing.service.impl;

import com.gdn.tms.routing.pojo.LatLon;
import com.gdn.tms.routing.pojo.OSRMDistanceResponse;
import com.gdn.tms.routing.service.api.IDistanceMatrixProvider;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.List;

@Service
@ConditionalOnBean(value = DistanceMatrixGenerator.class)
public class OsrmDistanceMatrixProvider implements IDistanceMatrixProvider {
   @Autowired
   RestTemplate restTemplate;
   @Value("${osrm.table.base.url}")
   String BASE_URL;
    @Override
    public long[][] getDistanceMatrix(List<LatLon> latLonPairs) {
        if(latLonPairs.size() == 0){
            return new long[0][];
        }
        //TODO: generate this url better
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (; i < latLonPairs.size() - 1; i++) {
            LatLon l = latLonPairs.get(i);
            builder.append(l.getLon() + "," + l.getLat() + ";");
        }
        builder.append(latLonPairs.get(i).getLon() + "," + latLonPairs.get(i).getLat());
        builder.append("?annotations=distance");
        ResponseEntity<OSRMDistanceResponse> response
                = restTemplate.getForEntity(BASE_URL + builder, OSRMDistanceResponse.class);
        return response.getBody().getDistances();
    }
}
