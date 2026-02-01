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
import com.vishalchauhan0688.dailyStandUp.util.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<DailyUpdatePost> findAll() {
        return dailyUpdatePostRepository.findAll();
    }

    public PageResponse<DailyUpdatePost> findAll(QueryParams params) {
        Page<DailyUpdatePost> page = queryService.query(
                dailyUpdatePostRepository,
                params,
                this::dailyUpdateSearchSpec
        );

        return PageResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    /**
     * Defines HOW search works for DailyUpdatePost
     */
    private Specification<DailyUpdatePost> dailyUpdateSearchSpec(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        String searchTerm = "%" + search.toLowerCase() + "%";

        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("generalNotes")),
                        searchTerm
                );
    }

    public DailyUpdatePost findById(Long id) {
        return dailyUpdatePostRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Daily update not found with id: " + id
                        )
                );
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
                dto.getDate()
        ).ifPresent(existing -> {
            throw new BadRequestException(
                "Daily update already exists for employee " + employeeId + 
                " in team " + dto.getTeamId() + " on date " + dto.getDate()
            );
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
