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
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO:
 * 1. return Dto remove double fetching of user detail
 * 2. optimize save and update query (remove if tickt id exists in loop)
 */
@Service
@RequiredArgsConstructor
public class DailyUpdateService {
    private final DailyUpdateRepository dailyUpdateRepository;
    private final EmployeeService employeeService;
    private final TicketRepository ticketRepository;

    public List<DailyUpdate> findAll(){
        return dailyUpdateRepository.findAll();
    }
    public DailyUpdate save(DailyUpdateCreateDto dailyUpdateCreateDto) throws ResourceNotFoundException {

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
    public DailyUpdate update(Long id, DailyUpdateCreateDto dailyUpdateCreateDto) throws ResourceNotFoundException {
        Employee loggedInEmployee = employeeService.getMe();
        DailyUpdate dailyUpdateEntity = dailyUpdateRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("daily update with id " + id +" is not found !",403));

        dailyUpdateEntity.getTicketMentions().clear();
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
        for(var tm : ticketMentions){
            dailyUpdateEntity.getTicketMentions().add(tm);
        }
        return  dailyUpdateRepository.save(dailyUpdateEntity);

    }

    public void deleteById(Long id) {
        dailyUpdateRepository.deleteById(id);
    }
}
