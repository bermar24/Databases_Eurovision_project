package com.dhbw.eurovision.controller;

import com.dhbw.eurovision.dto.request.BallotRequestDTO;
import com.dhbw.eurovision.dto.response.BallotResponseDTO;
import com.dhbw.eurovision.dto.response.VoteLogResponseDTO;
import com.dhbw.eurovision.service.VoteLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/votes")
public class VoteLogController {

    private final VoteLogService voteLogService;

    public VoteLogController(VoteLogService voteLogService) {
        this.voteLogService = voteLogService;
    }

    /** GET /api/votes — list all vote entries */
    @GetMapping
    public ResponseEntity<List<VoteLogResponseDTO>> getAllVotes() {
        return ResponseEntity.ok(voteLogService.getAllVotes());
    }

    /**
     * POST /api/votes/session — submit a complete ballot of 10 entries.
     * This is the primary voting endpoint.
     */
    @PostMapping("/session")
    public ResponseEntity<BallotResponseDTO> submitBallot(
            @RequestBody BallotRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(voteLogService.submitBallot(dto));
    }
}