package com.gdn.tms.routing;

import com.gdn.tms.routing.enums.VehicleType;
import com.gdn.tms.routing.pojo.LatLon;
import com.gdn.tms.routing.pojo.VehicleInfo;
import com.gdn.tms.routing.service.api.IAssignmentStrategy;
import com.gdn.tms.routing.service.api.ITSPAssignment;
import com.gdn.tms.routing.service.api.IVRPAssignment;
import com.google.ortools.Loader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public void run(String... args) {
		Loader.loadNativeLibraries();
		System.setProperty("java.awt.headless", "false");
		List<VehicleInfo> infoList = new ArrayList<>();
		for(int i = 1; i <= BIKE_COUNT; i++){
			infoList.add(VehicleInfo.builder().vehicleType(VehicleType.BIKE)
					.identifier("BIKE00" + i).latLon(new LatLon(-6.901932084944011, 107.6123815591905)).build());
		}
		for(int i = 1; i <= CAR_COUNT; i++) {
			infoList.add(VehicleInfo.builder().vehicleType(VehicleType.CAR)
					.identifier("CAR00" + i).latLon(new LatLon(-6.901932084944011, 107.6123815591905)).build());
		}
		if(assignmentStrategy instanceof ITSPAssignment){
			((ITSPAssignment)assignmentStrategy).run(1, 1, 100, 1000, infoList.get(0));
		} else if (assignmentStrategy instanceof IVRPAssignment) {
			((IVRPAssignment)assignmentStrategy).run(1, 1, 100, 1000, infoList);
		}
	}
}
