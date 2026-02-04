package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.dto.DailyUpdateCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.PageResponse;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.dto.TicketMentionCreateDto;
import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.DailyUpdatePost;
import com.vishalchauhan0688.dailyStandUp.model.DailyUpdateTicketMention;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.model.Team;
import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import com.vishalchauhan0688.dailyStandUp.repository.DailyUpdatePostRepository;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeTeamRoleRepository;
import com.vishalchauhan0688.dailyStandUp.util.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyUpdateService {

    private final DailyUpdatePostRepository dailyUpdatePostRepository;
    private final EmployeeService employeeService;
    private final TeamService teamService;
    private final TicketService ticketService;
    private final AuthorizationService authorizationService;
    private final QueryService queryService;
    private final EmployeeTeamRoleRepository employeeTeamRoleRepository;

    public List<DailyUpdatePost> findAll() {
        return dailyUpdatePostRepository.findAll();
    }

    public PageResponse<DailyUpdatePost> findAll(QueryParams params) {
        return findAll(params, null);
    }

    /**
     * Find daily updates with team filtering, authorization, and expanded search.
     * 
     * @param params Query parameters (filter, search, pagination, sort)
     * @param teamId Optional team ID to filter by. If null, filters by user's teams
     *               (unless admin)
     */
    public PageResponse<DailyUpdatePost> findAll(QueryParams params, Long teamId) {
        Employee loggedInEmployee = employeeService.getMe();
        Long employeeId = loggedInEmployee.getId();
        boolean isGlobalAdmin = authorizationService.isGlobalAdmin(employeeId);

        // Build a combined specification
        Specification<DailyUpdatePost> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Add team filter based on authorization
            if (teamId != null) {
                // Specific team requested - verify user has access
                if (!isGlobalAdmin && !authorizationService.isTeamMember(employeeId, teamId)) {
                    throw new BadRequestException("You don't have access to team " + teamId);
                }
                predicates.add(cb.equal(root.get("team").get("id"), teamId));
            } else if (!isGlobalAdmin) {
                // Non-admin: only show updates from teams the user belongs to
                List<Long> userTeamIds = getUserTeamIds(employeeId);
                if (!userTeamIds.isEmpty()) {
                    predicates.add(root.get("team").get("id").in(userTeamIds));
                } else {
                    // User is not in any team - return empty result
                    predicates.add(cb.disjunction()); // Always false condition
                }
            }
            // Global admin sees all teams (no filter)

            // Add search filter if provided
            if (params.getSearch() != null && !params.getSearch().trim().isEmpty()) {
                String searchTerm = "%" + params.getSearch().toLowerCase() + "%";

                // Search across: generalNotes, employee name, team name, ticket titles
                jakarta.persistence.criteria.Predicate searchPred = cb.or(
                        cb.like(cb.lower(root.get("generalNotes")), searchTerm),
                        cb.like(cb.lower(root.get("employee").get("name")), searchTerm),
                        cb.like(cb.lower(root.get("team").get("teamName")), searchTerm));
                predicates.add(searchPred);
            }

            // Add any additional filter parameters from QueryParams
            for (QueryParams.FilterParam filter : params.getFilterParams()) {
                // Handle special filters
                if ("employeeId".equals(filter.getField())) {
                    Object value = parseLongValue(filter.getValue());
                    if (value != null) {
                        predicates.add(cb.equal(root.get("employee").get("id"), value));
                    }
                } else if ("teamId".equals(filter.getField())) {
                    Object value = parseLongValue(filter.getValue());
                    if (value != null) {
                        predicates.add(cb.equal(root.get("team").get("id"), value));
                    }
                } else if ("date".equals(filter.getField())) {
                    Object value = parseLocalDateValue(filter.getValue());
                    if (value != null) {
                        predicates.add(cb.equal(root.get("date"), value));
                    }
                } else {
                    // Default behavior - use the field directly
                    jakarta.persistence.criteria.Predicate filterPred = queryService.createFilterPredicate(root, query,
                            cb, filter);
                    if (filterPred != null) {
                        predicates.add(filterPred);
                    }
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<DailyUpdatePost> page = queryService.query(
                dailyUpdatePostRepository,
                params,
                s -> null); // Search is handled in spec above

        return PageResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements());
    }

    /**
     * Get all team IDs that a user belongs to
     */
    public List<Long> getUserTeamIds(Long employeeId) {
        return employeeTeamRoleRepository.findByEmployeeId(employeeId)
                .stream()
                .map(etr -> etr.getTeam().getId())
                .toList();
    }

    /**
     * Defines HOW search works for DailyUpdatePost
     * Now expanded to search across multiple fields
     */
    private Specification<DailyUpdatePost> dailyUpdateSearchSpec(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        String searchTerm = "%" + search.toLowerCase() + "%";

        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("generalNotes")), searchTerm),
                cb.like(cb.lower(root.get("employee").get("name")), searchTerm),
                cb.like(cb.lower(root.get("team").get("teamName")), searchTerm));
    }

    /**
     * Parse Long value safely
     */
    private Long parseLongValue(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse LocalDate value safely
     */
    private java.time.LocalDate parseLocalDateValue(String value) {
        try {
            return java.time.LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    public DailyUpdatePost findById(Long id) {
        return dailyUpdatePostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Daily update not found with id: " + id));
    }

    @Transactional
    public DailyUpdatePost save(DailyUpdateCreateDto dto) {
        Employee loggedInEmployee = employeeService.getMe();
        Long employeeId = loggedInEmployee.getId();
        Team team = teamService.findById(dto.getTeamId());

        // Authorization: Must be team member
        if (!authorizationService.isTeamMember(employeeId, dto.getTeamId())) {
            throw new BadRequestException("You must be a team member to create daily updates");
        }

        // Check if update already exists for this employee, team, and date
        dailyUpdatePostRepository.findByEmployeeIdAndTeamIdAndDate(
                employeeId,
                dto.getTeamId(),
                dto.getDate()).ifPresent(existing -> {
                    throw new BadRequestException(
                            "Daily update already exists for employee " + employeeId +
                                    " in team " + dto.getTeamId() + " on date " + dto.getDate());
                });

        DailyUpdatePost dailyUpdate = DailyUpdatePost.builder()
                .employee(loggedInEmployee)
                .team(team)
                .date(dto.getDate())
                .generalNotes(dto.getGeneralNotes())
                .build();

        dailyUpdate = dailyUpdatePostRepository.save(dailyUpdate);

        if (dto.getTicketMentions() != null && !dto.getTicketMentions().isEmpty()) {
            attachTicketMentions(dailyUpdate, dto.getTicketMentions());
        }

        return dailyUpdate;
    }

    @Transactional
    public DailyUpdatePost update(Long id, DailyUpdateCreateDto dto) {
        DailyUpdatePost dailyUpdate = findById(id);

        // Verify ownership
        Employee loggedInEmployee = employeeService.getMe();
        if (!dailyUpdate.getEmployee().getId().equals(loggedInEmployee.getId())) {
            throw new BadRequestException("You can only update your own daily updates");
        }

        dailyUpdate.setGeneralNotes(dto.getGeneralNotes());

        // Remove existing ticket mentions
        dailyUpdate.getTicketMentions().clear();
        dailyUpdatePostRepository.save(dailyUpdate);

        if (dto.getTicketMentions() != null && !dto.getTicketMentions().isEmpty()) {
            attachTicketMentions(dailyUpdate, dto.getTicketMentions());
        }

        return dailyUpdate;
    }

    @Transactional
    public void deleteById(Long id) {
        DailyUpdatePost dailyUpdate = findById(id);

        // Verify ownership
        Employee loggedInEmployee = employeeService.getMe();
        if (!dailyUpdate.getEmployee().getId().equals(loggedInEmployee.getId())) {
            throw new BadRequestException("You can only delete your own daily updates");
        }

        dailyUpdatePostRepository.delete(dailyUpdate);
    }

    /**
     * Helper to attach ticket mentions to a daily update
     */
    private void attachTicketMentions(DailyUpdatePost dailyUpdate, List<TicketMentionCreateDto> mentionDtos) {
        Long employeeId = dailyUpdate.getEmployee().getId();

        List<DailyUpdateTicketMention> mentions = mentionDtos.stream()
                .map(tm -> {
                    Ticket ticket = ticketService.findById(tm.getTicketId());

                    // Authorization: User must be project member to mention ticket
                    authorizationService.verifyCanMentionTicket(employeeId, ticket);

                    DailyUpdateTicketMention mention = DailyUpdateTicketMention.builder()
                            .dailyUpdate(dailyUpdate)
                            .ticket(ticket)
                            .description(tm.getDescription())
                            .build();
                    return mention;
                })
                .toList();

        dailyUpdate.getTicketMentions().addAll(mentions);
        dailyUpdatePostRepository.save(dailyUpdate);
    }
}
