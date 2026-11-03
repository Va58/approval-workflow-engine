package com.project.controller;

import com.project.dto.ApprovalHistoryDto;
import com.project.dto.CreateRequestDto;
import com.project.dto.RequestDetailsDto;
import com.project.entity.Request;
import com.project.entity.User;
import com.project.repository.UserRepository;
import com.project.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;
    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('REQUESTER')")
    public Request createRequest(@Valid @RequestBody CreateRequestDto dto, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return requestService.createRequest(dto.getType(), user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('REQUESTER', 'APPROVER', 'ADMIN')")
    public RequestDetailsDto getRequestById(@PathVariable Long id) {
        return requestService.getRequestDetails(id);
    }

    @GetMapping("/history/{id}")
    @PreAuthorize("hasAnyRole('REQUESTER', 'ADMIN')")
    public List<ApprovalHistoryDto> getRequestHistory(@PathVariable Long id) {
        return requestService.getRequestHistory(id);
    }
}
