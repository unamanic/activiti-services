package com.example.connectorservice.connectors;

import org.activiti.cloud.common.messaging.functional.InputBinding;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.SubscribableChannel;

public interface IncrementActionConnectorChannels {
    String INCREMENT_ACTION_CONSUMER = "incrementActionConsumer";

    @InputBinding(INCREMENT_ACTION_CONSUMER)
    default SubscribableChannel incrementActionConsumer() {
        return MessageChannels.publishSubscribe(INCREMENT_ACTION_CONSUMER).getObject();
    }
}
