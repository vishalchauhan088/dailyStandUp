package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.dto.PageResponse;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.dto.TicketCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.TicketUpdateDto;
import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import com.vishalchauhan0688.dailyStandUp.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String fields) {
        
        QueryParams params = QueryParams.builder()
                .page(page != null ? page : 0)
                .size(size != null ? size : 20)
                .sort(sort)
                .search(search)
                .filter(filter)
                .fields(fields)
                .build();
        
        PageResponse<Ticket> result = ticketService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success("Tickets fetched successfully", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Ticket>> getById(@PathVariable Long id) {
        Ticket ticket = ticketService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Ticket fetched successfully", ticket));
    }

    // Use filter parameter instead: ?filter=project.id:1,status.id:2,createdBy.id:3,parentTicket.id:4

    @PostMapping
    public ResponseEntity<ApiResponse<Ticket>> save(@Valid @RequestBody TicketCreateDto ticketCreateDto) {
        Ticket ticket = ticketService.save(ticketCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Ticket created successfully", ticket));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Ticket>> update(
            @PathVariable Long id, 
            @Valid @RequestBody TicketUpdateDto ticketUpdateDto) {
        Ticket ticket = ticketService.update(id, ticketUpdateDto);
        return ResponseEntity.ok(ApiResponse.success("Ticket updated successfully", ticket));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Ticket deleted successfully", null));
    }
}
