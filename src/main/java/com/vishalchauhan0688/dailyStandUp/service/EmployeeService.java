package com.vishalchauhan0688.dailyStandUp.service;
import org.springframework.beans.factory.annotation.Value;


import com.vishalchauhan0688.dailyStandUp.dto.EmployeeCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.EmployeeResponseDto;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.model.Role;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeRepository;
import com.vishalchauhan0688.dailyStandUp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-role}")
    private String defaultRoleName;

    //Get
    public List<EmployeeResponseDto> findAll(){
        return employeeRepository.findAll().stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }
    public EmployeeResponseDto findById(Long id) throws ResourceNotFoundException {
        return employeeRepository.findById(id).map(this::mapToResponseDto).orElseThrow(() -> {
            return new ResourceNotFoundException(String.format("Employee {} not found.",id),403);
        });
    }
    public Optional<Employee> findByEmail(String email){
        return employeeRepository.findByEmail(email);
    }

    //save
    public EmployeeResponseDto save(EmployeeCreateDto empReqDto) throws ResourceNotFoundException {
        Employee emp = this.mapFromRequestDto(empReqDto);
        emp.setPassword(passwordEncoder.encode(empReqDto.getPassword()));
        Role defaultRole = roleRepository.findByName(defaultRoleName).orElseThrow(
                ()-> new ResourceNotFoundException("Default role not found: ", 403)
        );
        emp.setRole(defaultRole);
        System.out.println(emp);
        return this.mapToResponseDto(employeeRepository.save(emp));
    }

    //Delete
    public boolean deleteById(Long id){
        return employeeRepository.deleteEmployeeById(id) == 1;
    }

    public Employee getMe() throws ResourceNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        java.lang.Object principal = authentication.getPrincipal();

        if(principal instanceof String) {
            return employeeRepository.findByEmail(principal.toString()).orElseThrow(
                    ()-> new ResourceNotFoundException("LoggedIn user not found: " + principal.toString(),403)
            );

        }
        return null;
    }


    /**
     * Employee -> EmployeeResponseDto
     * @param employee Map employee to its Response DTO
     * @return EmployeeResponseDto
     */
    public EmployeeResponseDto mapToResponseDto(@NonNull Employee employee) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setCreated_at(employee.getCreated_at());
        dto.setUpdated_at(employee.getUpdated_at());
        dto.setUsername(employee.getUserName());
        dto.setRole(employee.getRole());

        if (employee.getManager() != null) {
            dto.setManagerId(employee.getManager().getId());
            dto.setManagerName(employee.getManager().getFirstName() +
                    (employee.getManager().getLastName() != null ?
                            " " + employee.getManager().getLastName() : ""));
        }
        return dto;
    }

    /**
     * EmployeeRequestDto -> Employee
     * @param dto
     * @return
     */
    public Employee mapFromRequestDto(EmployeeCreateDto dto) {
        Employee employee = new Employee();
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setUserName(dto.getUserName());
        employee.setEmail(dto.getEmail());
        return employee;
    }

}
