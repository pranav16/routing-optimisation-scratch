package com.gdn.tms.routing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.tms.routing.pojo.LatLon;
import com.gdn.tms.routing.service.simulation.Batch;
import com.gdn.tms.routing.service.simulation.CSVRouteReader;
import com.gdn.tms.routing.service.simulation.Simulation;
import com.google.ortools.Loader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@SpringBootApplication
public class RoutingApplication implements CommandLineRunner {
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
		}catch (Exception ex){
			logger.info(ex.getMessage());
		}
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
		List<Batch> batches = getBatches("simulation/sale/bandung25/batches.json");
		simulation.run(batches, new LatLon(-6.935987, 107.642309));

	}
}
