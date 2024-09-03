package com.example.connectorservice;

import org.activiti.cloud.connectors.starter.configuration.EnableActivitiCloudConnector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableActivitiCloudConnector
public class ConnectorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConnectorServiceApplication.class, args);
    }

}
