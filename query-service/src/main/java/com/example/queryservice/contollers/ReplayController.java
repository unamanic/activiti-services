package com.example.queryservice.contollers;

import com.example.queryservice.model.ReplayResponse;
import com.example.queryservice.services.ReplayService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/v1")
public class ReplayController {

    private final ReplayService replayService;

    public ReplayController(ReplayService replayService) {
        this.replayService = replayService;
    }

    @PreAuthorize("hasRole('ACTIVITI_ADMIN')")
    @GetMapping("/replay/{id}")
    public ResponseEntity<ReplayResponse> replay(@PathVariable("id") String id) {
        replayService.replay(id);
        return ResponseEntity.accepted()
                .body(ReplayResponse.builder()
                        .processInstanceId(id)
                        .message("Replay requested")
                        .build());
    }
}
