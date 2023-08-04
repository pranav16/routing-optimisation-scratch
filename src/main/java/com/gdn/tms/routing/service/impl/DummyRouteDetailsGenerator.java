package com.gdn.tms.routing.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.tms.routing.pojo.MultiPickUpDeliveryDetails;
import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.TimeWindow;
import com.gdn.tms.routing.service.api.ISourceRouteDetailsGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

//TODO: Pick data from actual source
@Service
public class DummyRouteDetailsGenerator implements ISourceRouteDetailsGenerator {
    @Autowired
    ObjectMapper mapper;
    private static final Logger logger = Logger.getLogger(DummyRouteDetailsGenerator.class.getName());

    static double getRandomNumber(double min, double max) {
        return ((Math.random() * (max - min)) + min);
    }
    static List<RoutingDetails> generateRandomValues(){
        List<RoutingDetails> details = new ArrayList<>();
        RoutingDetails cc = RoutingDetails.builder()
                .slaInMins(0)
                .volumetricWeight(0L)
                .lat(-6.2200017)
                .lon(106.8466451)
                .identifier("CC")
                .timeWindow(TimeWindow.builder().startTime(1).endTime(2).build())
                .build();
        details.add(cc);
        for (int i = 0; i < 99; i++) {
            long startTime = (long)getRandomNumber(1, 12);
            long delta = (long)getRandomNumber(1 , 4);
            long endTime = startTime + delta;
            RoutingDetails detail = RoutingDetails.builder()
                    .slaInMins((long)getRandomNumber(10, 1000))
                    .volumetricWeight((long)getRandomNumber(10, 100))
                    .lat(getRandomNumber(-6.2555973,-7.2200017))
                    .lon(getRandomNumber(106.8478141, 107.8478141))
                    .identifier("BLI" + i)
                    .timeWindow(TimeWindow.builder().startTime(startTime)
                            .endTime(endTime).build())
                    .build();
            details.add(detail);
        }

        return details;
    }

