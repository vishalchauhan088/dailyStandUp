package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.model.EmployeeTeamRole;
import com.vishalchauhan0688.dailyStandUp.service.EmployeeTeamRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/members")
@RequiredArgsConstructor
public class EmployeeTeamRoleController {
    private final EmployeeTeamRoleService employeeTeamRoleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeTeamRole>>> getTeamMembers(
            @PathVariable Long teamId) {
        List<EmployeeTeamRole> members = employeeTeamRoleService.findByTeamId(teamId);
        return ResponseEntity.ok(ApiResponse.success("Team members fetched successfully", members));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeTeamRole>> addMember(
            @PathVariable Long teamId,
            @RequestBody Map<String, Long> request) {
        Long employeeId = request.get("employeeId");
        Long roleId = request.get("roleId");
        
        if (employeeId == null || roleId == null) {
            throw new com.vishalchauhan0688.dailyStandUp.exception.BadRequestException("employeeId and roleId are required");
        }
        
        EmployeeTeamRole member = employeeTeamRoleService.addEmployeeToTeam(employeeId, teamId, roleId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Member added to team successfully", member));
    }

    @PutMapping("/{employeeId}/role")
    public ResponseEntity<ApiResponse<EmployeeTeamRole>> updateMemberRole(
            @PathVariable Long teamId,
            @PathVariable Long employeeId,
            @RequestBody Map<String, Long> request) {
        Long roleId = request.get("roleId");
        
        if (roleId == null) {
            throw new com.vishalchauhan0688.dailyStandUp.exception.BadRequestException("roleId is required");
        }
        
        employeeTeamRoleService.updateEmployeeRoleInTeam(employeeId, teamId, roleId);
        EmployeeTeamRole member = employeeTeamRoleService.findByEmployeeIdAndTeamId(employeeId, teamId);
        return ResponseEntity.ok(ApiResponse.success("Member role updated successfully", member));
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long employeeId) {
        employeeTeamRoleService.removeEmployeeFromTeam(employeeId, teamId);
        return ResponseEntity.ok(ApiResponse.success("Member removed from team successfully", null));
    }
}

