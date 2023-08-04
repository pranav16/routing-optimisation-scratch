package com.gdn.tms.routing.service.simulation;

import com.gdn.tms.routing.pojo.RoutingDetails;
import com.gdn.tms.routing.pojo.csv.ShippingDetailsCsv;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class CSVRouteReader {
    private static final Logger logger = Logger.getLogger(CSVRouteReader.class.getName());
    public List<RoutingDetails> getRouteDetails(String filePath, List<RoutingDetails> initialSet){
        List<RoutingDetails> details = new ArrayList<>(initialSet);

        try(CSVReader csvReader = new CSVReader(new FileReader(filePath))){
            CsvToBeanBuilder<ShippingDetailsCsv> beanBuilder = new CsvToBeanBuilder<>(csvReader);
            beanBuilder.withType(ShippingDetailsCsv.class);
            List<RoutingDetails> a = beanBuilder.build().parse()
                    .stream().map(ShippingDetailsCsv::toRoutingDetails)
                    .collect(Collectors.toList());
            details.addAll(a);
        }catch (Exception exception){
            logger.info("Issue with csv file reading :" + exception.getMessage());
        }
     return details;
    }



}
