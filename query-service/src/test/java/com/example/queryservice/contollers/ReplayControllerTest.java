package com.example.queryservice.contollers;

import com.example.queryservice.services.ReplayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReplayControllerTest {

    @Mock ReplayService replayService;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ReplayController(replayService)).build();
        when(replayService.replay(ArgumentMatchers.anyString()))
                .thenReturn(CompletableFuture.completedFuture(true));
    }

    @Test
    void replay_returns202Accepted() throws Exception {
        mockMvc.perform(get("/admin/v1/replay/proc-1"))
               .andExpect(status().isAccepted());
    }

    @Test
    void replay_returnsProcessInstanceIdAndMessage() throws Exception {
        mockMvc.perform(get("/admin/v1/replay/proc-1"))
               .andExpect(jsonPath("$.processInstanceId").value("proc-1"))
               .andExpect(jsonPath("$.message").value("Replay requested"));
    }

    @Test
    void replay_delegatesToService() throws Exception {
        mockMvc.perform(get("/admin/v1/replay/proc-42"));
        verify(replayService).replay("proc-42");
    }
}
