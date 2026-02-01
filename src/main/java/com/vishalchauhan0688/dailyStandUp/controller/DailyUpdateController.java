package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.dto.DailyUpdateCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.PageResponse;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.model.DailyUpdatePost;
import com.vishalchauhan0688.dailyStandUp.service.DailyUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) String filter) {
        
        QueryParams params = QueryParams.builder()
                .page(page != null ? page : 0)
                .size(size != null ? size : 20)
                .sort(sort)
                .search(search)
                .filter(filter)
                .build();
        
        PageResponse<DailyUpdatePost> result = dailyUpdateService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success("Daily updates fetched successfully", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DailyUpdatePost>> getById(@PathVariable Long id) {
        DailyUpdatePost update = dailyUpdateService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Daily update fetched successfully", update));
    }

    // Use filter parameter instead: ?filter=employee.id:1,team.id:2,date:2024-01-01

    @PostMapping
    public ResponseEntity<ApiResponse<DailyUpdatePost>> save(@Valid @RequestBody DailyUpdateCreateDto dailyUpdateCreateDto) {
        DailyUpdatePost data = dailyUpdateService.save(dailyUpdateCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Daily update created successfully", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DailyUpdatePost>> update(@PathVariable Long id, @Valid @RequestBody DailyUpdateCreateDto dailyUpdateCreateDto) {
        DailyUpdatePost data = dailyUpdateService.update(id, dailyUpdateCreateDto);
        return ResponseEntity.ok(ApiResponse.success("Daily update updated successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        dailyUpdateService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Daily update deleted successfully", null));
    }
}
