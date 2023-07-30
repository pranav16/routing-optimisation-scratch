package com.gdn.tms.routing.service.impl;

import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.LatLon;
import com.gdn.tms.routing.service.api.ICostMatrixGenerator;
import com.gdn.tms.routing.service.api.IDistanceMatrixProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(value="routing.cost.matrix", havingValue = "distance", matchIfMissing = true)
public class DistanceMatrixGenerator implements ICostMatrixGenerator {

    @Autowired
    IDistanceMatrixProvider provider;
    @Override
    public long[][] generateCostMatrix(List<RoutingDetails> detailsList) {
        List<LatLon> locations =  detailsList.stream()
                .map(awbDetails -> new LatLon(awbDetails.getLat(),
                        awbDetails.getLon())).collect(Collectors.toList());
        return provider.getDistanceMatrix(locations);
    }
}
