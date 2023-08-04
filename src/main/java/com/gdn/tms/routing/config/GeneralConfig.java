package com.gdn.tms.routing.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.TimeZone;


@Configuration
public class GeneralConfig {

    @Bean
    ObjectMapper getMapper(){
        return new ObjectMapper().registerModule(new JodaModule()).setTimeZone(TimeZone.getDefault());
    }

    @Bean
    public RestTemplate getRestTemplate() {return new RestTemplate();}

}
