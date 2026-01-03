package com.example.trafficalert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TrafficAlertApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrafficAlertApplication.class, args);
    }

}
