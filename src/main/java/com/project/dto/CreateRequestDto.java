package com.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRequestDto {

    @NotBlank(message = "Request type is required")
    private String type;


}