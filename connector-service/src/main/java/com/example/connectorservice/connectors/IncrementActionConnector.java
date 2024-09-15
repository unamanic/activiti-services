package com.example.connectorservice.connectors;

import org.activiti.cloud.api.process.model.IntegrationRequest;
import org.activiti.cloud.common.messaging.functional.ConnectorBinding;
import org.activiti.cloud.common.messaging.functional.ConsumerConnector;
import org.activiti.cloud.connectors.starter.channels.IntegrationResultSender;
import org.activiti.cloud.connectors.starter.configuration.ConnectorProperties;
import org.activiti.cloud.connectors.starter.model.IntegrationResultBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.logging.Logger;

@ConnectorBinding(
        input = IncrementActionConnectorChannels.INCREMENT_ACTION_CONSUMER,
        condition = "",
        outputHeader = ""
)
@Component
public class IncrementActionConnector implements ConsumerConnector<IntegrationRequest> {
    Logger logger = Logger.getLogger(IncrementActionConnector.class.getName());


    private final IntegrationResultSender integrationResultSender;
    private final ConnectorProperties connectorProperties;

    public IncrementActionConnector(
            IntegrationResultSender integrationResultSender,
            ConnectorProperties connectorProperties
    ) {
        this.integrationResultSender = integrationResultSender;
        this.connectorProperties = connectorProperties;
    }

    @Override
    public void accept(IntegrationRequest integrationRequest) {

        logger.info("Increment Action Called");

        Integer count = integrationRequest.getIntegrationContext().getInBoundVariable("count", Integer.class);

        Map<String, Object> result = Map.of("count", count + 1);

        integrationResultSender.send(
                IntegrationResultBuilder.resultFor(integrationRequest, connectorProperties).withOutboundVariables(result).buildMessage()
        );
    }
}
