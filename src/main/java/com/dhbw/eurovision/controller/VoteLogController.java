package com.dhbw.eurovision.controller;

import com.dhbw.eurovision.dto.request.VoteLogRequestDTO;
import com.dhbw.eurovision.dto.response.VoteLogResponseDTO;
import com.dhbw.eurovision.service.VoteLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for VoteLog.
 * Base path: /api/votes
 */
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
     * POST /api/votes — cast a vote.
     * Body must contain songId and exactly one of juryId or citizenId.
     */
    @PostMapping
    public ResponseEntity<VoteLogResponseDTO> castVote(@RequestBody VoteLogRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(voteLogService.castVote(dto));
    }
}
