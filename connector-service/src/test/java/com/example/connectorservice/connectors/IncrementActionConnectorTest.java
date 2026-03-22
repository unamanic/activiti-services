package com.example.connectorservice.connectors;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.cloud.api.process.model.IntegrationRequest;
import org.activiti.cloud.connectors.starter.channels.IntegrationResultSender;
import org.activiti.cloud.connectors.starter.configuration.ConnectorProperties;
import org.activiti.cloud.connectors.starter.model.IntegrationResultBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncrementActionConnectorTest {

    @Mock IntegrationResultSender integrationResultSender;
    @Mock ConnectorProperties connectorProperties;
    @Mock IntegrationRequest integrationRequest;
    @Mock IntegrationContext integrationContext;

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void accept_incrementsCountByOneAndSendsResult() {
        try (MockedStatic<IntegrationResultBuilder> mocked = mockStatic(IntegrationResultBuilder.class)) {
            IntegrationResultBuilder mockBuilder = mock(IntegrationResultBuilder.class);
            Message mockMessage = mock(Message.class);

            mocked.when(() -> IntegrationResultBuilder.resultFor(integrationRequest, connectorProperties))
                  .thenReturn(mockBuilder);
            when(mockBuilder.withOutboundVariables(any())).thenReturn(mockBuilder);
            when(mockBuilder.buildMessage()).thenReturn(mockMessage);
            when(integrationRequest.getIntegrationContext()).thenReturn(integrationContext);
            when(integrationContext.getInBoundVariable("count", Integer.class)).thenReturn(5);

            new IncrementActionConnector(integrationResultSender, connectorProperties).accept(integrationRequest);

            ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
            verify(mockBuilder).withOutboundVariables(captor.capture());
            assertThat(captor.getValue()).containsEntry("count", 6);
            verify(integrationResultSender).send(mockMessage);
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void accept_startsFromZero_whenCountIsZero() {
        try (MockedStatic<IntegrationResultBuilder> mocked = mockStatic(IntegrationResultBuilder.class)) {
            IntegrationResultBuilder mockBuilder = mock(IntegrationResultBuilder.class);
            Message mockMessage = mock(Message.class);

            mocked.when(() -> IntegrationResultBuilder.resultFor(integrationRequest, connectorProperties))
                  .thenReturn(mockBuilder);
            when(mockBuilder.withOutboundVariables(any())).thenReturn(mockBuilder);
            when(mockBuilder.buildMessage()).thenReturn(mockMessage);
            when(integrationRequest.getIntegrationContext()).thenReturn(integrationContext);
            when(integrationContext.getInBoundVariable("count", Integer.class)).thenReturn(0);

            new IncrementActionConnector(integrationResultSender, connectorProperties).accept(integrationRequest);

            ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
            verify(mockBuilder).withOutboundVariables(captor.capture());
            assertThat(captor.getValue()).containsEntry("count", 1);
        }
    }
}
