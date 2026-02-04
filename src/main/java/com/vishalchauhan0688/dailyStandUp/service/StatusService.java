package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Status;
import com.vishalchauhan0688.dailyStandUp.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatusService {
    private final StatusRepository statusRepository;

    public List<Status> findAll() {
        return statusRepository.findAll();
    }

    public Status findById(Long id) {
        return statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found with id: " + id));
    }

    public Status findByStatus(String status) {
        return statusRepository.findByStatus(status)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found: " + status));
    }

    @Transactional
    public Status save(Status status) {
        if (statusRepository.existsByStatus(status.getStatus())) {
            throw new BadRequestException("Status already exists: " + status.getStatus());
        }
        return statusRepository.save(status);
    }

    @Transactional
    public Status update(Long id, Status status) {
        Status existing = findById(id);
        if (!existing.getStatus().equals(status.getStatus()) && 
            statusRepository.existsByStatus(status.getStatus())) {
            throw new BadRequestException("Status already exists: " + status.getStatus());
        }
        existing.setStatus(status.getStatus());
        if (status.getDescription() != null) {
            existing.setDescription(status.getDescription());
        }
        return statusRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Status status = findById(id);
        statusRepository.delete(status);
    }
}
