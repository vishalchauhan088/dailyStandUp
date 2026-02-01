package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.dto.DailyUpdateCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.PageResponse;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.DailyUpdate;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import com.vishalchauhan0688.dailyStandUp.model.TicketMention;
import com.vishalchauhan0688.dailyStandUp.repository.DailyUpdateRepository;
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

    private final DailyUpdateRepository dailyUpdateRepository;
    private final EmployeeService employeeService;
    private final TicketService ticketService;
    private final QueryService queryService;

    public List<DailyUpdate> findAll() {
        return dailyUpdateRepository.findAll();
    }

    public PageResponse<DailyUpdate> findAll(QueryParams params) {

        Page<DailyUpdate> page = queryService.query(
                dailyUpdateRepository,
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
     * Defines HOW search works for DailyUpdate
     */
    private Specification<DailyUpdate> dailyUpdateSearchSpec(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        String searchTerm = "%" + search.toLowerCase() + "%";

        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("generalDescription")),
                        searchTerm
                );
    }

    public DailyUpdate findById(Long id) {
        return dailyUpdateRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Daily update not found with id: " + id
                        )
                );
    }

    @Transactional
    public DailyUpdate save(DailyUpdateCreateDto dto) {

        Employee loggedInEmployee = employeeService.getMe();

        DailyUpdate dailyUpdate = new DailyUpdate();
        dailyUpdate.setEmployee(loggedInEmployee);
        dailyUpdate.setGeneralDescription(dto.getGeneralDesciption());

        dailyUpdate = dailyUpdateRepository.save(dailyUpdate);

        if (dto.getTicketMentions() != null && !dto.getTicketMentions().isEmpty()) {
            attachTicketMentions(dailyUpdate, dto);
        }

        return dailyUpdate;
    }

    @Transactional
    public DailyUpdate update(Long id, DailyUpdateCreateDto dto) {

        DailyUpdate dailyUpdate = findById(id);
        dailyUpdate.setGeneralDescription(dto.getGeneralDesciption());

        dailyUpdate.getTicketMentions().clear();
        dailyUpdateRepository.save(dailyUpdate);

        if (dto.getTicketMentions() != null && !dto.getTicketMentions().isEmpty()) {
            attachTicketMentions(dailyUpdate, dto);
        }

        return dailyUpdate;
    }

    @Transactional
    public void deleteById(Long id) {
        DailyUpdate dailyUpdate = findById(id);
        dailyUpdateRepository.delete(dailyUpdate);
    }

    /**
     * Helper to attach ticket mentions to a daily update
     */
    private void attachTicketMentions(DailyUpdate dailyUpdate, DailyUpdateCreateDto dto) {

        List<TicketMention> mentions = dto.getTicketMentions()
                .stream()
                .map(tm -> {
                    Ticket ticket = ticketService.findById(tm.getTicketId());
                    TicketMention mention = new TicketMention();
                    mention.setTicket(ticket);
                    mention.setPost(dailyUpdate);
                    mention.setDescription(tm.getDescription());
                    return mention;
                })
                .toList();

        dailyUpdate.setTicketMentions(mentions);
        dailyUpdateRepository.save(dailyUpdate);
    }
}
