package com.gdn.tms.routing.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

// All values in SI units, expect for velocity which is in meters per min.
public enum VehicleType {
    BIKE(150, 1L, 333, 50000, 100),
    CAR(500,1L, 200, Integer.MAX_VALUE, 100),
    TRUCK(1000L, 100L, 2,50000, 20);
    VehicleType(long capacity, long packageCount, double maxVelocity, double maxDistance, int deadWeight){
        this.capacity = capacity;
        this.packageCount = packageCount;
        this.maxVelocity = maxVelocity;
        this.maxDistance = maxDistance;
        this.deadWeight = deadWeight;
    }

    @Getter
    long capacity;
    @Getter
    long packageCount;
    @Getter
    double maxVelocity; // m/s
    @Getter
    double maxDistance;
    @Getter
    int deadWeight;
    @JsonCreator
    public static VehicleType forValue(String value) {
       switch (value){
         case "BIKE": return VehicleType.BIKE;
       }
       return VehicleType.BIKE;
    }
}
