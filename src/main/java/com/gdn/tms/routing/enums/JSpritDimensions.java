package com.gdn.tms.routing.enums;

import lombok.Getter;

public enum JSpritDimensions {
    WEIGHT_CAPACITY(0), PACKAGE_COUNT(1), VEHICLE_MAX_DISTANCE(2) , DEAD_WEIGHT_CAPACITY(3);

    JSpritDimensions(int index) {
        this.index = index;
    }

    @Getter
    int index;
}
