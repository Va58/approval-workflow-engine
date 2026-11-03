package com.project.config;

import com.project.entity.ApprovalStep;
import com.project.entity.User;
import com.project.enums.Role;
import com.project.repository.ApprovalStepRepository;
import com.project.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   ApprovalStepRepository approvalStepRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User requester = new User(null, "om", "om@test.com", "password123", Role.REQUESTER);
                User approver = new User(null, "sai", "sai@test.com", "password123", Role.APPROVER);
                User admin = new User(null, "ram", "ram@test.com", "password123", Role.ADMIN);

                userRepository.saveAll(List.of(requester, approver, admin));
            }

            if (approvalStepRepository.count() == 0) {
                ApprovalStep leaveStep1 = new ApprovalStep(null, "LEAVE", 1, Role.APPROVER);
                ApprovalStep leaveStep2 = new ApprovalStep(null, "LEAVE", 2, Role.ADMIN);

                ApprovalStep expenseStep1 = new ApprovalStep(null, "EXPENSE", 1, Role.APPROVER);
                ApprovalStep expenseStep2 = new ApprovalStep(null, "EXPENSE", 2, Role.ADMIN);

                approvalStepRepository.saveAll(List.of(leaveStep1, leaveStep2, expenseStep1, expenseStep2));
            }
        };
    }
}
