package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.*;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.service.EmployeeService;
import com.vishalchauhan0688.dailyStandUp.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final EmployeeService employeeService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<AuthResponseDataDto>> signup(@RequestBody EmployeeCreateDto employeeCreateDto) throws ResourceNotFoundException {
        EmployeeResponseDto emp = employeeService.save(employeeCreateDto);
        String jwtToken = jwtUtil.generateToken(emp.getEmail(), List.of(emp.getRole().getName()));

        AuthResponseDataDto data = new AuthResponseDataDto(jwtToken, emp);

        ApiResponseDto<AuthResponseDataDto> response = new ApiResponseDto<>(
                200,
                "Signup successful",
                data
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<AuthResponseDataDto>> login(@RequestBody LoginRequestDto credentials) throws ResourceNotFoundException {
        Employee emp = employeeService.findByEmail(credentials.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("user not found",403));
        if(!passwordEncoder.matches(credentials.getPassword(), emp.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }
        String jwtToken = jwtUtil.generateToken(credentials.getEmail(), List.of());
        EmployeeResponseDto userResponse = employeeService.mapToResponseDto(emp);

        AuthResponseDataDto data = new AuthResponseDataDto(jwtToken, userResponse);
        System.out.print("data is " + data);

        ApiResponseDto<AuthResponseDataDto> response = new ApiResponseDto<>(
                200,
                "Login Successful",
                data
        );

        return ResponseEntity.ok(response);
    }
}
