package com.example.queryservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplayJobRequest {
    List<String> processInstanceIds;
    int limitSize;
    boolean async;
}
