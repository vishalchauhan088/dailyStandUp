package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.dto.DailyUpdateCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.DailyUpdateResponseDto;
import com.vishalchauhan0688.dailyStandUp.dto.PageResponse;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.model.DailyUpdatePost;
import com.vishalchauhan0688.dailyStandUp.service.DailyUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Daily Update management.
 * Returns DTOs instead of entities to avoid lazy-loading issues.
 */
@RestController
@RequestMapping("/api/v1/dailyupdates")
@RequiredArgsConstructor
public class DailyUpdateController {
    private final DailyUpdateService dailyUpdateService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> findAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Long teamId) {

        QueryParams params = QueryParams.builder()
                .page(page != null ? page : 0)
                .size(size != null ? size : 20)
                .sort(sort)
                .search(search)
                .filter(filter)
                .build();

        PageResponse<DailyUpdatePost> result = dailyUpdateService.findAll(params, teamId);

        // Convert to DTOs
        List<DailyUpdateResponseDto> dtos = result.getContent().stream()
                .map(DailyUpdateResponseDto::fromEntity)
                .collect(Collectors.toList());

        PageResponse<DailyUpdateResponseDto> dtoResponse = PageResponse.of(
                dtos, result.getPage(), result.getSize(), result.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success("Daily updates fetched successfully", dtoResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DailyUpdateResponseDto>> getById(@PathVariable Long id) {
        DailyUpdatePost update = dailyUpdateService.findById(id);
        DailyUpdateResponseDto dto = DailyUpdateResponseDto.fromEntity(update);
        return ResponseEntity.ok(ApiResponse.success("Daily update fetched successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DailyUpdateResponseDto>> save(
            @Valid @RequestBody DailyUpdateCreateDto dailyUpdateCreateDto) {
        DailyUpdatePost data = dailyUpdateService.save(dailyUpdateCreateDto);
        DailyUpdateResponseDto dto = DailyUpdateResponseDto.fromEntity(data);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Daily update created successfully", dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DailyUpdateResponseDto>> update(@PathVariable Long id,
            @Valid @RequestBody DailyUpdateCreateDto dailyUpdateCreateDto) {
        DailyUpdatePost data = dailyUpdateService.update(id, dailyUpdateCreateDto);
        DailyUpdateResponseDto dto = DailyUpdateResponseDto.fromEntity(data);
        return ResponseEntity.ok(ApiResponse.success("Daily update updated successfully", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        dailyUpdateService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Daily update deleted successfully", null));
    }
}
