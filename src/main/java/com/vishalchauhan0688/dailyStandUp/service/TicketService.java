package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.Enum.TicketStatus;
import com.vishalchauhan0688.dailyStandUp.dto.LoginRequestDto;
import com.vishalchauhan0688.dailyStandUp.dto.TicketCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.TicketUpdateDto;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeRepository;
import com.vishalchauhan0688.dailyStandUp.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    public Ticket findById(Long id) throws ResourceNotFoundException {
        return ticketRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Ticket not found , id: " + id,403));

    }
    public List<Ticket> findAll() {
        return ticketRepository.findAll();

    }
    public Ticket save(TicketCreateDto ticketCreateDto) throws ResourceNotFoundException {
        Ticket ticket = new Ticket();
        ticket.setExternalId(ticketCreateDto.getExternalId());
        ticket.setTitle(ticketCreateDto.getTitle());
        ticket.setStatus(TicketStatus.TODO);
        ticket.setCreatedBy(employeeService.getMe());
        return ticketRepository.save(ticket);
    }

    public Ticket update(Long id, TicketUpdateDto ticketUpdateDto) throws ResourceNotFoundException {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No ticket with id: " + id,403));
        this.updateTicketFromDto(ticket, ticketUpdateDto);
        if(ticketUpdateDto.getEmployeeId() != null) {
            ticket.setCreatedBy(
                    employeeRepository.findById(ticketUpdateDto.getEmployeeId()).orElseThrow(
                            ()-> new ResourceNotFoundException("LoggedIn user not found: " + ticketUpdateDto.getEmployeeId(),403)
                    )
            );
        }
        return ticketRepository.save(ticket);
    }
    private void updateTicketFromDto(Ticket ticket, TicketUpdateDto dto) {
        if(dto.getTitle() != null) {ticket.setTitle(dto.getTitle());}
        if(dto.getExternalId() != null) {ticket.setExternalId(dto.getExternalId());}
        if(dto.getStatus() != null) {ticket.setStatus(dto.getStatus());}
    }

}
