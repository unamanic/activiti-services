package com.example.queryservice.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.activiti.cloud.services.query.app.repository.*;
import org.activiti.cloud.services.query.model.TaskEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurgeServiceTest {

    @Mock EntityManager entityManager;
    @Mock CriteriaBuilder criteriaBuilder;
    @Mock @SuppressWarnings("rawtypes") CriteriaDelete criteriaDelete;
    @Mock @SuppressWarnings("rawtypes") CriteriaQuery taskCriteriaQuery;
    @Mock @SuppressWarnings("rawtypes") Root root;
    @Mock Predicate predicate;
    @Mock jakarta.persistence.Query deleteQuery;
    @Mock @SuppressWarnings("rawtypes") TypedQuery selectQuery;
    @Mock @SuppressWarnings("rawtypes") CriteriaBuilder.In inClause;

    PurgeService purgeService;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        purgeService = new PurgeService(
            entityManager,
            mock(BPMNActivityRepository.class),
            mock(BPMNSequenceFlowRepository.class),
            mock(IntegrationContextRepository.class),
            mock(ProcessInstanceRepository.class),
            mock(TaskRepository.class),
            mock(TaskCandidateGroupRepository.class),
            mock(TaskCandidateGroupRepository.class),
            mock(TaskVariableRepository.class)
        );

        lenient().when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        lenient().when(criteriaBuilder.createCriteriaDelete(any())).thenReturn(criteriaDelete);
        lenient().when(criteriaBuilder.createQuery(TaskEntity.class)).thenReturn(taskCriteriaQuery);
        lenient().when(criteriaDelete.from(any(Class.class))).thenReturn(root);
        lenient().when(taskCriteriaQuery.from(TaskEntity.class)).thenReturn(root);
        lenient().when(root.get(anyString())).thenReturn(mock(Path.class));
        lenient().when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        lenient().when(criteriaBuilder.in(any())).thenReturn(inClause);
        lenient().when(inClause.value(any())).thenReturn(inClause);
        lenient().when(criteriaDelete.where(any(Predicate.class))).thenReturn(criteriaDelete);
        lenient().when(taskCriteriaQuery.where(any(Predicate.class))).thenReturn(taskCriteriaQuery);
        lenient().when(entityManager.createQuery(any(CriteriaDelete.class))).thenReturn(deleteQuery);
        lenient().when(entityManager.createQuery(taskCriteriaQuery)).thenReturn(selectQuery);
        lenient().when(selectQuery.getResultList()).thenReturn(List.of());
        lenient().when(deleteQuery.executeUpdate()).thenReturn(1);
    }

    @Test
    void purgeByProcessInstanceId_executesDeletesForAllTables_whenNoTasksExist() {
        purgeService.purgeByProcessInstanceId("proc-1");

        // integrationContexts + sequenceFlows + bpmnActivities
        // + taskCandidateGroups + taskCandidateUsers
        // + taskVariables + processVariables + processInstance = 8
        verify(deleteQuery, times(8)).executeUpdate();
        verify(entityManager, never()).remove(any());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void purgeByProcessInstanceId_removesEachTaskEntity_whenTasksExist() {
        TaskEntity task1 = mock(TaskEntity.class);
        TaskEntity task2 = mock(TaskEntity.class);
        when(task1.getId()).thenReturn("task-1");
        when(task2.getId()).thenReturn("task-2");
        when(selectQuery.getResultList()).thenReturn(List.of(task1, task2));

        purgeService.purgeByProcessInstanceId("proc-1");

        verify(entityManager).remove(task1);
        verify(entityManager).remove(task2);
    }

    @Test
    void purgeByProcessInstanceId_targetsCorrectProcessInstance() {
        purgeService.purgeByProcessInstanceId("proc-42");

        // The process instance ID is passed to criteriaBuilder.equal() for each delete
        verify(criteriaBuilder, atLeastOnce()).equal(any(), eq("proc-42"));
    }
}
