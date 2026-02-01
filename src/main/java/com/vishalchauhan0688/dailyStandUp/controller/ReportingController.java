package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Controller for reporting and dashboard endpoints
 * Provides bird's eye view for project managers
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportingController {
    private final ReportingService reportingService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverallDashboard() {
        Map<String, Object> dashboard = reportingService.getOverallDashboard();
        return ResponseEntity.ok(ApiResponse.success("Dashboard data fetched successfully", dashboard));
    }

    @GetMapping("/employee/{employeeId}/workload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeWorkload(@PathVariable Long employeeId) {
        Map<String, Object> workload = reportingService.getEmployeeWorkload(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee workload fetched successfully", workload));
    }

    @GetMapping("/team/{teamId}/progress")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTeamProgress(@PathVariable Long teamId) {
        Map<String, Object> progress = reportingService.getTeamProgress(teamId);
        return ResponseEntity.ok(ApiResponse.success("Team progress fetched successfully", progress));
    }

    @GetMapping("/project/{projectId}/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProjectHealth(@PathVariable Long projectId) {
        Map<String, Object> health = reportingService.getProjectHealth(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project health fetched successfully", health));
    }

    @GetMapping("/standup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDailyStandupReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        Map<String, Object> report = reportingService.getDailyStandupReport(date);
        return ResponseEntity.ok(ApiResponse.success("Daily standup report fetched successfully", report));
    }

    @GetMapping("/manager/{managerId}/view")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getManagerView(@PathVariable Long managerId) {
        Map<String, Object> view = reportingService.getManagerView(managerId);
        return ResponseEntity.ok(ApiResponse.success("Manager view fetched successfully", view));
    }
}
