package com.gdn.tms.routing.service.impl.utils.jsprit;

import com.gdn.tms.routing.enums.JSpritDimensions;
import com.gdn.tms.routing.pojo.VehicleInfo;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JSpritVehicleAdapter {

    final int WEIGHT_INDEX = 0;
    public List<VehicleImpl> buildVehiclesOnWeightAndCount(List<VehicleInfo> infoList){
        List<VehicleImpl> result = new ArrayList<>();
        Map<com.gdn.tms.routing.enums.VehicleType, VehicleType> types = new HashMap<>();
        for (com.gdn.tms.routing.enums.VehicleType type : com.gdn.tms.routing.enums.VehicleType.values()) {
            VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance(type.name())
                    .addCapacityDimension(JSpritDimensions.WEIGHT_CAPACITY.getIndex(), (int)type.getCapacity())
                    .addCapacityDimension(JSpritDimensions.PACKAGE_COUNT.getIndex(), (int)type.getPackageCount())
                    .addCapacityDimension(JSpritDimensions.DEAD_WEIGHT_CAPACITY.getIndex(), (int)type.getDeadWeight())
                    .setMaxVelocity(type.getMaxVelocity());

            Map<String, Object> userProperties = new HashMap<>();
            userProperties.put("max_distance", type.getMaxDistance());
            vehicleTypeBuilder.setUserData(userProperties);
            types.put(type, vehicleTypeBuilder.build());
        }
        Location.Builder locationBuilder = Location.Builder.newInstance();
        for (VehicleInfo info: infoList) {
            VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance(info.getIdentifier());
            locationBuilder.setCoordinate(Coordinate.newInstance(info.getLatLon().getLat(), info.getLatLon().getLon()));
            locationBuilder.setId("0");
            vehicleBuilder.setStartLocation(locationBuilder.build());
            vehicleBuilder.setType(types.get(info.getVehicleType()));
            vehicleBuilder.setReturnToDepot(true);
            result.add(vehicleBuilder.build());
        }
       return result;
    }

}
