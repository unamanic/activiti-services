package com.example.queryservice.transformers;

import org.activiti.cloud.api.model.shared.events.CloudVariableUpdatedEvent;
import org.activiti.cloud.services.audit.jpa.events.VariableUpdatedEventEntity;

public class VariableUpdatedEvent {

    public CloudVariableUpdatedEvent transform(VariableUpdatedEventEntity variableUpdatedEventEntity) {
        return null;
    }
}
