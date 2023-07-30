package com.gdn.tms.routing.service.impl;

import com.gdn.tms.routing.pojo.LatLon;
import com.gdn.tms.routing.pojo.OSRMTimeResponse;
import com.gdn.tms.routing.service.api.ITimeMatrixProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
@Service
@ConditionalOnBean(value = TimeMatrixGenerator.class)
public class OsrmTimeMatrixProvider implements ITimeMatrixProvider {
    @Autowired
    RestTemplate restTemplate;
    @Value("${osrm.table.base.url}")
    String BASE_URL;
    @Override
    public long[][] getTimeMatrix(List<LatLon> latLonPairs) {
        if(latLonPairs.size() == 0){
            return new long[0][];
        }
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (; i < latLonPairs.size() - 1; i++) {
            LatLon l = latLonPairs.get(i);
            builder.append(l.getLon() + "," + l.getLat() + ";");
        }
        builder.append(latLonPairs.get(i).getLon() + "," + latLonPairs.get(i).getLat());
        builder.append("?annotations=duration");
        ResponseEntity<OSRMTimeResponse> response
                    = restTemplate.getForEntity(BASE_URL + builder, OSRMTimeResponse.class);
        return response.getBody().getDurations();
    }
}
