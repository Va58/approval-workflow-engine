package com.project;

import com.project.entity.ApprovalStep;
import com.project.entity.Request;
import com.project.entity.User;
import com.project.enums.Role;
import com.project.repository.ApprovalStepRepository;
import com.project.repository.UserRepository;
import com.project.security.JwtUtil;
import com.project.service.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class ApiControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApprovalStepRepository approvalStepRepository;

    @Autowired
    private RequestService requestService;

    @Autowired
    private JwtUtil jwtUtil;

    private String requesterToken;
    private Long testRequestId;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        User requester = userRepository.findByEmail("om@test.com")
                .orElseGet(() -> userRepository.save(new User(null, "om", "om@test.com", "password123", Role.REQUESTER)));

        userRepository.findByEmail("sai@test.com")
                .orElseGet(() -> userRepository.save(new User(null, "sai", "sai@test.com", "password123", Role.APPROVER)));

        userRepository.findByEmail("ram@test.com")
                .orElseGet(() -> userRepository.save(new User(null, "ram", "ram@test.com", "password123", Role.ADMIN)));

        if (approvalStepRepository.findByRequestTypeOrderByStepOrderAsc("LEAVE").isEmpty()) {
            approvalStepRepository.save(new ApprovalStep(null, "LEAVE", 1, Role.APPROVER));
            approvalStepRepository.save(new ApprovalStep(null, "LEAVE", 2, Role.ADMIN));
        }

        Request req = requestService.createRequest("LEAVE", requester);
        testRequestId = req.getId();

        requesterToken = "Bearer " + jwtUtil.generateToken("om@test.com", "REQUESTER");
    }

    @Test
    void testLoginApi() throws Exception {
        String loginJson = "{\"email\":\"om@test.com\",\"password\":\"password123\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("om@test.com"));
    }

    @Test
    void testCreateRequestApi() throws Exception {
        String body = "{\"type\":\"LEAVE\"}";

        mockMvc.perform(post("/requests")
                        .header("Authorization", requesterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.type").value("LEAVE"));
    }

    @Test
    void testGetRequestByIdApi() throws Exception {
        mockMvc.perform(get("/requests/" + testRequestId)
                        .header("Authorization", requesterToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetRequestHistoryApi() throws Exception {
        mockMvc.perform(get("/requests/history/" + testRequestId)
                        .header("Authorization", requesterToken))
                .andExpect(status().isOk());
    }

    @Test
    void testUnauthenticatedBlocked() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"LEAVE\"}"))
                .andExpect(status().isForbidden());
    }
}