package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.AddMemberToTeamDto;
import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.dto.UpdateMemberRoleDto;
import com.vishalchauhan0688.dailyStandUp.model.EmployeeTeamRole;
import com.vishalchauhan0688.dailyStandUp.service.AuthorizationService;
import com.vishalchauhan0688.dailyStandUp.service.EmployeeService;
import com.vishalchauhan0688.dailyStandUp.service.EmployeeTeamRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams/{teamId}/members")
@RequiredArgsConstructor
public class EmployeeTeamRoleController {
    private final EmployeeTeamRoleService employeeTeamRoleService;
    private final AuthorizationService authorizationService;
    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeTeamRole>>> getTeamMembers(
            @PathVariable Long teamId) {
        List<EmployeeTeamRole> members = employeeTeamRoleService.findByTeamId(teamId);
        return ResponseEntity.ok(ApiResponse.success("Team members fetched successfully", members));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeTeamRole>> addMember(
            @PathVariable Long teamId,
            @Valid @RequestBody AddMemberToTeamDto dto) {
        Long currentEmployeeId = employeeService.getMe().getId();
        
        // Authorization: Only OWNER or TEAM_ADMIN can add members
        authorizationService.verifyCanManageTeamMembers(currentEmployeeId, teamId, false);
        
        EmployeeTeamRole member = employeeTeamRoleService.addEmployeeToTeam(
                dto.getEmployeeId(), teamId, dto.getRoleId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Member added to team successfully", member));
    }

    @PutMapping("/{employeeId}/role")
    public ResponseEntity<ApiResponse<EmployeeTeamRole>> updateMemberRole(
            @PathVariable Long teamId,
            @PathVariable Long employeeId,
            @Valid @RequestBody UpdateMemberRoleDto dto) {
        Long currentEmployeeId = employeeService.getMe().getId();
        
        // Authorization: Only OWNER can change roles
        authorizationService.verifyCanManageTeamMembers(currentEmployeeId, teamId, true);
        
        // Verify team has at least one OWNER before role change
        authorizationService.verifyTeamHasOwner(teamId, employeeId);
        
        employeeTeamRoleService.updateEmployeeRoleInTeam(employeeId, teamId, dto.getRoleId());
        EmployeeTeamRole member = employeeTeamRoleService.findByEmployeeIdAndTeamId(employeeId, teamId);
        return ResponseEntity.ok(ApiResponse.success("Member role updated successfully", member));
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long employeeId) {
        Long currentEmployeeId = employeeService.getMe().getId();
        
        // Authorization: Only OWNER or TEAM_ADMIN can remove members
        authorizationService.verifyCanManageTeamMembers(currentEmployeeId, teamId, false);
        
        // Verify team has at least one OWNER
        authorizationService.verifyTeamHasOwner(teamId, employeeId);
        
        employeeTeamRoleService.removeEmployeeFromTeam(employeeId, teamId);
        return ResponseEntity.ok(ApiResponse.success("Member removed from team successfully", null));
    }
}
