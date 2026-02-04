package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.dto.MarkDependencyResolvedDto;
import com.vishalchauhan0688.dailyStandUp.dto.TicketDependencyCreateDto;
import com.vishalchauhan0688.dailyStandUp.model.TicketDependency;
import com.vishalchauhan0688.dailyStandUp.service.TicketDependencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ticket-dependencies")
@RequiredArgsConstructor
public class TicketDependencyController {
    private final TicketDependencyService dependencyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketDependency>>> getAll() {
        List<TicketDependency> dependencies = dependencyService.findAll();
        return ResponseEntity.ok(ApiResponse.success("Dependencies fetched successfully", dependencies));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDependency>> getById(@PathVariable Long id) {
        TicketDependency dependency = dependencyService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Dependency fetched successfully", dependency));
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<ApiResponse<List<TicketDependency>>> getByTicketId(@PathVariable Long ticketId) {
        List<TicketDependency> dependencies = dependencyService.findByTicketId(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Dependencies fetched successfully", dependencies));
    }

    @GetMapping("/ticket/{ticketId}/unresolved")
    public ResponseEntity<ApiResponse<List<TicketDependency>>> getUnresolved(@PathVariable Long ticketId) {
        List<TicketDependency> dependencies = dependencyService.findUnresolvedDependenciesByTicketId(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Unresolved dependencies fetched successfully", dependencies));
    }

    @GetMapping("/ticket/{ticketId}/blockers")
    public ResponseEntity<ApiResponse<List<TicketDependency>>> getBlockers(@PathVariable Long ticketId) {
        List<TicketDependency> blockers = dependencyService.findBlockingTickets(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Blocking tickets fetched successfully", blockers));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketDependency>> create(
            @Valid @RequestBody TicketDependencyCreateDto dto) {
        TicketDependency created = dependencyService.create(dto.getTicketId(), dto.getDependsOnTicketId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Dependency created successfully", created));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<TicketDependency>> markResolved(
            @PathVariable Long id, 
            @Valid @RequestBody MarkDependencyResolvedDto dto) {
        TicketDependency updated = dependencyService.markResolved(id, dto.getResolved());
        return ResponseEntity.ok(ApiResponse.success("Dependency updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        dependencyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Dependency deleted successfully", null));
    }
}
