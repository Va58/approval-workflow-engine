package com.project.service;

import com.project.dto.ApprovalHistoryDto;
import com.project.dto.RequestDetailsDto;
import com.project.entity.*;
import com.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;

    @Transactional
    public Request createRequest(String type, User createdBy) {
        List<ApprovalStep> steps = approvalStepRepository.findByRequestTypeOrderByStepOrderAsc(type);
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("No approval workflow configured for this request type");
        }

        Request request = new Request();
        request.setType(type);
        request.setStatus("PENDING");
        request.setCreatedBy(createdBy);
        request.setCreatedAt(LocalDateTime.now());

        Request savedRequest = requestRepository.save(request);

        ApprovalHistory history = new ApprovalHistory();
        history.setRequest(savedRequest);
        history.setAction("SUBMITTED");
        history.setActionBy(createdBy);
        history.setActionAt(LocalDateTime.now());

        approvalHistoryRepository.save(history);
        return savedRequest;
    }

    public RequestDetailsDto getRequestDetails(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        List<ApprovalStep> steps = approvalStepRepository.findByRequestTypeOrderByStepOrderAsc(request.getType());
        List<ApprovalHistory> history = approvalHistoryRepository.findByRequestIdOrderByActionAtAsc(requestId);

        int approvedCount = 0;
        for (ApprovalHistory h : history) {
            if ("APPROVED".equals(h.getAction())) {
                approvedCount++;
            }
        }

        Integer currentStepNumber = null;
        String requiredRole = null;

        if ("PENDING".equals(request.getStatus()) && approvedCount < steps.size()) {
            ApprovalStep nextStep = steps.get(approvedCount);
            currentStepNumber = nextStep.getStepOrder();
            requiredRole = nextStep.getRole().name();
        }

        return new RequestDetailsDto(
                request.getId(),
                request.getType(),
                request.getStatus(),
                request.getCreatedBy().getEmail(),
                request.getCreatedAt(),
                currentStepNumber,
                requiredRole
        );
    }

    public List<ApprovalHistoryDto> getRequestHistory(Long requestId) {
        requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        List<ApprovalHistory> history = approvalHistoryRepository.findByRequestIdOrderByActionAtAsc(requestId);
        List<ApprovalHistoryDto> dtoList = new ArrayList<>();

        for (ApprovalHistory h : history) {
            dtoList.add(new ApprovalHistoryDto(
                    h.getId(),
                    h.getRequest().getId(),
                    h.getAction(),
                    h.getActionBy().getEmail(),
                    h.getActionAt()
            ));
        }

        return dtoList;
    }
}
