package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.dto.EmployeeCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.EmployeeResponseDto;
import com.vishalchauhan0688.dailyStandUp.dto.PageResponse;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.model.Role;
import com.vishalchauhan0688.dailyStandUp.model.Team;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeRepository;
import com.vishalchauhan0688.dailyStandUp.repository.RoleRepository;
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
    private final RoleRepository roleRepository;
    private final TeamService teamService;
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
                cb.like(cb.lower(root.get("userName")), searchTerm),
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

        if (employeeRepository.findByUserName(dto.getUserName()).isPresent()) {
            throw new BadRequestException(
                    "Employee with username already exists: " + dto.getUserName()
            );
        }

        Employee employee = mapFromRequestDto(dto);
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));

        Team team = teamService.findById(dto.getTeamId());
        employee.setTeam(team);

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found with id: " + dto.getRoleId()
                        )
                );
        employee.setRole(role);

        if (dto.getManagerId() != null) {
            Employee manager = employeeRepository.findById(dto.getManagerId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Manager not found with id: " + dto.getManagerId()
                            )
                    );
            employee.setManager(manager);
        }

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

        if (!employee.getSubordinates().isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete employee with subordinates. Please reassign them first."
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
        dto.setUsername(employee.getUserName());
        dto.setName(employee.getName());
        dto.setEmail(employee.getEmail());
        dto.setCreated_at(employee.getCreated_at());
        dto.setUpdated_at(employee.getUpdated_at());
        dto.setRole(employee.getRole());

        if (employee.getManager() != null) {
            dto.setManagerId(employee.getManager().getId());
            dto.setManagerName(employee.getManager().getName());
        }

        if (employee.getTeam() != null) {
            dto.setTeamId(employee.getTeam().getId());
            dto.setTeamName(employee.getTeam().getTeamName());
        }

        return dto;
    }

    private Employee mapFromRequestDto(EmployeeCreateDto dto) {

        Employee employee = new Employee();
        employee.setUserName(dto.getUserName());
        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        return employee;
    }
}