    @Override
    public List<RoutingDetails> getRouteDetails(double lat, double lon, double radius, long maxLimit){
        List<RoutingDetails> details = new ArrayList<>();
        try{
//            details = mapper.readValue("[{\n" +
//                    "\t\"lat\": -6.901932084944011,\n" +
//                    "\t\"lon\": 107.6123815591905,\n" +
//                    "\t\"sla_in_mins\": 0,\n" +
//                    "\t\"volumetric_weight\": 0,\n" +
//                    "\t\"time_window\": {\n" +
//                    "\t\t\"start_time\": 1,\n" +
//                    "\t\t\"end_time\": 11\n" +
//                    "\t},\n" +
//                    "\t\"identifier\": \"DEPOT\"\n" +
//                    "}, {\n" +
//                    "\t\"lat\": -6.898907150348287,\n" +
//                    "\t\"lon\": 107.61276035676688,\n" +
//                    "\t\"sla_in_mins\": 100,\n" +
//                    "\t\"volumetric_weight\": 5,\n" +
//                    "\t\"time_window\": {\n" +
//                    "\t\t\"start_time\": 2,\n" +
//                    "\t\t\"end_time\": 3\n" +
//                    "\t},\n" +
//                    "\t\"identifier\": \"BLI1107234649156\"\n" +
//                    "}, {\n" +
//                    "\t\"lat\": -6.860134886487217,\n" +
//                    "\t\"lon\": 107.62267847440233,\n" +
//                    "\t\"sla_in_mins\": 130,\n" +
//                    "\t\"volumetric_weight\": 5,\n" +
//                    "\t\"time_window\": {\n" +
//                    "\t\t\"start_time\": 3,\n" +
//                    "\t\t\"end_time\": 5\n" +
//                    "\t},\n" +
//                    "\t\"identifier\": \"BLI1107234442557\"\n" +
//                    "}, {\n" +
//                    "\t\"lat\": -6.897927626807114,\n" +
//                    "\t\"lon\": 107.60850494856804,\n" +
//                    "\t\"sla_in_mins\": 160,\n" +
//                    "\t\"volumetric_weight\": 5,\n" +
//                    "\t\"time_window\": {\n" +
//                    "\t\t\"start_time\": 4,\n" +
//                    "\t\t\"end_time\": 5\n" +
//                    "\t},\n" +
//                    "\t\"identifier\": \"BLI1107239263284\"\n" +
//                    "}, {\n" +
//                    "\t\"lat\": -6.9208930044945145,\n" +
//                    "\t\"lon\": 107.60696985316298,\n" +
//                    "\t\"sla_in_mins\": -120,\n" +
//                    "\t\"volumetric_weight\": 15,\n" +
//                    "\t\"time_window\": {\n" +
//                    "\t\t\"start_time\": 4,\n" +
//                    "\t\t\"end_time\": 6\n" +
//                    "\t},\n" +
//                    "\t\"identifier\": \"BLI1107235117767\"\n" +
//                    "}, {\n" +
//                    "\t\"lat\": -6.901743404138123,\n" +
//                    "\t\"lon\": 107.64994012289219,\n" +
//                    "\t\"sla_in_mins\": 220,\n" +
//                    "\t\"volumetric_weight\": 15,\n" +
//                    "\t\"time_window\": {\n" +
//                    "\t\t\"start_time\": 6,\n" +
//                    "\t\t\"end_time\": 10\n" +
//                    "\t},\n" +
//                    "\t\"identifier\": \"BLI1107231235525\"\n" +
//                    "}, {\n" +
//                    "\t\"lat\": -6.89701833630495,\n" +
//                    "\t\"lon\": 107.63624536512651,\n" +
//                    "\t\"sla_in_mins\": 250,\n" +
//                    "\t\"volumetric_weight\": 15,\n" +
//                    "\t\"time_window\": {\n" +
//                    "\t\t\"start_time\": 6,\n" +
//                    "\t\t\"end_time\": 20\n" +
//                    "\t},\n" +
//                    "\t\"identifier\": \"BLI1107238522893\"\n" +
//                    "}]", new TypeReference<ArrayList<RoutingDetails>>(){});
//            details = mapper.readValue("[{\n" +
//                    "\t\"lat\": -6.901932084944011,\n" +
//                    "\t\"lon\": 107.6123815591905,\n" +
//                    "\t\"sla_in_mins\": 100,\n" +
//                    "\t\"volumetric_weight\": 10,\n" +
//                    "\t\"identifier\": \"DEPOT\"\n" +
//                    "}, {\n" +
//                    "\t\"lat\": -6.8970183363049,\n" +
//                    "\t\"lon\": 107.63624536512651,\n" +
//                    "\t\"sla_in_mins\": 100,\n" +
//                    "\t\"volumetric_weight\": 50,\n" +
//                    "\t\"identifier\": \"BLI1107238522893\"\n" +
//                    "}, {\n" +
//                    "\t\"lat\": -6.8970183363049,\n" +
//                    "\t\"lon\": 107.63624536512651,\n" +
//                    "\t\"sla_in_mins\": 120,\n" +
//                    "\t\"volumetric_weight\": 50,\n" +
//                    "\t\"identifier\": \"BLI1107238522894\"\n" +
//                    "}, {\n" +
//                    "\t\"lat\": -6.8970183363049,\n" +
//                    "\t\"lon\": 107.63624536512651,\n" +
//                    "\t\"sla_in_mins\": 90,\n" +
//                    "\t\"volumetric_weight\": 50,\n" +
//                    "\t\"identifier\": \"BLI1107238522895\"\n" +
//                    "}]", new TypeReference<ArrayList<RoutingDetails>>(){});

                        details = mapper.readValue("[{\n" +
                                "\n" +
                                "\t\"lat\": -6.901932084944011,\n" +
                                "\t\"lon\": 107.6123815591905,\n" +
                                "\t\"sla_in_mins\": 0,\n" +
                                "\t\"volumetric_weight\": 0,\n" +
                                "\t\"dead_weight\": 0,\n" +
                                "\t\"identifier\": \"HUB\"\n" +
                                "}, {\n" +
                                "\n" +
                                "\t\"lat\": -6.8970183363049,\n" +
                                "\t\"lon\": 107.63624536512651,\n" +
                                "\t\"sla_in_mins\": 100,\n" +
                                "\t\"volumetric_weight\": 100,\n" +
                                "\t\"dead_weight\": 250,\n" +
                                "\t\"identifier\": \"BLI1107238522893\"\n" +
                                "}, {\n" +
                                "\n" +
                                "\t\"lat\": -6.8970183363049,\n" +
                                "\t\"lon\": 107.63624536512651,\n" +
                                "\t\"sla_in_mins\": 100,\n" +
                                "\t\"volumetric_weight\": 100,\n" +
                                "\t\"dead_weight\": 250,\n" +
                                "\t\"identifier\": \"BLI1107238522894\"\n" +
                                "}, {\n" +
                                "\n" +
                                "\t\"lat\": -6.8970183363049,\n" +
                                "\t\"lon\": 107.63624536512651,\n" +
                                "\t\"sla_in_mins\": 100,\n" +
                                "\t\"volumetric_weight\": 100,\n" +
                                "\t\"dead_weight\": 20,\n" +
                                "\t\"identifier\": \"BLI1107238522895\"\n" +
                                "}]", new TypeReference<ArrayList<RoutingDetails>>(){});

        }catch (Exception ex){
          logger.info("Exception:" + ex.getMessage());
        }
        return details;
    }

