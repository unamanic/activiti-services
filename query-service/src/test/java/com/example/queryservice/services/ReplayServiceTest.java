package com.example.queryservice.services;

import org.activiti.cloud.api.model.shared.impl.events.CloudRuntimeEventImpl;
import org.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;
import org.activiti.cloud.services.audit.api.converters.APIEventToEntityConverters;
import org.activiti.cloud.services.audit.jpa.converters.BaseEventToEntityConverter;
import org.activiti.cloud.services.audit.jpa.events.AuditEventEntity;
import org.activiti.cloud.services.audit.jpa.repository.EventsRepository;
import org.activiti.cloud.services.query.app.repository.ProcessInstanceRepository;
import org.activiti.cloud.services.query.events.handlers.QueryEventHandlerContext;
import org.activiti.cloud.services.query.events.handlers.QueryEventHandlerContextOptimizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplayServiceTest {

    @Mock EventsRepository<AuditEventEntity> auditEventRepository;
    @Mock QueryEventHandlerContext eventHandlerContext;
    @Mock QueryEventHandlerContextOptimizer optimizer;
    @Mock APIEventToEntityConverters eventConverters;
    @Mock ProcessInstanceRepository processInstanceRepository;
    @Mock PurgeService purgeService;

    @InjectMocks ReplayService replayService;

    @Test
    void replay_returnsFalse_whenNoAuditEventsFound() {
        when(auditEventRepository.findAll(any(Specification.class))).thenReturn(List.of());

        assertThat(replayService.replay("proc-1")).isFalse();
        verifyNoInteractions(purgeService, eventHandlerContext);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void replay_returnsTrue_andPurgesAndReplays_whenEventsExist() {
        AuditEventEntity auditEvent = mock(AuditEventEntity.class);
        when(auditEvent.getEventType()).thenReturn("PROCESS_STARTED");

        BaseEventToEntityConverter converter = mock(BaseEventToEntityConverter.class);
        CloudRuntimeEventImpl cloudEvent = mock(CloudRuntimeEventImpl.class);
        when(converter.convertToAPI(auditEvent)).thenReturn(cloudEvent);
        when(eventConverters.getConverterByEventTypeName("PROCESS_STARTED")).thenReturn(converter);
        when(auditEventRepository.findAll(any(Specification.class))).thenReturn(List.of(auditEvent));
        when(optimizer.optimize(any())).thenReturn(List.of(cloudEvent));

        assertThat(replayService.replay("proc-1")).isTrue();
        verify(purgeService).purgeByProcessInstanceId("proc-1");
        verify(eventHandlerContext).handle(any(CloudRuntimeEvent[].class));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void replay_purgesBeforeReplaying() {
        AuditEventEntity auditEvent = mock(AuditEventEntity.class);
        when(auditEvent.getEventType()).thenReturn("PROCESS_COMPLETED");

        BaseEventToEntityConverter converter = mock(BaseEventToEntityConverter.class);
        CloudRuntimeEventImpl cloudEvent = mock(CloudRuntimeEventImpl.class);
        when(converter.convertToAPI(auditEvent)).thenReturn(cloudEvent);
        when(eventConverters.getConverterByEventTypeName("PROCESS_COMPLETED")).thenReturn(converter);
        when(auditEventRepository.findAll(any(Specification.class))).thenReturn(List.of(auditEvent));
        when(optimizer.optimize(any())).thenReturn(List.of(cloudEvent));

        replayService.replay("proc-2");

        var inOrder = inOrder(purgeService, eventHandlerContext);
        inOrder.verify(purgeService).purgeByProcessInstanceId("proc-2");
        inOrder.verify(eventHandlerContext).handle(any());
    }
}
