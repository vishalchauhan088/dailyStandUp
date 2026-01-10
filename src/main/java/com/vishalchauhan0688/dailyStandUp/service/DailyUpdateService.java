package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.dto.DailyUpdateCreateDto;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.DailyUpdate;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import com.vishalchauhan0688.dailyStandUp.model.TicketMention;
import com.vishalchauhan0688.dailyStandUp.repository.DailyUpdateRepository;
import com.vishalchauhan0688.dailyStandUp.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyUpdateService {
    private final DailyUpdateRepository dailyUpdateRepository;
    private final EmployeeService employeeService;
    private final TicketRepository ticketRepository;

    public List<DailyUpdate> findAll(){
        return dailyUpdateRepository.findAll();
    }
    public DailyUpdate
    save(DailyUpdateCreateDto dailyUpdateCreateDto) throws ResourceNotFoundException {

        Employee loggedInEmployee = employeeService.getMe();
        DailyUpdate dailyUpdateEntity = new DailyUpdate();
        dailyUpdateEntity.setEmployee(loggedInEmployee);
        dailyUpdateEntity.setGeneralDescription(dailyUpdateCreateDto.getGeneralDesciption());

        List<TicketMention> ticketMentions = dailyUpdateCreateDto.getTicketMentions()
                .stream()
                .map(tm ->{
                    Ticket ticket = ticketRepository.findById(tm.getTicketId())
                            .orElseThrow(
                                    ()-> new RuntimeException("Ticket Not Found")
                            );
                    TicketMention ticketMention = new TicketMention();
                    ticketMention.setTicket(ticket);
                    ticketMention.setDescription(tm.getDescription());
                    return  ticketMention;
                })
                .toList();
        dailyUpdateEntity.setTicketMentions(ticketMentions);
        return  dailyUpdateRepository.save(dailyUpdateEntity);

    }
}
