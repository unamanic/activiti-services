package com.example.connectorservice.connectors;

import org.activiti.cloud.common.messaging.functional.InputBinding;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.SubscribableChannel;

public interface TestActionConnectorChannels {
    String TEST_ACTION_CONSUMER = "testActionConsumer";

    @InputBinding(TEST_ACTION_CONSUMER)
    default SubscribableChannel testActionConsumer() {
        return MessageChannels.publishSubscribe(TEST_ACTION_CONSUMER).getObject();
    }
}
