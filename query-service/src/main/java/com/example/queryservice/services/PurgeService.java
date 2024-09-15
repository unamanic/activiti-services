package com.example.queryservice.services;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Visitor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.activiti.cloud.services.query.app.repository.*;
import org.activiti.cloud.services.query.model.*;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PurgeService {

    private final EntityManager entityManager;

    private final ProcessInstanceRepository processInstanceRepository;

    public PurgeService(EntityManager entityManager, BPMNActivityRepository bpmnActivityRepository, BPMNSequenceFlowRepository bpmnSequenceFlowRepository, IntegrationContextRepository integrationContextRepository, ProcessInstanceRepository processInstanceRepository, TaskRepository taskRepository, TaskCandidateGroupRepository taskCandidateGroupRepository, TaskCandidateGroupRepository taskCandidateUserRepository, TaskVariableRepository taskVariableRepository) {
        this.entityManager = entityManager;
        this.processInstanceRepository = processInstanceRepository;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void purgeByProcessInstanceId(String processInstanceId) {
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        deleteIntegrationContexts(processInstanceId, criteriaBuilder);
        deleteSequenceFlows(processInstanceId, criteriaBuilder);
        deleteBPMNActivities(processInstanceId, criteriaBuilder);
        deleteTasks(processInstanceId, criteriaBuilder);
        deleteTaskVariables(processInstanceId, criteriaBuilder);
        deleteProcessVariables(processInstanceId, criteriaBuilder);

        deleteProcessInstance(processInstanceId, criteriaBuilder);
    }

    private void deleteProcessInstance(String processInstanceId, CriteriaBuilder criteriaBuilder) {
        CriteriaDelete<ProcessInstanceEntity> processInstanceEntityCriteriaDelete = criteriaBuilder.createCriteriaDelete(ProcessInstanceEntity.class);
        Root<ProcessInstanceEntity> processInstanceEntityRoot = processInstanceEntityCriteriaDelete.from(ProcessInstanceEntity.class);
        processInstanceEntityCriteriaDelete.where(criteriaBuilder.equal(processInstanceEntityRoot.get("id"), processInstanceId));
        entityManager.createQuery(processInstanceEntityCriteriaDelete).executeUpdate();
    }

    private void deleteTaskVariables(String processInstanceId, CriteriaBuilder criteriaBuilder) {
        CriteriaDelete<TaskVariableEntity> taskVariableEntityCriteriaDelete = criteriaBuilder.createCriteriaDelete(TaskVariableEntity.class);
        Root<TaskVariableEntity> taskVariableEntityRoot = taskVariableEntityCriteriaDelete.from(TaskVariableEntity.class);
        taskVariableEntityCriteriaDelete.where(criteriaBuilder.equal(taskVariableEntityRoot.get("processInstanceId"), processInstanceId));
        entityManager.createQuery(taskVariableEntityCriteriaDelete).executeUpdate();
    }

    private void deleteProcessVariables(String processInstanceId, CriteriaBuilder criteriaBuilder) {
        CriteriaDelete<ProcessVariableEntity> processVariableEntityCriteriaDelete = criteriaBuilder.createCriteriaDelete(ProcessVariableEntity.class);
        Root<ProcessVariableEntity> processVariableEntityRoot = processVariableEntityCriteriaDelete.from(ProcessVariableEntity.class);
        processVariableEntityCriteriaDelete.where(criteriaBuilder.equal(processVariableEntityRoot.get("processInstanceId"), processInstanceId));
        entityManager.createQuery(processVariableEntityCriteriaDelete).executeUpdate();
    }

    private void deleteTasks(String processInstanceId, CriteriaBuilder criteriaBuilder) {
        CriteriaQuery<TaskEntity> taskEntityCriteria = criteriaBuilder.createQuery(TaskEntity.class);
        Root<TaskEntity> taskEntityRoot = taskEntityCriteria.from(TaskEntity.class);
        taskEntityCriteria.where(criteriaBuilder.equal(taskEntityRoot.get("processInstanceId"), processInstanceId));
        List<TaskEntity> taskEntities = entityManager.createQuery(taskEntityCriteria).getResultList();

        List<String> taskIds = taskEntities.stream().map(TaskEntity::getId).collect(Collectors.toUnmodifiableList());

        CriteriaDelete<TaskCandidateGroupEntity> taskCandidateGroupEntityCriteriaDelete = criteriaBuilder.createCriteriaDelete(TaskCandidateGroupEntity.class);
        Root<TaskCandidateGroupEntity> taskCandidateGroupEntityRoot = taskCandidateGroupEntityCriteriaDelete.from(TaskCandidateGroupEntity.class);
        CriteriaBuilder.In<String> taskCandidateGroupInClause = criteriaBuilder.in(taskCandidateGroupEntityRoot.get("taskId"));
        taskIds.forEach(taskCandidateGroupInClause::value);
        entityManager.createQuery(taskCandidateGroupEntityCriteriaDelete).executeUpdate();

        CriteriaDelete<TaskCandidateUserEntity> taskCandidateUserEntityCriteriaDelete = criteriaBuilder.createCriteriaDelete(TaskCandidateUserEntity.class);
        Root<TaskCandidateUserEntity> taskCandidateUserEntityRoot = taskCandidateUserEntityCriteriaDelete.from(TaskCandidateUserEntity.class);
        CriteriaBuilder.In<String> taskCandidateUserInClause = criteriaBuilder.in(taskCandidateUserEntityRoot.get("taskId"));
        taskIds.forEach(taskCandidateUserInClause::value);
        entityManager.createQuery(taskCandidateUserEntityCriteriaDelete).executeUpdate();

        taskEntities.forEach(entityManager::remove);
    }

    private void deleteBPMNActivities(String processInstanceId, CriteriaBuilder criteriaBuilder) {
        CriteriaDelete<BPMNActivityEntity> bpmnActivityEntityCriteriaDelete = criteriaBuilder.createCriteriaDelete(BPMNActivityEntity.class);
        Root<BPMNActivityEntity> bpmnActivityEntityRoot = bpmnActivityEntityCriteriaDelete.from(BPMNActivityEntity.class);
        bpmnActivityEntityCriteriaDelete.where(criteriaBuilder.equal(bpmnActivityEntityRoot.get("processInstanceId"), processInstanceId));
        entityManager.createQuery(bpmnActivityEntityCriteriaDelete).executeUpdate();
    }

    private void deleteSequenceFlows(String processInstanceId, CriteriaBuilder criteriaBuilder) {
        CriteriaDelete<BPMNSequenceFlowEntity> bpmnSequenceFlowEntityCriteriaDelete = criteriaBuilder.createCriteriaDelete(BPMNSequenceFlowEntity.class);
        Root<BPMNSequenceFlowEntity> bpmnSequenceFlowEntityRoot = bpmnSequenceFlowEntityCriteriaDelete.from(BPMNSequenceFlowEntity.class);
        bpmnSequenceFlowEntityCriteriaDelete.where(criteriaBuilder.equal(bpmnSequenceFlowEntityRoot.get("processInstanceId"), processInstanceId));
        entityManager.createQuery(bpmnSequenceFlowEntityCriteriaDelete).executeUpdate();
    }

    private void deleteIntegrationContexts(String processInstanceId, CriteriaBuilder criteriaBuilder) {
        CriteriaDelete<IntegrationContextEntity> integrationContextEntityCriteriaDelete = criteriaBuilder.createCriteriaDelete(IntegrationContextEntity.class);
        Root<IntegrationContextEntity> integrationContextEntityRoot = integrationContextEntityCriteriaDelete.from(IntegrationContextEntity.class);
        integrationContextEntityCriteriaDelete.where(criteriaBuilder.equal(integrationContextEntityRoot.get("processInstanceId"), processInstanceId));
        entityManager.createQuery(integrationContextEntityCriteriaDelete).executeUpdate();
    }
}
