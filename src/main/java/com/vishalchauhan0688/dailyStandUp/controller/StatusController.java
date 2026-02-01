package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.model.Status;
import com.vishalchauhan0688.dailyStandUp.service.StatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/status")
@RequiredArgsConstructor
public class StatusController {
    private final StatusService statusService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Status>>> getAll() {
        List<Status> statuses = statusService.findAll();
        return ResponseEntity.ok(ApiResponse.success("Statuses fetched successfully", statuses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Status>> getById(@PathVariable Long id) {
        Status status = statusService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Status fetched successfully", status));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Status>> create(@Valid @RequestBody Status status) {
        Status created = statusService.save(status);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Status created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Status>> update(@PathVariable Long id, @Valid @RequestBody Status status) {
        Status updated = statusService.update(id, status);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        statusService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Status deleted successfully", null));
    }
}
