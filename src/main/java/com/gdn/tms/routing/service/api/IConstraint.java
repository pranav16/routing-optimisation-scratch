package com.gdn.tms.routing.service.api;

import com.gdn.tms.routing.pojo.ORRoutingContext;

//Responsible For adding the Dimension and
// TODO: considering only single vehicle at this time
public interface IConstraint {
    void addConstraint(ORRoutingContext context);
}
