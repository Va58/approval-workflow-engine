package com.project.service;

import com.project.entity.*;
import com.project.enums.Role;
import com.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final RequestRepository requestRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;

    @Transactional
    public Request approveRequest(Long requestId, User actionBy) {
        Request request = findPendingRequest(requestId);

        validateRequester(request, actionBy);

        List<ApprovalStep> steps = getApprovalSteps(request);

        int currentStepIndex = getCurrentStepIndex(requestId, steps.size());

        validateApprover(actionBy, steps.get(currentStepIndex));

        ApprovalHistory history = new ApprovalHistory();
        history.setRequest(request);
        history.setAction("APPROVED");
        history.setActionBy(actionBy);
        history.setActionAt(LocalDateTime.now());
        approvalHistoryRepository.save(history);

        if (currentStepIndex + 1 == steps.size()) {
            request.setStatus("APPROVED");
            return requestRepository.save(request);
        }

        return request;
    }

    @Transactional
    public Request rejectRequest(Long requestId, User actionBy) {
        Request request = findPendingRequest(requestId);

        validateRequester(request, actionBy);

        List<ApprovalStep> steps = getApprovalSteps(request);

        int currentStepIndex = getCurrentStepIndex(requestId, steps.size());

        validateApprover(actionBy, steps.get(currentStepIndex));

        ApprovalHistory history = new ApprovalHistory();
        history.setRequest(request);
        history.setAction("REJECTED");
        history.setActionBy(actionBy);
        history.setActionAt(LocalDateTime.now());
        approvalHistoryRepository.save(history);

        request.setStatus("REJECTED");
        return requestRepository.save(request);
    }

    private Request findPendingRequest(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("Request is not pending");
        }

        return request;
    }

    private void validateRequester(Request request, User actionBy) {
        if (request.getCreatedBy().getId().equals(actionBy.getId())) {
            throw new IllegalArgumentException("You cannot approve or reject your own request");
        }
    }

    private List<ApprovalStep> getApprovalSteps(Request request) {
        List<ApprovalStep> steps = approvalStepRepository
                .findByRequestTypeOrderByStepOrderAsc(request.getType());

        if (steps.isEmpty()) {
            throw new IllegalArgumentException("No approval workflow configured");
        }

        return steps;
    }

    private int getCurrentStepIndex(Long requestId, int totalSteps) {
        List<ApprovalHistory> history = approvalHistoryRepository
                .findByRequestIdOrderByActionAtAsc(requestId);

        long approvedCount = history.stream()
                .filter(h -> "APPROVED".equals(h.getAction()))
                .count();

        if (approvedCount >= totalSteps) {
            throw new IllegalArgumentException("All approval steps are already completed");
        }

        return (int) approvedCount;
    }

    private void validateApprover(User actionBy, ApprovalStep currentStep) {
        if (actionBy.getRole() != Role.ADMIN && actionBy.getRole() != currentStep.getRole()) {
            throw new IllegalArgumentException("You are not authorized to perform this action");
        }
    }
}
