package com.example.queryservice.services;

import com.example.queryservice.model.CleanupJobRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.activiti.cloud.services.query.model.ProcessInstanceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CleanupServiceTest {

    @Mock EntityManager entityManager;
    @Mock PurgeService purgeService;
    @Mock CriteriaBuilder criteriaBuilder;
    @Mock @SuppressWarnings("rawtypes") CriteriaQuery criteriaQuery;
    @Mock @SuppressWarnings("rawtypes") Root root;
    @Mock @SuppressWarnings("rawtypes") Path path;
    @Mock Predicate predicate;
    @Mock @SuppressWarnings("rawtypes") TypedQuery typedQuery;

    CleanupService cleanupService;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        cleanupService = new CleanupService(entityManager, purgeService);
        lenient().when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        lenient().when(criteriaBuilder.createQuery(ProcessInstanceEntity.class)).thenReturn(criteriaQuery);
        lenient().when(criteriaQuery.from(ProcessInstanceEntity.class)).thenReturn(root);
        lenient().when(root.get(anyString())).thenReturn(path);
        lenient().when(criteriaBuilder.lessThan(any(), any(java.util.Date.class))).thenReturn(predicate);
        lenient().when(path.in(any(java.util.Collection.class))).thenReturn(predicate);
        lenient().when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(criteriaQuery.where(any(Predicate.class))).thenReturn(criteriaQuery);
        lenient().when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        lenient().when(typedQuery.getResultList()).thenReturn(List.of());
    }

    @Test
    void cleanup_returnsZero_whenNoInstancesFound() {
        CleanupJobRequest request = new CleanupJobRequest(30, null, 0, false);

        int result = cleanupService.cleanup(request);

        assertThat(result).isZero();
        verifyNoInteractions(purgeService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void cleanup_purgesEachFoundInstance() {
        ProcessInstanceEntity pi1 = mock(ProcessInstanceEntity.class);
        ProcessInstanceEntity pi2 = mock(ProcessInstanceEntity.class);
        when(pi1.getId()).thenReturn("proc-1");
        when(pi2.getId()).thenReturn("proc-2");
        when(typedQuery.getResultList()).thenReturn(List.of(pi1, pi2));

        int result = cleanupService.cleanup(new CleanupJobRequest(30, null, 0, false));

        assertThat(result).isEqualTo(2);
        verify(purgeService).purgeByProcessInstanceId("proc-1");
        verify(purgeService).purgeByProcessInstanceId("proc-2");
    }

    @Test
    @SuppressWarnings("unchecked")
    void cleanup_appliesLimitSize() {
        CleanupJobRequest request = new CleanupJobRequest(30, null, 10, false);

        cleanupService.cleanup(request);

        verify(typedQuery).setMaxResults(10);
    }

    @Test
    void cleanup_doesNotSetMaxResults_whenLimitSizeIsZero() {
        CleanupJobRequest request = new CleanupJobRequest(30, null, 0, false);

        cleanupService.cleanup(request);

        verify(typedQuery, never()).setMaxResults(anyInt());
    }

    @Test
    @SuppressWarnings("unchecked")
    void cleanup_appliesProcessDefinitionKeyFilter_whenKeysProvided() {
        CleanupJobRequest request = new CleanupJobRequest(0, List.of("proc-def-1", "proc-def-2"), 0, false);

        cleanupService.cleanup(request);

        verify(path).in(List.of("proc-def-1", "proc-def-2"));
    }

    @Test
    void cleanup_doesNotApplyDateFilter_whenRetentionDaysIsZero() {
        CleanupJobRequest request = new CleanupJobRequest(0, null, 0, false);

        cleanupService.cleanup(request);

        verify(criteriaBuilder, never()).lessThan(any(), any(java.util.Date.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void cleanup_appliesBothFilters_whenBothProvided() {
        CleanupJobRequest request = new CleanupJobRequest(30, List.of("myProc"), 0, false);

        cleanupService.cleanup(request);

        verify(criteriaBuilder).lessThan(any(), any(java.util.Date.class));
        verify(path).in(List.of("myProc"));
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    void cleanup_cutoffDate_isInThePast() {
        CleanupJobRequest request = new CleanupJobRequest(30, null, 0, false);
        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);

        cleanupService.cleanup(request);

        verify(criteriaBuilder).lessThan(any(), dateCaptor.capture());
        assertThat(dateCaptor.getValue()).isBefore(new Date());
    }

    @Test
    void cleanup_doesNotApplyWhereClause_whenNoFilters() {
        CleanupJobRequest request = new CleanupJobRequest(0, null, 0, false);

        cleanupService.cleanup(request);

        verify(criteriaQuery, never()).where(any(Predicate.class));
    }
}
