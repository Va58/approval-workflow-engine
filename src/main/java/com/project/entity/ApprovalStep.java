package com.project.entity;

import com.project.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "approval_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestType;

    private Integer stepOrder;

    @Enumerated(EnumType.STRING)
    private Role role;
}