package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.model.TeamJoinRequest;
import com.vishalchauhan0688.dailyStandUp.service.EmployeeService;
import com.vishalchauhan0688.dailyStandUp.service.TeamJoinRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/join-requests")
@RequiredArgsConstructor
public class TeamJoinRequestController {
    private final TeamJoinRequestService teamJoinRequestService;
    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<ApiResponse<TeamJoinRequest>> createJoinRequest(
            @PathVariable Long teamId) {
        Long employeeId = employeeService.getMe().getId();
        TeamJoinRequest request = teamJoinRequestService.createJoinRequest(employeeId, teamId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Join request created successfully", request));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<TeamJoinRequest>>> getPendingRequests(
            @PathVariable Long teamId) {
        List<TeamJoinRequest> requests = teamJoinRequestService.getPendingRequestsForTeam(teamId);
        return ResponseEntity.ok(ApiResponse.success("Pending requests fetched successfully", requests));
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<ApiResponse<TeamJoinRequest>> approveRequest(
            @PathVariable Long teamId,
            @PathVariable Long requestId) {
        Long approverId = employeeService.getMe().getId();
        TeamJoinRequest request = teamJoinRequestService.approveRequest(requestId, approverId);
        return ResponseEntity.ok(ApiResponse.success("Join request approved successfully", request));
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<TeamJoinRequest>> rejectRequest(
            @PathVariable Long teamId,
            @PathVariable Long requestId) {
        Long approverId = employeeService.getMe().getId();
        TeamJoinRequest request = teamJoinRequestService.rejectRequest(requestId, approverId);
        return ResponseEntity.ok(ApiResponse.success("Join request rejected successfully", request));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<List<TeamJoinRequest>>> getMyRequests() {
        Long employeeId = employeeService.getMe().getId();
        List<TeamJoinRequest> requests = teamJoinRequestService.getRequestsByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success("My join requests fetched successfully", requests));
    }
}
