A sample project to get use or tools for grouping plus routing.
It assumes that the driver and pick up location are locked. It maximizes on distance, 
capacity and SLA breach is added as penalty.

DummyAwbDetailsGenerator is the class to create dummy routing details. 
We plan to move this to final provider.

RoutingDemoService is the driver class containing the routing solution accepts 
lat, lon, and radius which will be used later.

routing.cost.matrix=Time : Can be distance to make the routing distance based(default).
routing.constraint.window=False : Is the constraint for time window. 
This has some issue at the movement.
routing.constraint.package.count and routing.constraint.capacity are vehicle capacity
and courier capacity.They are enabled by default.

ICostMatrixGenerator - is the interface to get the cost matrix that is used for cost analysis for graph nodes.
We have two implementations DistanceMatrixGenerator and TimeMatrixGenerator for distance and time cost matrix.

The above implementation composes: 
IDistanceMatrixProvider and ITimeMatrixProvider needs to be implemented for Google api. 
We will need to change the conditional properties as well.

IPenaltyGenerator is the interface that is used to generate penalties to drop the a location.
SLABasedPenaltyGenerator is the implementation. 
I have made the system such that the cost penalty is inversely proportional to sla. So lower sla is picked faster. 
I am using distance from hub as a factor to bucket it better, as the evaluation has as 
min(distance + penalty).

I have added a couple of TODOs to clean the solution up, 
there are some places where I have taken the lazier approach.