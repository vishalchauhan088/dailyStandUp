package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.LoginRequestDto;
import com.vishalchauhan0688.dailyStandUp.dto.TicketCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.TicketUpdateDto;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import com.vishalchauhan0688.dailyStandUp.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    @PostMapping
    public ResponseEntity<Ticket> save(@RequestBody TicketCreateDto ticketCreateDto) throws ResourceNotFoundException {
        return ResponseEntity.ok(ticketService.save(ticketCreateDto));
    }
    @GetMapping
    public ResponseEntity<List<Ticket>> getAll() {
        return ResponseEntity.ok(ticketService.findAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getById(@PathVariable Long id) throws ResourceNotFoundException {
        return ResponseEntity.ok(ticketService.findById(id));
    }

    /**
     * TODO: Authorize that only owner of ticket can update ticket or role higher than current user
     * @param id
     * @param ticketUpdateDto
     * @return
     * @throws ResourceNotFoundException
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Ticket> update(@PathVariable Long id, @RequestBody TicketUpdateDto ticketUpdateDto) throws ResourceNotFoundException {
        return ResponseEntity.ok(ticketService.update(id,ticketUpdateDto));
    }

}
