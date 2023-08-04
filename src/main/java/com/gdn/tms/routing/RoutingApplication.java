package com.gdn.tms.routing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.tms.routing.enums.VehicleType;
import com.gdn.tms.routing.pojo.LatLon;
import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.VehicleInfo;
import com.gdn.tms.routing.service.api.IAssignmentStrategy;
import com.gdn.tms.routing.service.simulation.Batch;
import com.gdn.tms.routing.service.simulation.CSVRouteReader;
import com.gdn.tms.routing.service.simulation.Simulation;
import com.google.ortools.Loader;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

@SpringBootApplication
public class RoutingApplication implements CommandLineRunner {
	@Autowired
	public IAssignmentStrategy assignmentStrategy;

	@Value("${simulation.bike.count:1}")
	public int BIKE_COUNT;

	@Value("${simulation.car.count:1}")
	public int CAR_COUNT;

	public static void main(String[] args) {
		SpringApplication.run(RoutingApplication.class, args);
	}

	@Autowired
	ObjectMapper mapper;
	@Autowired
	Simulation simulation;


	List<Batch> getBatches(String fileName){
		List<Batch> batches = null;
		try{
			batches = mapper.readValue(new File(fileName), new TypeReference<ArrayList<Batch>>(){});
		}catch (Exception ex){}
      return batches;
	}
	private static final Logger logger = Logger.getLogger(CSVRouteReader.class.getName());
	@Override
	public void run(String... args) {
		Loader.loadNativeLibraries();
//		logger.info("time zone: " + TimeZone.getDefault());
//		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
//		logger.info("time zone: " + TimeZone.getDefault());
		System.setProperty("java.awt.headless", "false");
		List<Batch> batches = getBatches("simulation/batches.json");
				List<VehicleInfo> infoList = new ArrayList<>();
		simulation.run(batches);

	}
}
