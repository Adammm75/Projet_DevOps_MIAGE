package org.example.devopslearning.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseCreateRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String title;

    private String description;
}
