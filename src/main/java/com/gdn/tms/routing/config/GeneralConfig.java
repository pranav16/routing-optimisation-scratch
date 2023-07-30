package com.gdn.tms.routing.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class GeneralConfig {

    @Bean
    ObjectMapper getMapper(){
        return new ObjectMapper();
    }

    @Bean
    public RestTemplate getRestTemplate() {return new RestTemplate();}
}