    @Override
    public MultiPickUpDeliveryDetails getMultiPickUpDeliveryDetails(double lat, double lon, double radius, long maxLimit) {
        MultiPickUpDeliveryDetails details = new MultiPickUpDeliveryDetails();
        try {
            details = mapper.readValue("{\n" +
                    "\t\"pick_up_drop_off_pairs\": [{\n" +
                    "\t\t\"pick_up\": 1,\n" +
                    "\t\t\"drop_off\": 2\n" +
                    "\t}, {\n" +
                    "\t\t\"pick_up\": 3,\n" +
                    "\t\t\"drop_off\": 4\n" +
                    "\t}, {\n" +
                    "\t\t\"pick_up\": 5,\n" +
                    "\t\t\"drop_off\": 6\n" +
                    "\t}],\n" +
                    "\t\"routing_details\": [{\n" +
                    "\t\t\"lat\": -6.901932084944011,\n" +
                    "\t\t\"lon\": 107.6123815591905,\n" +
                    "\t\t\"sla_in_mins\": 0,\n" +
                    "\t\t\"volumetric_weight\": 0,\n" +
                    "\t\t\"time_window\": {},\n" +
                    "\t\t\"identifier\": \"DEPOT\"\n" +
                    "\t}, {\n" +
                    "\t\t\"lat\": -6.898907150348287,\n" +
                    "\t\t\"lon\": 107.61276035676688,\n" +
                    "\t\t\"sla_in_mins\": 100,\n" +
                    "\t\t\"volumetric_weight\": 5,\n" +
                    "\t\t\"time_window\": {},\n" +
                    "\t\t\"identifier\": \"1\"\n" +
                    "\t}, {\n" +
                    "\t\t\"lat\": -6.860134886487217,\n" +
                    "\t\t\"lon\": 107.62267847440233,\n" +
                    "\t\t\"sla_in_mins\": 130,\n" +
                    "\t\t\"volumetric_weight\": 5,\n" +
                    "\t\t\"time_window\": {},\n" +
                    "\t\t\"identifier\": \"2\"\n" +
                    "\t}, {\n" +
                    "\t\t\"lat\": -6.897927626807114,\n" +
                    "\t\t\"lon\": 107.60850494856804,\n" +
                    "\t\t\"sla_in_mins\": 160,\n" +
                    "\t\t\"volumetric_weight\": 5,\n" +
                    "\t\t\"time_window\": {},\n" +
                    "\t\t\"identifier\": \"3\"\n" +
                    "\t}, {\n" +
                    "\t\t\"lat\": -6.9208930044945145,\n" +
                    "\t\t\"lon\": 107.60696985316298,\n" +
                    "\t\t\"sla_in_mins\": 190,\n" +
                    "\t\t\"volumetric_weight\": 5,\n" +
                    "\t\t\"time_window\": {},\n" +
                    "\t\t\"identifier\": \"4\"\n" +
                    "\t}, {\n" +
                    "\t\t\"lat\": -6.901743404138123,\n" +
                    "\t\t\"lon\": 107.64994012289219,\n" +
                    "\t\t\"sla_in_mins\": 210,\n" +
                    "\t\t\"volumetric_weight\": 5,\n" +
                    "\t\t\"time_window\": {},\n" +
                    "\t\t\"identifier\": \"5\"\n" +
                    "\t}, {\n" +
                    "\t\t\"lat\": -6.89701833630495,\n" +
                    "\t\t\"lon\": 107.63624536512651,\n" +
                    "\t\t\"sla_in_mins\": 230,\n" +
                    "\t\t\"volumetric_weight\": 5,\n" +
                    "\t\t\"time_window\": {},\n" +
                    "\t\t\"identifier\": \"6\"\n" +
                    "\t}]\n" +
                    "\n" +
                    "}", MultiPickUpDeliveryDetails.class);
        }catch (Exception ex){
            logger.info("Exception:" + ex.getMessage());
        }
        return details;
    }
}


