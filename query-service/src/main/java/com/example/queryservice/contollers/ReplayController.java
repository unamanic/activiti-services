package com.example.queryservice.contollers;

import com.example.queryservice.model.ReplayResponse;
import com.example.queryservice.services.ReplayService;
import jakarta.websocket.server.PathParam;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class ReplayController {

    private final ReplayService replayService;

    public ReplayController(ReplayService replayService) {
        this.replayService = replayService;
    }

    @GetMapping("/replay/{id}")
    public ReplayResponse replay(@PathVariable("id") String id) {
        boolean success = replayService.replay(id);
        return ReplayResponse
                .builder()
                .processInstanceId(id)
                .success(success)
                .build();
    }
}
