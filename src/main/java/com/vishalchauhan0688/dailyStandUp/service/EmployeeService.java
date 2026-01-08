package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.dto.EmployeeCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.EmployeeResponseDto;
import com.vishalchauhan0688.dailyStandUp.dto.EmployeeUpdateDto;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

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
    public EmployeeResponseDto save(EmployeeCreateDto empReqDto){
        return this.mapToResponseDto(employeeRepository.save(this.mapFromRequestDto(empReqDto)));
    }

    //Delete
    public boolean deleteById(Long id){
        return employeeRepository.deleteEmployeeById(id) == 1;
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
        employee.setEmail(dto.getEmail());
        return employee;
    }

}
