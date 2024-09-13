package com.example.queryservice.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReplayResponse {
    String processInstanceId;
    Boolean success;
    String message;
}
