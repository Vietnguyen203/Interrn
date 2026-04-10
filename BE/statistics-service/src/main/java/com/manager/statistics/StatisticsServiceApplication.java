package com.manager.statistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.manager.statistics.infrastructure.clients")
@ComponentScan(basePackages = {"com.manager"})
@EntityScan(basePackages = {"com.manager"})
@EnableJpaRepositories(basePackages = {"com.manager"})
public class StatisticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatisticsServiceApplication.class, args);
    }
}
