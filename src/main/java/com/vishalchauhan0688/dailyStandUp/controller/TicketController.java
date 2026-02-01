package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.dto.PageResponse;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.dto.TicketCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.TicketResponseDto;
import com.vishalchauhan0688.dailyStandUp.dto.TicketUpdateDto;
import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import com.vishalchauhan0688.dailyStandUp.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Ticket management.
 * Returns DTOs instead of entities to avoid lazy-loading issues.
 */
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

        // Convert to DTOs
        List<TicketResponseDto> dtos = result.getContent().stream()
                .map(TicketResponseDto::fromEntity)
                .collect(Collectors.toList());

        PageResponse<TicketResponseDto> dtoResponse = PageResponse.of(
                dtos, result.getPage(), result.getSize(), result.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success("Tickets fetched successfully", dtoResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponseDto>> getById(@PathVariable Long id) {
        Ticket ticket = ticketService.findById(id);
        TicketResponseDto dto = TicketResponseDto.fromEntity(ticket);
        return ResponseEntity.ok(ApiResponse.success("Ticket fetched successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponseDto>> save(@Valid @RequestBody TicketCreateDto ticketCreateDto) {
        Ticket ticket = ticketService.save(ticketCreateDto);
        TicketResponseDto dto = TicketResponseDto.fromEntity(ticket);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Ticket created successfully", dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody TicketUpdateDto ticketUpdateDto) {
        Ticket ticket = ticketService.update(id, ticketUpdateDto);
        TicketResponseDto dto = TicketResponseDto.fromEntity(ticket);
        return ResponseEntity.ok(ApiResponse.success("Ticket updated successfully", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Ticket deleted successfully", null));
    }
}
