package com.example.queryservice.contollers;

import com.example.queryservice.services.ReplayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
    }

    @Test
    void replay_returnsSuccessTrue_whenServiceReturnsTrue() throws Exception {
        when(replayService.replay("proc-1")).thenReturn(true);

        mockMvc.perform(get("/admin/replay/proc-1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.processInstanceId").value("proc-1"))
               .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void replay_returnsSuccessFalse_whenServiceReturnsFalse() throws Exception {
        when(replayService.replay("proc-missing")).thenReturn(false);

        mockMvc.perform(get("/admin/replay/proc-missing"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.processInstanceId").value("proc-missing"))
               .andExpect(jsonPath("$.success").value(false));
    }
}
