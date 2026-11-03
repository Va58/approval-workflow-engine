package com.project;

import com.project.dto.ApprovalHistoryDto;
import com.project.dto.RequestDetailsDto;
import com.project.entity.ApprovalStep;
import com.project.entity.Request;
import com.project.entity.User;
import com.project.enums.Role;
import com.project.repository.ApprovalStepRepository;
import com.project.repository.UserRepository;
import com.project.service.ApprovalService;
import com.project.service.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class WorkflowIntegrationTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApprovalStepRepository approvalStepRepository;

    private User requester;
    private User approver;
    private User admin;

    @BeforeEach
    void setupData() {
        requester = userRepository.findByEmail("om@test.com")
                .orElseGet(() -> userRepository.save(new User(null, "om", "om@test.com", "password123", Role.REQUESTER)));

        approver = userRepository.findByEmail("sai@test.com")
                .orElseGet(() -> userRepository.save(new User(null, "sai", "sai@test.com", "password123", Role.APPROVER)));

        admin = userRepository.findByEmail("ram@test.com")
                .orElseGet(() -> userRepository.save(new User(null, "ram", "ram@test.com", "password123", Role.ADMIN)));

        if (approvalStepRepository.findByRequestTypeOrderByStepOrderAsc("LEAVE").isEmpty()) {
            approvalStepRepository.save(new ApprovalStep(null, "LEAVE", 1, Role.APPROVER));
            approvalStepRepository.save(new ApprovalStep(null, "LEAVE", 2, Role.ADMIN));
        }
    }

    @Test
    void testCompleteApprovalFlow() {
        Request request = requestService.createRequest("LEAVE", requester);
        assertEquals("PENDING", request.getStatus());

        Request afterStep1 = approvalService.approveRequest(request.getId(), approver);
        assertEquals("PENDING", afterStep1.getStatus());

        Request finalRequest = approvalService.approveRequest(request.getId(), admin);
        assertEquals("APPROVED", finalRequest.getStatus());
    }

    @Test
    void testRequesterCannotApproveOwnRequest() {
        Request request = requestService.createRequest("LEAVE", requester);

        assertThrows(IllegalArgumentException.class, () ->
                approvalService.approveRequest(request.getId(), requester)
        );
    }

    @Test
    void testRejectRequest() {
        Request request = requestService.createRequest("LEAVE", requester);

        Request rejected = approvalService.rejectRequest(request.getId(), approver);
        assertEquals("REJECTED", rejected.getStatus());
    }

    @Test
    void testRequestDetailsAndHistory() {
        Request request = requestService.createRequest("LEAVE", requester);

        RequestDetailsDto details = requestService.getRequestDetails(request.getId());
        assertEquals("PENDING", details.getStatus());
        assertEquals(1, details.getCurrentStep());
        assertEquals("APPROVER", details.getRequiredRole());

        List<ApprovalHistoryDto> history = requestService.getRequestHistory(request.getId());
        assertFalse(history.isEmpty());
        assertEquals("SUBMITTED", history.get(0).getAction());
    }
}