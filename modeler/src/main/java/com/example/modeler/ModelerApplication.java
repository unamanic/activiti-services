package com.example.modeler;

import org.activiti.cloud.starter.juel.configuration.EnableActivitiJuel;
import org.activiti.cloud.starter.modeling.configuration.EnableActivitiModeling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableActivitiModeling
@EnableActivitiJuel
public class ModelerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModelerApplication.class, args);
    }

}
