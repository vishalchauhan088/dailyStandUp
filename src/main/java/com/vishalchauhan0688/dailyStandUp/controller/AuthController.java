package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.*;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.service.EmployeeService;
import com.vishalchauhan0688.dailyStandUp.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final EmployeeService employeeService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * Generate JWT token with both system roles and team roles.
     * 
     * Important: We query the database to get the FULL role information
     * because the employee entity might not be fully loaded with roles yet.
     */
    private String generateTokenWithAllRoles(Employee emp) {

        // Get global/system roles (these are now stored separately)
        Set<String> systemRoles = emp.getGlobalRoles().stream()
                .map(egr -> egr.getGlobalRole() != null ? egr.getGlobalRole().getName().name() : null)
                .filter(name -> name != null)
                .collect(Collectors.toSet());
        return jwtUtil.generateToken(emp.getEmail(), systemRoles.stream().toList());
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponseDataDto>> signup(
            @Valid @RequestBody EmployeeCreateDto employeeCreateDto) {
        EmployeeResponseDto emp = employeeService.save(employeeCreateDto);

        // Convert DTO back to entity for role extraction (or fetch fresh from DB)
        Employee employeeEntity = employeeService.findByEmail(emp.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found after creation"));

        // Generate token with both system and team roles
        String jwtToken = generateTokenWithAllRoles(employeeEntity);
        AuthResponseDataDto data = new AuthResponseDataDto(jwtToken, emp);

        return ResponseEntity.ok(ApiResponse.success("Signup successful", data));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDataDto>> login(@Valid @RequestBody LoginRequestDto credentials) {
        Employee emp = employeeService.findByEmail(credentials.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(credentials.getPassword(), emp.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }

        // Generate token with both system and team roles
        String jwtToken = generateTokenWithAllRoles(emp);
        EmployeeResponseDto userResponse = employeeService.mapToResponseDto(emp);

        AuthResponseDataDto data = new AuthResponseDataDto(jwtToken, userResponse);

        return ResponseEntity.ok(ApiResponse.success("Login successful", data));
    }
}
