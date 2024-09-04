package com.example.queryservice.services;

import jakarta.persistence.EntityManager;
import org.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;
import org.activiti.cloud.api.model.shared.events.CloudVariableUpdatedEvent;
import org.activiti.cloud.api.model.shared.impl.events.CloudVariableUpdatedEventImpl;
import org.activiti.cloud.services.audit.jpa.events.AuditEventEntity;
import org.activiti.cloud.services.audit.jpa.repository.EventsRepository;
import org.activiti.cloud.services.query.events.handlers.QueryEventHandlerContext;
import org.activiti.cloud.services.query.events.handlers.QueryEventHandlerContextOptimizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReplayService {

    private final EventsRepository<AuditEventEntity> auditEventRepository;

    private final QueryEventHandlerContext eventHandlerContext;
    private final QueryEventHandlerContextOptimizer optimizer;
    private final EntityManager entityManager;

    public ReplayService(EventsRepository<AuditEventEntity> auditEventRepository, QueryEventHandlerContext eventHandlerContext, QueryEventHandlerContextOptimizer optimizer, EntityManager entityManager) {
        this.auditEventRepository = auditEventRepository;
        this.eventHandlerContext = eventHandlerContext;
        this.optimizer = optimizer;
        this.entityManager = entityManager;
    }

    public boolean replay(String id) {
        // find all audit events for a processInstance
        List<AuditEventEntity> auditEvents = auditEventRepository.findAll(((root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("processInstanceId"), id);
        }));

        // transform from audit events to cloud Runtime events
        List<CloudRuntimeEvent<?, ?>> cloudEvents = auditEvents.stream()
                // do something real here, create one for each type that can be converted to an audit message:
                // https://github.com/Activiti/activiti-cloud/tree/afa337cc8ed9ea847b65fd8270b80b5473266a91/activiti-cloud-audit-service/activiti-cloud-services-audit-model/src/main/java/org/activiti/cloud/services/audit/jpa/converters
                //
                // The runtime event implemenations can be found in this repository:
                // https://github.com/Activiti/Activiti/blob/ecec52d763b4ce0fe0362a0f3996b28bd877c318/activiti-api/activiti-api-model-shared/src/main/java/org/activiti/api/model/shared/event/RuntimeEvent.java
                .map(auditEventEntity -> new CloudVariableUpdatedEventImpl<String>())
                .collect(Collectors.toUnmodifiableList());


        // purge process from query service
        // I don't know if there is a method to cleanly purge
        // worst case, go through each of these repositories and delete anything associated with the process instance id
        // https://github.com/Activiti/activiti-cloud/tree/bec91b5a92319033f4f4d621d9f0123f3df641a9/activiti-cloud-query-service/activiti-cloud-services-query/activiti-cloud-services-query-repo/src/main/java/org/activiti/cloud/services/query/app/repository


        //reload query service with audit events
        // https://github.com/Activiti/activiti-cloud/blob/afa337cc8ed9ea847b65fd8270b80b5473266a91/activiti-cloud-query-service/activiti-cloud-services-query/activiti-cloud-services-query-events/src/main/java/org/activiti/cloud/services/query/app/QueryConsumerChannelHandler.java#L45-L48
        eventHandlerContext.handle(optimizer.optimize(cloudEvents).toArray(new CloudRuntimeEvent[] {}));

        return true;
    }
}
