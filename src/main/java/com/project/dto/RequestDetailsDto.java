package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class RequestDetailsDto {
    private Long id;
    private String type;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private Integer currentStep;
    private String requiredRole;
}