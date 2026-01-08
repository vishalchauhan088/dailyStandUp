package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.dto.EmployeeRequestDto;
import com.vishalchauhan0688.dailyStandUp.dto.EmployeeResponseDto;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    //Get
    public List<Employee> findAll(){
        return employeeRepository.findAll();
    }
    public Optional<Employee> findById(Long id){
        return employeeRepository.findById(id);
    }
    public Optional<Employee> findByEmail(String email){
        return employeeRepository.findByEmail();
    }

    //save
    public Employee save(Employee employee){
        return employeeRepository.save(employee);
    }

    //Delete
    public boolean deleteById(Long id){
        return employeeRepository.deleteEmployeeById(id) == 1;
    }

    public EmployeeResponseDto mapToResponseDto(Employee employee) {
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

    public Employee mapFromRequestDto(EmployeeRequestDto dto) {
        Employee employee = new Employee();
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        return employee;
    }



}
