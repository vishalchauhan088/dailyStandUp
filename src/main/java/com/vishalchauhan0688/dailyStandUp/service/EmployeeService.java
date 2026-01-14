package com.vishalchauhan0688.dailyStandUp.service;
import com.vishalchauhan0688.dailyStandUp.fieldPathResolver.EmployeePathResolver;
import com.vishalchauhan0688.dailyStandUp.query.Direction;
import com.vishalchauhan0688.dailyStandUp.query.normalization.NormalizedQuery;
import com.vishalchauhan0688.dailyStandUp.query.normalization.QueryNormalizer;
import com.vishalchauhan0688.dailyStandUp.query.parser.QueryParser;
import com.vishalchauhan0688.dailyStandUp.query.request.QueryRequest;
import com.vishalchauhan0688.dailyStandUp.query.request.SortCondition;
import com.vishalchauhan0688.dailyStandUp.query.translation.SpecificationBuilder;
import com.vishalchauhan0688.dailyStandUp.query.validation.QueryValidator;
import com.vishalchauhan0688.dailyStandUp.query.validation.ValidatiedQuery;
import com.vishalchauhan0688.dailyStandUp.querySchema.EmployeeSchema;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final QueryValidator queryValidator;
    private final QueryParser  queryParser;
    private final QueryNormalizer queryNormalizer;
    private final SpecificationBuilder specificationBuilder;

    @Value("${app.default-role}")
    private String defaultRoleName;

    //Get
    public Page<EmployeeResponseDto> findAll(Map<String, String[]> requestParams) {

            //1. Http params -> query request
            QueryRequest query = queryParser.parse((requestParams));

            //validte against EmployeeSchema
        ValidatiedQuery validatedQuery = queryValidator.validateQuery(query, EmployeeSchema.SCHEMA);

        // 3️⃣ Normalize (defaults, tie‑breaker, etc.)
         NormalizedQuery normalizedQuery =
                queryNormalizer.normalize(
                        validatedQuery,
                        EmployeeSchema.SCHEMA
                );

        // 4️⃣ Build Specification (THIS is where SpecificationBuilder is used)
        Specification<Employee> specification =
                specificationBuilder.buildSpecification(
                        normalizedQuery,
                        new EmployeePathResolver()
                );

        // 5️⃣ Build PageRequest (pagination + sorting)
        PageRequest pageRequest =
                PageRequest.of(
                        normalizedQuery.getPage(),
                        normalizedQuery.getSize(),
                        toSpringSort(normalizedQuery.getSorts())
                );

        // 6️⃣ Execute query
        Page<Employee> page =
                employeeRepository.findAll(
                        specification,
                        pageRequest
                );

        // 7️⃣ Map entities → DTOs
        return page.map(this::mapToResponseDto);

//        return employeeRepository.findAll().stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }
    private Sort toSpringSort(List<SortCondition> sorts) {

        if (sorts == null || sorts.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = sorts.stream()
                .map(sc -> new Sort.Order(
                        sc.getDirection() == Direction.ASC
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC,
                        sc.getField()
                ))
                .toList();

        return Sort.by(orders);
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
        dto.setCreated_at(employee.getCreatedAt());
        dto.setUpdated_at(employee.getUpdatedAt());
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
