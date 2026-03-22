package com.example.queryservice.contollers;

import com.example.queryservice.model.JobExecutionEntry;
import com.example.queryservice.model.JobExecutionResponse;
import com.example.queryservice.services.BatchJobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BatchJobControllerTest {

    @Mock BatchJobService batchJobService;
    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new BatchJobController(batchJobService)).build();
    }

    private JobExecutionResponse completedResponse(String jobName) {
        return new JobExecutionResponse(JobExecutionEntry.builder()
                .id(1L).jobId(1L).jobName(jobName)
                .startTime(Instant.now()).endTime(Instant.now())
                .exitDescription("Done").status("COMPLETED")
                .build());
    }

    @Test
    void launchReplay_returns200WithEntry() throws Exception {
        when(batchJobService.launchReplay(any()))
                .thenReturn(completedResponse("replay-query-process-instance-job"));

        mockMvc.perform(post("/admin/v1/batch/jobs/executions/replay-query-process-instance-job")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("processInstanceIds", new String[]{"p1"}, "limitSize", 0, "async", false))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.entry.jobName").value("replay-query-process-instance-job"))
               .andExpect(jsonPath("$.entry.status").value("COMPLETED"));
    }

    @Test
    void launchCleanup_returns200WithEntry() throws Exception {
        when(batchJobService.launchCleanup(any()))
                .thenReturn(completedResponse("cleanup-query-process-instance-history-job"));

        mockMvc.perform(post("/admin/v1/batch/jobs/executions/cleanup-query-process-instance-history-job")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("historicRetentionDays", 30, "limitSize", 0, "async", false))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.entry.jobName").value("cleanup-query-process-instance-history-job"))
               .andExpect(jsonPath("$.entry.status").value("COMPLETED"));
    }

    @Test
    void getExecutionLog_returns200WithEntry() throws Exception {
        when(batchJobService.getExecution(42L))
                .thenReturn(completedResponse("replay-query-process-instance-job"));

        mockMvc.perform(get("/admin/v1/batch/jobs/executions/42/log"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.entry.status").value("COMPLETED"));
    }

    @Test
    void getExecutionLog_returns404_whenNotFound() throws Exception {
        when(batchJobService.getExecution(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));

        mockMvc.perform(get("/admin/v1/batch/jobs/executions/99/log"))
               .andExpect(status().isNotFound());
    }

    @Test
    void launchReplay_returnsStarting_whenAsyncTrue() throws Exception {
        when(batchJobService.launchReplay(any())).thenReturn(
                new JobExecutionResponse(JobExecutionEntry.builder()
                        .id(2L).jobId(2L).jobName("replay-query-process-instance-job")
                        .startTime(Instant.now()).status("STARTING").build()));

        mockMvc.perform(post("/admin/v1/batch/jobs/executions/replay-query-process-instance-job")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("processInstanceIds", new String[]{"p1"}, "limitSize", 0, "async", true))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.entry.status").value("STARTING"))
               .andExpect(jsonPath("$.entry.endTime").doesNotExist());
    }

    @Test
    void launchCleanup_returnsStarting_whenAsyncTrue() throws Exception {
        when(batchJobService.launchCleanup(any())).thenReturn(
                new JobExecutionResponse(JobExecutionEntry.builder()
                        .id(3L).jobId(3L).jobName("cleanup-query-process-instance-history-job")
                        .startTime(Instant.now()).status("STARTING").build()));

        mockMvc.perform(post("/admin/v1/batch/jobs/executions/cleanup-query-process-instance-history-job")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("historicRetentionDays", 30, "limitSize", 0, "async", true))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.entry.status").value("STARTING"));
    }

    @Test
    void launchReplay_deserializesRequestBody_correctly() throws Exception {
        when(batchJobService.launchReplay(any())).thenReturn(completedResponse("replay-query-process-instance-job"));

        mockMvc.perform(post("/admin/v1/batch/jobs/executions/replay-query-process-instance-job")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"processInstanceIds\":[\"proc-1\",\"proc-2\"],\"limitSize\":5,\"async\":false}"));

        verify(batchJobService).launchReplay(argThat(req ->
                req.getProcessInstanceIds().equals(List.of("proc-1", "proc-2"))
                && req.getLimitSize() == 5
                && !req.isAsync()));
    }

    @Test
    void launchCleanup_deserializesRequestBody_correctly() throws Exception {
        when(batchJobService.launchCleanup(any())).thenReturn(completedResponse("cleanup-query-process-instance-history-job"));

        mockMvc.perform(post("/admin/v1/batch/jobs/executions/cleanup-query-process-instance-history-job")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"historicRetentionDays\":90,\"processDefinitionKeys\":[\"myProc\"],\"limitSize\":100,\"async\":true}"));

        verify(batchJobService).launchCleanup(argThat(req ->
                req.getHistoricRetentionDays() == 90
                && req.getProcessDefinitionKeys().equals(List.of("myProc"))
                && req.getLimitSize() == 100
                && req.isAsync()));
    }

    @Test
    void launchReplay_responseContainsAllEntryFields() throws Exception {
        Instant start = Instant.parse("2026-01-01T10:00:00Z");
        Instant end   = Instant.parse("2026-01-01T10:01:00Z");
        when(batchJobService.launchReplay(any())).thenReturn(
                new JobExecutionResponse(JobExecutionEntry.builder()
                        .id(7L).jobId(7L).jobName("replay-query-process-instance-job")
                        .startTime(start).endTime(end)
                        .exitDescription("Replayed 1 process instance(s)").status("COMPLETED")
                        .build()));

        mockMvc.perform(post("/admin/v1/batch/jobs/executions/replay-query-process-instance-job")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"processInstanceIds\":[\"p1\"],\"limitSize\":0,\"async\":false}"))
               .andExpect(jsonPath("$.entry.id").value(7))
               .andExpect(jsonPath("$.entry.jobId").value(7))
               .andExpect(jsonPath("$.entry.startTime").isNotEmpty())
               .andExpect(jsonPath("$.entry.endTime").isNotEmpty())
               .andExpect(jsonPath("$.entry.exitDescription").value("Replayed 1 process instance(s)"));
    }
}
