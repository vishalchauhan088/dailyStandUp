package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.EmployeeCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.EmployeeResponseDto;
import com.vishalchauhan0688.dailyStandUp.dto.LoginRequestDto;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final EmployeeService employeeService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody EmployeeCreateDto employeeCreateDto) throws ResourceNotFoundException {
        EmployeeResponseDto emp = employeeService.save(employeeCreateDto);
        String jwtToken = jwtUtil.generateToken(emp.getEmail(), List.of(emp.getRole().getName()));

        return ResponseEntity.ok(
                Map.of(
                        "token", jwtToken,
                        "user",emp
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto credentials) throws ResourceNotFoundException {
        Employee emp = employeeService.findByEmail(credentials.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("user not found",403));
        if(!passwordEncoder.matches(credentials.getPassword(), emp.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }
        String jwtToken = jwtUtil.generateToken(credentials.getEmail(), List.of());
        return ResponseEntity.ok(
                Map.of(
                        "token", jwtToken,
                        "user",employeeService.mapToResponseDto(emp)
                )
        );
    }
}
