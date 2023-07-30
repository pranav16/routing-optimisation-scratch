package com.gdn.tms.routing.service.impl.constraints.jsprit;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupService;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import java.util.Map;

public class SlaBreachConstraint implements HardActivityConstraint {

    private StateManager stateManager;
    private StateId distanceId;
    private TransportDistance distanceCalculator;
    private Map<String, Long> slaPerAct;

    public SlaBreachConstraint(StateManager stateManager, StateId distanceId, TransportDistance distanceCalculator, Map<String, Long> slaPerAct) {
        this.stateManager = stateManager;
        this.distanceId = distanceId;
        this.distanceCalculator = distanceCalculator;
        this.slaPerAct = slaPerAct;
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        double distancePrevAct2NewAct = this.distanceCalculator.getDistance(prevAct.getLocation(), newAct.getLocation(), prevActDepTime, iFacts.getNewVehicle());
        double distanceNewAct2nextAct = this.distanceCalculator.getDistance(newAct.getLocation(), nextAct.getLocation(), prevActDepTime, iFacts.getNewVehicle());
        double distancePrevAct2NextAct = this.distanceCalculator.getDistance(prevAct.getLocation(), nextAct.getLocation(), prevActDepTime, iFacts.getNewVehicle());

        if (prevAct instanceof Start && nextAct instanceof End) {
            distancePrevAct2NextAct = 0.0;
        }

        if (nextAct instanceof End && !iFacts.getNewVehicle().isReturnToDepot()) {
            distanceNewAct2nextAct = 0.0;
            distancePrevAct2NextAct = 0.0;
        }

        double additionalDistance = distancePrevAct2NewAct + distanceNewAct2nextAct - distancePrevAct2NextAct;
        Double currentDistance = 0.0;
        boolean routeIsEmpty = iFacts.getRoute().isEmpty();
        if (!routeIsEmpty) {
            currentDistance = (Double)this.stateManager.getRouteState(iFacts.getRoute(), iFacts.getNewVehicle(), this.distanceId, Double.class);
        }

        long sla = 0;
        if(newAct instanceof PickupService){
            sla =  slaPerAct.get(((PickupService) newAct).getJob().getId());
            if(sla < 0){
                return ConstraintsStatus.FULFILLED;
            }
            double totalDistance =  currentDistance + additionalDistance;
            double speed = iFacts.getNewVehicle().getType().getMaxVelocity();
            double totalTime = totalDistance / speed;
            if(totalTime > sla){
                return ConstraintsStatus.NOT_FULFILLED;
            }
        }

        return ConstraintsStatus.FULFILLED;
    }
}
