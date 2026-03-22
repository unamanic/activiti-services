package com.example.queryservice.services;

import com.example.queryservice.model.CleanupJobRequest;
import com.example.queryservice.model.JobExecutionEntry;
import com.example.queryservice.model.JobExecutionResponse;
import com.example.queryservice.model.ReplayJobRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class BatchJobService {

    private static final String REPLAY_JOB_NAME = "replay-query-process-instance-job";
    private static final String CLEANUP_JOB_NAME = "cleanup-query-process-instance-history-job";

    private final AtomicLong idSequence = new AtomicLong(1);
    private final ConcurrentHashMap<Long, JobExecutionEntry> executions = new ConcurrentHashMap<>();

    private final ReplayService replayService;
    private final CleanupService cleanupService;

    public BatchJobService(ReplayService replayService, CleanupService cleanupService) {
        this.replayService = replayService;
        this.cleanupService = cleanupService;
    }

    public JobExecutionResponse launchReplay(ReplayJobRequest request) {
        long execId = createEntry(REPLAY_JOB_NAME);

        List<String> ids = request.getProcessInstanceIds();
        if (request.getLimitSize() > 0) {
            ids = ids.stream().limit(request.getLimitSize()).collect(Collectors.toList());
        }

        List<CompletableFuture<Boolean>> futures = ids.stream()
                .map(replayService::replay)
                .collect(Collectors.toList());

        CompletableFuture<Void> allOf = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> complete(execId, "Replayed " + futures.size() + " process instance(s)"))
                .exceptionally(ex -> { fail(execId, ex.getMessage()); return null; });

        if (!request.isAsync()) {
            try {
                allOf.get();
            } catch (Exception e) {
                fail(execId, e.getMessage());
            }
        }

        return new JobExecutionResponse(executions.get(execId));
    }

    public JobExecutionResponse launchCleanup(CleanupJobRequest request) {
        long execId = createEntry(CLEANUP_JOB_NAME);

        Runnable work = () -> {
            try {
                int count = cleanupService.cleanup(request);
                complete(execId, "Cleaned up " + count + " process instance(s)");
            } catch (Exception e) {
                fail(execId, e.getMessage());
            }
        };

        if (request.isAsync()) {
            CompletableFuture.runAsync(work);
        } else {
            work.run();
        }

        return new JobExecutionResponse(executions.get(execId));
    }

    public JobExecutionResponse getExecution(long executionId) {
        JobExecutionEntry entry = executions.get(executionId);
        if (entry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No execution found with id " + executionId);
        }
        return new JobExecutionResponse(entry);
    }

    private long createEntry(String jobName) {
        long execId = idSequence.getAndIncrement();
        executions.put(execId, JobExecutionEntry.builder()
                .id(execId)
                .jobId(execId)
                .jobName(jobName)
                .startTime(Instant.now())
                .status("STARTING")
                .build());
        return execId;
    }

    private void complete(long execId, String exitDescription) {
        executions.computeIfPresent(execId, (k, e) -> JobExecutionEntry.builder()
                .id(e.getId()).jobId(e.getJobId()).jobName(e.getJobName())
                .startTime(e.getStartTime()).endTime(Instant.now())
                .exitDescription(exitDescription).status("COMPLETED")
                .build());
    }

    private void fail(long execId, String message) {
        executions.computeIfPresent(execId, (k, e) -> JobExecutionEntry.builder()
                .id(e.getId()).jobId(e.getJobId()).jobName(e.getJobName())
                .startTime(e.getStartTime()).endTime(Instant.now())
                .exitDescription(message).status("FAILED")
                .build());
    }
}
