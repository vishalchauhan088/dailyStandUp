package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.dto.EmployeeCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.EmployeeResponseDto;
import com.vishalchauhan0688.dailyStandUp.dto.PageResponse;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.service.EmployeeService;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String fields) {
        
        QueryParams params = QueryParams.builder()
                .page(page != null ? page : 0)
                .size(size != null ? size : 20)
                .sort(sort)
                .search(search)
                .filter(filter)
                .fields(fields)
                .build();
        
        PageResponse<EmployeeResponseDto> result = employeeService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success("Employees fetched successfully", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> getById(@PathVariable Long id) {
        EmployeeResponseDto employee = employeeService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee fetched successfully", employee));
    }

    // Use filter parameter instead: ?filter=team.id:1 or filter=manager.id:1

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponseDto>> save(@Valid @RequestBody EmployeeCreateDto empReqDto) {
        EmployeeResponseDto employee = employeeService.save(empReqDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Employee created successfully", employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        employeeService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully", null));
    }
}
