package com.gdn.tms.routing.pojo.csv;

import com.gdn.tms.routing.pojo.RoutingDetails;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBean;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.TimeZone;

public class ShippingDetailsCsv extends CsvToBean {
    @CsvBindByName(column = "Shipment Number")
    private String shipmentName;
    @CsvBindByName(column = "Booking time")
    private String bookingTime;
    @CsvBindByName(column = "SLA")
    private String sla;
    @CsvBindByName(column = "ETD")
    private String etd;
    @CsvBindByName(column = "Pickup lat")
    private double pickUpLat;
    @CsvBindByName(column = "Pickup Long")
    private double PickUpLon;
    @CsvBindByName(column = "Customer Lat")
    private double customerLat;
    @CsvBindByName(column = "Customer Long")
    private double customerLon;
    @CsvBindByName(column = "Status")
    private String status;
    @CsvBindByName(column = "Dead weight")
    private double deadWeight;
    @CsvBindByName(column = "Volumetric weight")
    private double volumetricWeight;
    @CsvBindByName(column = "Courier name")
    private String courierName;
    @CsvBindByName(column = "runsheet ID")
    private String runsheetId;
    @CsvBindByName(column = "Runsheet creation time")
    private String runsheetCreationTime;

   public RoutingDetails toRoutingDetails(){
       DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd/MM/yy HH:mm");
      return RoutingDetails.builder().lon(customerLon)
               .lat(customerLat)
               .identifier(shipmentName)
               .deadWeight((int)deadWeight)
               .volumetricWeight((long)volumetricWeight)
               .slaInMins(10)
               .sla(DateTime.parse(sla, dateFormat))
               .etd(DateTime.parse(etd, dateFormat))
               .bookingTime(DateTime.parse(bookingTime, dateFormat))
               .build();
   }

}
