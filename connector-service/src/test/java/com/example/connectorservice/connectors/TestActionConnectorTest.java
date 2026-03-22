package com.example.connectorservice.connectors;

import org.activiti.cloud.api.process.model.IntegrationRequest;
import org.activiti.cloud.connectors.starter.channels.IntegrationResultSender;
import org.activiti.cloud.connectors.starter.configuration.ConnectorProperties;
import org.activiti.cloud.connectors.starter.model.IntegrationResultBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestActionConnectorTest {

    @Mock IntegrationResultSender integrationResultSender;
    @Mock ConnectorProperties connectorProperties;
    @Mock IntegrationRequest integrationRequest;

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void accept_sendsIntegrationResult() {
        try (MockedStatic<IntegrationResultBuilder> mocked = mockStatic(IntegrationResultBuilder.class)) {
            IntegrationResultBuilder mockBuilder = mock(IntegrationResultBuilder.class);
            Message mockMessage = mock(Message.class);

            mocked.when(() -> IntegrationResultBuilder.resultFor(integrationRequest, connectorProperties))
                  .thenReturn(mockBuilder);
            when(mockBuilder.buildMessage()).thenReturn(mockMessage);

            new TestActionConnector(integrationResultSender, connectorProperties).accept(integrationRequest);

            verify(integrationResultSender).send(mockMessage);
            verify(mockBuilder, never()).withOutboundVariables(any());
        }
    }
}
