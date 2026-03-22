package com.example.queryservice.services;

import com.example.queryservice.model.CleanupJobRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.activiti.cloud.services.query.model.ProcessInstanceEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class CleanupService {

    private final EntityManager entityManager;
    private final PurgeService purgeService;

    public CleanupService(EntityManager entityManager, PurgeService purgeService) {
        this.entityManager = entityManager;
        this.purgeService = purgeService;
    }

    @Transactional
    public int cleanup(CleanupJobRequest request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProcessInstanceEntity> query = cb.createQuery(ProcessInstanceEntity.class);
        Root<ProcessInstanceEntity> root = query.from(ProcessInstanceEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        if (request.getHistoricRetentionDays() > 0) {
            Date cutoff = new Date(System.currentTimeMillis() -
                    (long) request.getHistoricRetentionDays() * 24 * 60 * 60 * 1000L);
            predicates.add(cb.lessThan(root.get("startDate"), cutoff));
        }

        if (request.getProcessDefinitionKeys() != null && !request.getProcessDefinitionKeys().isEmpty()) {
            predicates.add(root.get("processDefinitionKey").in(request.getProcessDefinitionKeys()));
        }

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        TypedQuery<ProcessInstanceEntity> typedQuery = entityManager.createQuery(query);

        if (request.getLimitSize() > 0) {
            typedQuery.setMaxResults(request.getLimitSize());
        }

        List<ProcessInstanceEntity> instances = typedQuery.getResultList();
        instances.forEach(pi -> purgeService.purgeByProcessInstanceId(pi.getId()));
        return instances.size();
    }
}
