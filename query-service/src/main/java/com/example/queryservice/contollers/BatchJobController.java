package com.example.queryservice.contollers;

import com.example.queryservice.model.CleanupJobRequest;
import com.example.queryservice.model.JobExecutionResponse;
import com.example.queryservice.model.ReplayJobRequest;
import com.example.queryservice.services.BatchJobService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/v1/batch/jobs/executions")
public class BatchJobController {

    private final BatchJobService batchJobService;

    public BatchJobController(BatchJobService batchJobService) {
        this.batchJobService = batchJobService;
    }

    @PreAuthorize("hasRole('ACTIVITI_ADMIN')")
    @PostMapping("/replay-query-process-instance-job")
    public JobExecutionResponse launchReplay(@RequestBody ReplayJobRequest request) {
        return batchJobService.launchReplay(request);
    }

    @PreAuthorize("hasRole('ACTIVITI_ADMIN')")
    @PostMapping("/cleanup-query-process-instance-history-job")
    public JobExecutionResponse launchCleanup(@RequestBody CleanupJobRequest request) {
        return batchJobService.launchCleanup(request);
    }

    @PreAuthorize("hasRole('ACTIVITI_ADMIN')")
    @GetMapping("/{executionId}/log")
    public JobExecutionResponse getExecutionLog(@PathVariable long executionId) {
        return batchJobService.getExecution(executionId);
    }
}
