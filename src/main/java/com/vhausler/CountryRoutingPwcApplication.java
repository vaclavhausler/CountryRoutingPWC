package com.vhausler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CountryRoutingPwcApplication {

    public static void main(String[] args) {
        SpringApplication.run(CountryRoutingPwcApplication.class, args);
    }

}
