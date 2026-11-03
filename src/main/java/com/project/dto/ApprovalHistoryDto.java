package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApprovalHistoryDto {
    private Long id;
    private Long requestId;
    private String action;
    private String actionBy;
    private LocalDateTime actionAt;
}