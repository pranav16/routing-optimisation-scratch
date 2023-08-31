package com.gdn.tms.routing.service.simulation;

import com.gdn.tms.routing.enums.VehicleType;
import com.gdn.tms.routing.pojo.Courier;
import com.gdn.tms.routing.pojo.RoutingDetails;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class CourierManager {

    private List<Courier> courierPool;

    private Map<String, Courier> lookUp;
    @Value("${courier.manager.rounds.break:2}")
    private Integer roundsForBreak;
    @Value("${courier.manager.break.mins:60}")
    private Integer minsOfBreak;
    private static final Logger logger = Logger.getLogger(CourierManager.class.getName());

    public void setCourierPool(List<Courier> courierPool){
        this.courierPool = courierPool;
        lookUp = courierPool.stream().collect(Collectors.toMap(Courier::getIdentifier, Function.identity()));
    }
    public final List<Courier> getAvailableCouriers(DateTime time, Integer deltaInMins){
        DateTime timeWindow = time.plusMinutes(deltaInMins);
        return courierPool.stream().filter(courier ->
                courier.getTimeAtHub().isBefore(timeWindow)).collect(Collectors.toList());
    }
    public void updateCourier(String identifier, DateTime timeOfReturn, List<RoutingDetails> runsheet){
        Courier courier = lookUp.get(identifier);
        courier.setRoutingDetails(runsheet);
        courier.setRuns(courier.getRuns() + 1);
        if(courier.getRuns() %  roundsForBreak == 0){
            courier.setBreaks(courier.getBreaks() + 1);
            courier.setTimeAtHub(timeOfReturn.plusMinutes(minsOfBreak));
            logger.info(courier.getIdentifier() + " is going for break" +
                    " total runs made " + courier.getRuns());
        }else{
            courier.setTimeAtHub(timeOfReturn);
        }
    }

    public DateTime getNextAvailableCourierTime(){
        if(courierPool == null || courierPool.isEmpty())
            return null;
        courierPool.sort(Comparator.comparing(Courier::getTimeAtHub));
        return courierPool.get(0).getTimeAtHub();
    }
    public VehicleType getVehicleTypeForCourier(String identifier){
        return lookUp.get(identifier).getType();
    }

}
