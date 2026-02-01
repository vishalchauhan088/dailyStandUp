package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.model.Team;
import com.vishalchauhan0688.dailyStandUp.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Team>>> getAll() {
        List<Team> teams = teamService.findAll();
        return ResponseEntity.ok(ApiResponse.success("Teams fetched successfully", teams));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Team>> getById(@PathVariable Long id) {
        Team team = teamService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Team fetched successfully", team));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Team>> create(@Valid @RequestBody Team team) {
        Team created = teamService.save(team);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Team created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Team>> update(@PathVariable Long id, @Valid @RequestBody Team team) {
        Team updated = teamService.update(id, team);
        return ResponseEntity.ok(ApiResponse.success("Team updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Team deleted successfully", null));
    }
}
