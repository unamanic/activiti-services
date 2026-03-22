package com.example.queryservice;

import org.activiti.cloud.starter.audit.configuration.EnableActivitiAudit;
import org.activiti.cloud.starter.query.configuration.EnableActivitiQuery;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableActivitiQuery
@EnableActivitiAudit
@EnableAsync
public class QueryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueryServiceApplication.class, args);
    }

}
