package com.example.queryservice.services;

import jakarta.persistence.EntityManager;
import org.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;
import org.activiti.cloud.api.model.shared.events.CloudVariableUpdatedEvent;
import org.activiti.cloud.api.model.shared.impl.events.CloudRuntimeEventImpl;
import org.activiti.cloud.api.model.shared.impl.events.CloudVariableUpdatedEventImpl;
import org.activiti.cloud.services.audit.api.converters.APIEventToEntityConverters;
import org.activiti.cloud.services.audit.jpa.converters.BaseEventToEntityConverter;
import org.activiti.cloud.services.audit.jpa.events.AuditEventEntity;
import org.activiti.cloud.services.audit.jpa.repository.EventsRepository;
import org.activiti.cloud.services.query.app.repository.ProcessInstanceRepository;
import org.activiti.cloud.services.query.events.handlers.QueryEventHandlerContext;
import org.activiti.cloud.services.query.events.handlers.QueryEventHandlerContextOptimizer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReplayService {

    private final EventsRepository<AuditEventEntity> auditEventRepository;

    private final QueryEventHandlerContext eventHandlerContext;
    private final QueryEventHandlerContextOptimizer optimizer;
    private final APIEventToEntityConverters eventConverters;
    private final PurgeService purgeService;

    public ReplayService(EventsRepository<AuditEventEntity> auditEventRepository, QueryEventHandlerContext eventHandlerContext, QueryEventHandlerContextOptimizer optimizer, APIEventToEntityConverters eventConverters, ProcessInstanceRepository processInstanceRepository, PurgeService purgeService) {
        this.auditEventRepository = auditEventRepository;
        this.eventHandlerContext = eventHandlerContext;
        this.optimizer = optimizer;
        this.eventConverters = eventConverters;
        this.purgeService = purgeService;
    }

    @Transactional
    public boolean replay(String id) {
        // find all audit events for a processInstance
        List<AuditEventEntity> auditEvents = auditEventRepository.findAll(((root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("processInstanceId"), id);
        }));

        if (auditEvents.isEmpty()) {
            return false;
        }

        // transform from audit events to cloud Runtime events
        List<CloudRuntimeEvent<?, ?>> cloudEvents = auditEvents.stream()
                // use the audit service's built in mapper to map audit events back to cloud events
                .map(auditEventEntity -> {
                    var converter = eventConverters.getConverterByEventTypeName(auditEventEntity.getEventType());
                    return converter.convertToAPI(auditEventEntity);
                })
                .map(cloudRuntimeEvent -> (CloudRuntimeEventImpl<?, ?>) cloudRuntimeEvent)
                .collect(Collectors.toUnmodifiableList());


        // purge process from query service
        purgeService.purgeByProcessInstanceId(id);

        //reload query service with audit events
        // https://github.com/Activiti/activiti-cloud/blob/afa337cc8ed9ea847b65fd8270b80b5473266a91/activiti-cloud-query-service/activiti-cloud-services-query/activiti-cloud-services-query-events/src/main/java/org/activiti/cloud/services/query/app/QueryConsumerChannelHandler.java#L45-L48
        eventHandlerContext.handle(optimizer.optimize(cloudEvents).toArray(new CloudRuntimeEvent[]{}));

        return true;
    }

}
