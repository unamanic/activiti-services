package com.example.queryservice.services;

import com.example.queryservice.model.CleanupJobRequest;
import com.example.queryservice.model.JobExecutionResponse;
import com.example.queryservice.model.ReplayJobRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchJobServiceTest {

    @Mock ReplayService replayService;
    @Mock CleanupService cleanupService;

    @InjectMocks BatchJobService batchJobService;

    // --- replay ---

    @Test
    void launchReplay_sync_completesAndReturnsCOMPLETED() {
        when(replayService.replay(anyString())).thenReturn(CompletableFuture.completedFuture(true));

        JobExecutionResponse response = batchJobService.launchReplay(
                new ReplayJobRequest(List.of("proc-1", "proc-2"), 0, false));

        assertThat(response.getEntry().getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getEntry().getJobName()).isEqualTo("replay-query-process-instance-job");
        verify(replayService).replay("proc-1");
        verify(replayService).replay("proc-2");
    }

    @Test
    void launchReplay_async_returnsStartingImmediately() {
        when(replayService.replay(anyString())).thenReturn(new CompletableFuture<>());

        JobExecutionResponse response = batchJobService.launchReplay(
                new ReplayJobRequest(List.of("proc-1"), 0, true));

        assertThat(response.getEntry().getStatus()).isEqualTo("STARTING");
    }

    @Test
    void launchReplay_respectsLimitSize() {
        when(replayService.replay(anyString())).thenReturn(CompletableFuture.completedFuture(true));

        batchJobService.launchReplay(new ReplayJobRequest(List.of("p1", "p2", "p3"), 2, false));

        verify(replayService, times(2)).replay(anyString());
    }

    // --- cleanup ---

    @Test
    void launchCleanup_sync_completesAndReturnsCOMPLETED() {
        when(cleanupService.cleanup(any())).thenReturn(5);

        JobExecutionResponse response = batchJobService.launchCleanup(
                new CleanupJobRequest(30, null, 0, false));

        assertThat(response.getEntry().getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getEntry().getExitDescription()).contains("5");
        assertThat(response.getEntry().getJobName()).isEqualTo("cleanup-query-process-instance-history-job");
    }

    @Test
    void launchCleanup_sync_returnsFAILED_onException() {
        when(cleanupService.cleanup(any())).thenThrow(new RuntimeException("DB error"));

        JobExecutionResponse response = batchJobService.launchCleanup(
                new CleanupJobRequest(30, null, 0, false));

        assertThat(response.getEntry().getStatus()).isEqualTo("FAILED");
        assertThat(response.getEntry().getExitDescription()).contains("DB error");
    }

    @Test
    void launchCleanup_async_returnsStartingWithNullEndTime() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        when(cleanupService.cleanup(any())).thenAnswer(inv -> {
            latch.await(); // hold the async thread until assertions are done
            return 1;
        });

        JobExecutionResponse response = batchJobService.launchCleanup(
                new CleanupJobRequest(30, null, 0, true));
        try {
            assertThat(response.getEntry().getStatus()).isEqualTo("STARTING");
            assertThat(response.getEntry().getEndTime()).isNull();
        } finally {
            latch.countDown();
        }
    }

    @Test
    void multipleExecutions_haveUniqueIds() {
        when(replayService.replay(anyString())).thenReturn(CompletableFuture.completedFuture(true));
        when(cleanupService.cleanup(any())).thenReturn(0);

        long id1 = batchJobService.launchReplay(new ReplayJobRequest(List.of("p1"), 0, false)).getEntry().getId();
        long id2 = batchJobService.launchCleanup(new CleanupJobRequest(30, null, 0, false)).getEntry().getId();
        long id3 = batchJobService.launchReplay(new ReplayJobRequest(List.of("p2"), 0, false)).getEntry().getId();

        assertThat(List.of(id1, id2, id3)).doesNotHaveDuplicates();
    }

    @Test
    void completedEntry_hasNonNullTimestamps() {
        when(replayService.replay(anyString())).thenReturn(CompletableFuture.completedFuture(true));

        JobExecutionResponse response = batchJobService.launchReplay(
                new ReplayJobRequest(List.of("proc-1"), 0, false));

        assertThat(response.getEntry().getStartTime()).isNotNull();
        assertThat(response.getEntry().getEndTime()).isNotNull();
    }

    @Test
    void launchReplay_withEmptyList_completesImmediately() {
        JobExecutionResponse response = batchJobService.launchReplay(
                new ReplayJobRequest(List.of(), 0, false));

        assertThat(response.getEntry().getStatus()).isEqualTo("COMPLETED");
        verifyNoInteractions(replayService);
    }

    // --- log ---

    @Test
    void getExecution_returnsEntry_whenExists() {
        when(replayService.replay(anyString())).thenReturn(CompletableFuture.completedFuture(true));
        JobExecutionResponse launched = batchJobService.launchReplay(
                new ReplayJobRequest(List.of("proc-1"), 0, false));

        long execId = launched.getEntry().getId();
        JobExecutionResponse fetched = batchJobService.getExecution(execId);

        assertThat(fetched.getEntry().getId()).isEqualTo(execId);
    }

    @Test
    void getExecution_throws404_whenNotFound() {
        assertThatThrownBy(() -> batchJobService.getExecution(9999L))
                .isInstanceOf(ResponseStatusException.class);
    }
}
