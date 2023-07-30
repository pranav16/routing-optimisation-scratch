package com.gdn.tms.routing.service.api;

import com.gdn.tms.routing.pojo.LatLon;

import java.util.List;

public interface ITimeMatrixProvider {

    long[][] getTimeMatrix(List<LatLon> latLonPairs);
}
