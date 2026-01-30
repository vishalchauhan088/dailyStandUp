package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponseDto;
import com.vishalchauhan0688.dailyStandUp.dto.EmployeeCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.EmployeeResponseDto;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<EmployeeResponseDto>>> getAll(){
        List<EmployeeResponseDto> employees = employeeService.findAll();

        ApiResponseDto<List<EmployeeResponseDto>> response =
                new ApiResponseDto<>(
                        200,
                        "Employees fetched successfully",
                        employees
                );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<EmployeeResponseDto>> getById(@PathVariable Long id) throws ResourceNotFoundException {
        EmployeeResponseDto employee = employeeService.findById(id);
        ApiResponseDto<EmployeeResponseDto> response =
                new ApiResponseDto<>(
                        200,
                        "Fetched employee by id successfully",
                        employee
                );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<EmployeeResponseDto>> save(@RequestBody EmployeeCreateDto empReqDto) throws ResourceNotFoundException {
        EmployeeResponseDto employee = employeeService.save(empReqDto);
        ApiResponseDto<EmployeeResponseDto> response =
                new ApiResponseDto<>(
                        200,
                        "Employee saved successfully",
                        employee
                );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}
