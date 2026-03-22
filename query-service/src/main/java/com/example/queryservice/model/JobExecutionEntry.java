package com.example.queryservice.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class JobExecutionEntry {
    Long id;
    Long jobId;
    String jobName;
    Instant startTime;
    Instant endTime;
    String exitDescription;
    String status;
}
