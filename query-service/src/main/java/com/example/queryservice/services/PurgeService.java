package com.example.queryservice.services;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Visitor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import org.activiti.cloud.services.query.app.repository.*;
import org.activiti.cloud.services.query.model.*;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class PurgeService {

    private final EntityManager entityManager;

    private final ProcessInstanceRepository processInstanceRepository;

    public PurgeService(EntityManager entityManager, BPMNActivityRepository bpmnActivityRepository, BPMNSequenceFlowRepository bpmnSequenceFlowRepository, IntegrationContextRepository integrationContextRepository, ProcessInstanceRepository processInstanceRepository, TaskRepository taskRepository, TaskCandidateGroupRepository taskCandidateGroupRepository, TaskCandidateGroupRepository taskCandidateUserRepository, TaskVariableRepository taskVariableRepository) {
        this.entityManager = entityManager;
        this.processInstanceRepository = processInstanceRepository;
    }

    public void purgeByProcessInstanceId(String processInstanceId) {
        var criteriaBuilder = entityManager.getCriteriaBuilder();

        // Deleting all entries in process instance repository with the specified process instance ID
        processInstanceRepository.deleteById(processInstanceId);

        CriteriaDelete<IntegrationContextEntity> integrationContextEntityCriteriaDelete = criteriaBuilder.createCriteriaDelete(IntegrationContextEntity.class);
        Root<IntegrationContextEntity> integrationContextEntityRoot = integrationContextEntityCriteriaDelete.from(IntegrationContextEntity.class);
        criteriaBuilder.equal(integrationContextEntityRoot.get("processInstanceId"), processInstanceId);
        entityManager.createQuery(integrationContextEntityCriteriaDelete).executeUpdate();

        CriteriaDelete<BPMNSequenceFlowEntity> bpmnSequenceFlowEntityCriteriaDelete = criteriaBuilder.createCriteriaDelete(BPMNSequenceFlowEntity.class);
        Root<BPMNSequenceFlowEntity> bpmnSequenceFlowEntityRoot = bpmnSequenceFlowEntityCriteriaDelete.from(BPMNSequenceFlowEntity.class);
        criteriaBuilder.equal(bpmnSequenceFlowEntityRoot.get("processInstanceId"), processInstanceId);
        entityManager.createQuery(bpmnSequenceFlowEntityCriteriaDelete).executeUpdate();

        CriteriaDelete<BPMNActivityEntity> bpmnActivityEntityCriteriaDelete = criteriaBuilder.createCriteriaDelete(BPMNActivityEntity.class);
        Root<BPMNActivityEntity> bpmnActivityEntityRoot = bpmnActivityEntityCriteriaDelete.from(BPMNActivityEntity.class);
        criteriaBuilder.equal(bpmnActivityEntityRoot.get("processInstanceId"), processInstanceId);
        entityManager.createQuery(bpmnActivityEntityCriteriaDelete).executeUpdate();

        CriteriaDelete<TaskEntity> taskEntityCriteriaDelete = criteriaBuilder.createCriteriaDelete(TaskEntity.class);
        Root<TaskEntity> taskEntityRoot = taskEntityCriteriaDelete.from(TaskEntity.class);
        criteriaBuilder.equal(taskEntityRoot.get("processInstanceId"), processInstanceId);
        entityManager.createQuery(taskEntityCriteriaDelete).executeUpdate();

        CriteriaDelete<TaskVariableEntity> taskVariableEntityCriteriaDelete = criteriaBuilder.createCriteriaDelete(TaskVariableEntity.class);
        Root<TaskVariableEntity> taskVariableEntityRoot = taskVariableEntityCriteriaDelete.from(TaskVariableEntity.class);
        criteriaBuilder.equal(taskVariableEntityRoot.get("processInstanceId"), processInstanceId);
        entityManager.createQuery(taskVariableEntityCriteriaDelete).executeUpdate();
    }
}
