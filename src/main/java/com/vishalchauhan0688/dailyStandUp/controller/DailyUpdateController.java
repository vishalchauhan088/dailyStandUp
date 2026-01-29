package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponseDto;
import com.vishalchauhan0688.dailyStandUp.dto.DailyUpdateCreateDto;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.DailyUpdate;
import com.vishalchauhan0688.dailyStandUp.service.DailyUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dailyupdates")
@RequiredArgsConstructor
public class DailyUpdateController {
    private final DailyUpdateService dailyUpdateService;
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<DailyUpdate>>> findAll(){
        List<DailyUpdate> updates = dailyUpdateService.findAll();

        ApiResponseDto<List<DailyUpdate>> response =
                new ApiResponseDto<>(
                        200,
                        "Daily updates fetched successfully",
                        updates
                );

        return ResponseEntity.ok(response);
    }
    @PostMapping
    public ResponseEntity<ApiResponseDto<DailyUpdate>> save(@RequestBody DailyUpdateCreateDto dailyUpdateCreateDto) throws ResourceNotFoundException {
        DailyUpdate data = dailyUpdateService.save(dailyUpdateCreateDto);
        ApiResponseDto<DailyUpdate> response =
                new ApiResponseDto<>(
                        200,
                        "Daily update created successfully",
                        data
                );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<DailyUpdate>> update(@PathVariable Long id, @RequestBody DailyUpdateCreateDto dailyUpdateCreateDto) throws ResourceNotFoundException {
        DailyUpdate data = dailyUpdateService.update(id,dailyUpdateCreateDto);
        ApiResponseDto<DailyUpdate> response =
                new ApiResponseDto<>(
                        200,
                        "Daily update updated successfully",
                        data
                );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable Long id){

        dailyUpdateService.deleteById(id);
        ApiResponseDto<Void> response =
                new ApiResponseDto<>(
                        204,
                        "Daily update deleted successfully",
                        null
                );
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(response);
    }

}
