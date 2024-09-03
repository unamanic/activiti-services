package com.example.connectorservice.connectors;

import org.activiti.cloud.api.process.model.IntegrationRequest;
import org.activiti.cloud.common.messaging.functional.ConnectorBinding;
import org.activiti.cloud.common.messaging.functional.ConsumerConnector;
import org.activiti.cloud.connectors.starter.channels.IntegrationResultSender;
import org.activiti.cloud.connectors.starter.configuration.ConnectorProperties;
import org.activiti.cloud.connectors.starter.model.IntegrationResultBuilder;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@ConnectorBinding(
        input = TestActionConnectorChannels.TEST_ACTION_CONSUMER,
        condition = "",
        outputHeader = ""
)
@Component
public class TestActionConnector implements ConsumerConnector<IntegrationRequest> {
    Logger logger = Logger.getLogger(TestActionConnector.class.getName());


    private final IntegrationResultSender integrationResultSender;
    private final ConnectorProperties connectorProperties;

    public TestActionConnector(
            IntegrationResultSender integrationResultSender,
            ConnectorProperties connectorProperties
    ) {
        this.integrationResultSender = integrationResultSender;
        this.connectorProperties = connectorProperties;
    }

    @Override
    public void accept(IntegrationRequest integrationRequest) {

        logger.info("Test Action Called");

        integrationResultSender.send(
                IntegrationResultBuilder.resultFor(integrationRequest, connectorProperties).buildMessage()
        );
    }
}
