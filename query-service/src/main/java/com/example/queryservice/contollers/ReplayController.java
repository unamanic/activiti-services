package com.example.queryservice.contollers;

import com.example.queryservice.services.ReplayService;
import jakarta.websocket.server.PathParam;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/testing")
public class ReplayController {

    private final ReplayService replayService;

    public ReplayController(ReplayService replayService) {
        this.replayService = replayService;
    }

    @GetMapping("/replay/{id}")
    public String replay(@PathVariable("id") String id) {
        replayService.replay(id);
        return "foo";
    }
}
