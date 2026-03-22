package com.example.queryservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CleanupJobRequest {
    int historicRetentionDays;
    List<String> processDefinitionKeys;
    int limitSize;
    boolean async;
}
