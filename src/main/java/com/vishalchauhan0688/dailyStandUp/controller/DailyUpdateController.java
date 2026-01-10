package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.DailyUpdateCreateDto;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.service.DailyUpdateService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dailyupdates")
@RequiredArgsConstructor
public class DailyUpdateController {
    private final DailyUpdateService dailyUpdateService;
    @GetMapping
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok(dailyUpdateService.findAll());
    }
    @PostMapping
    public ResponseEntity<?> save(@RequestBody DailyUpdateCreateDto dailyUpdateCreateDto) throws ResourceNotFoundException {
        return ResponseEntity.ok(dailyUpdateService.save(dailyUpdateCreateDto));
    }

}
