package com.gdn.tms.routing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.tms.routing.pojo.CourierShiftInfo;
import com.gdn.tms.routing.pojo.LatLon;
import com.gdn.tms.routing.service.ISimulationRunner;
import com.gdn.tms.routing.service.simulation.*;
import com.google.ortools.Loader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
	ISimulationRunner simulation;
	@Value("${simulation.batch.filepath}")
	private String batchFilePath;
	@Value("${simulation.shifts.filepath}")
	private String shiftFilePath;
	List<Batch> getBatches(String fileName){
		List<Batch> batches = null;
		try{
			batches = mapper.readValue(new File(fileName), new TypeReference<ArrayList<Batch>>(){});
		}catch (Exception ex){
			logger.info(ex.getMessage());
		}
      return batches;
	}

	CourierShiftInfo getShiftInfo(String fileName){
		CourierShiftInfo shiftInfo = null;
		try{
			shiftInfo = mapper.readValue(new File(fileName), CourierShiftInfo.class);
		}catch (Exception ex){
			logger.info(ex.getMessage());
		}
		return shiftInfo;
	}
	private static final Logger logger = Logger.getLogger(CSVRouteReader.class.getName());
	@Override
	public void run(String... args) {
		Loader.loadNativeLibraries();
//		logger.info("time zone: " + TimeZone.getDefault());
//		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
//		logger.info("time zone: " + TimeZone.getDefault());
		//System.setProperty("java.awt.headless", "false");
		List<Batch> batches = getBatches(batchFilePath);
		CourierShiftInfo shiftInfo = getShiftInfo(shiftFilePath);
		simulation.run(shiftInfo, batches, new LatLon(-6.152183, 106.637445));
		//courierShiftSimulation.run(shiftInfo, batches, new LatLon(-6.152183, 106.637445));
	}
}
