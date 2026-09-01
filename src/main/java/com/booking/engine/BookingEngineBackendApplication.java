package com.booking.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.booking.engine.repository.sql")
@EnableMongoRepositories(basePackages = "com.booking.engine.repository.mongodb")
@EnableAsync
@EnableScheduling
public class BookingEngineBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingEngineBackendApplication.class, args);
    }
}
