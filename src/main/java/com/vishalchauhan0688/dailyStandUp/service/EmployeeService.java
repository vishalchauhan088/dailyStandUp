package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.dto.EmployeeCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.EmployeeResponseDto;
import com.vishalchauhan0688.dailyStandUp.dto.PageResponse;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeRepository;
import com.vishalchauhan0688.dailyStandUp.util.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final QueryService queryService;

    public List<EmployeeResponseDto> findAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public PageResponse<EmployeeResponseDto> findAll(QueryParams params) {
        Page<Employee> page = queryService.query(
                employeeRepository,
                params,
                this::employeeSearchSpec
        );

        List<EmployeeResponseDto> content = page.getContent()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return PageResponse.of(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    /**
     * Defines HOW search works for Employee
     */
    private Specification<Employee> employeeSearchSpec(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        String searchTerm = "%" + search.toLowerCase() + "%";

        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("username")), searchTerm),
                cb.like(cb.lower(root.get("email")), searchTerm),
                cb.like(cb.lower(root.get("name")), searchTerm)
        );
    }

    public EmployeeResponseDto findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + id
                        )
                );
        return mapToResponseDto(employee);
    }

    public Optional<Employee> findByIdEntity(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    @Transactional
    public EmployeeResponseDto save(EmployeeCreateDto dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException(
                    "Employee with email already exists: " + dto.getEmail()
            );
        }

        if (employeeRepository.existsByUsername(dto.getUsername())) {
            throw new BadRequestException(
                    "Employee with username already exists: " + dto.getUsername()
            );
        }

        Employee employee = Employee.builder()
                .username(dto.getUsername())
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        return mapToResponseDto(employeeRepository.save(employee));
    }

    @Transactional
    public boolean deleteById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + id
                        )
                );

        // Check if employee has team roles
        if (!employee.getTeamRoles().isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete employee with team memberships. Please remove from teams first."
            );
        }

        return employeeRepository.deleteEmployeeById(id) == 1;
    }

    public Employee getMe() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Object principal = authentication.getPrincipal();

        if (principal instanceof String email) {
            return employeeRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Logged in user not found: " + email
                            )
                    );
        }

        throw new ResourceNotFoundException("Invalid authentication principal");
    }

    public EmployeeResponseDto mapToResponseDto(Employee employee) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(employee.getId());
        dto.setUsername(employee.getUsername());
        dto.setName(employee.getName());
        dto.setEmail(employee.getEmail());
        dto.setCreatedAt(employee.getCreatedAt());
        dto.setUpdatedAt(employee.getUpdatedAt());

        // Map team roles
        List<EmployeeResponseDto.TeamRoleInfo> teamRoles = employee.getTeamRoles().stream()
                .map(etr -> {
                    EmployeeResponseDto.TeamRoleInfo info = new EmployeeResponseDto.TeamRoleInfo();
                    info.setTeamId(etr.getTeam().getId());
                    info.setTeamName(etr.getTeam().getTeamName());
                    info.setRoleId(etr.getTeamRole().getId());
                    info.setRoleName(etr.getTeamRole().getName());
                    return info;
                })
                .collect(Collectors.toList());
        dto.setTeamRoles(teamRoles);

        return dto;
    }
}
