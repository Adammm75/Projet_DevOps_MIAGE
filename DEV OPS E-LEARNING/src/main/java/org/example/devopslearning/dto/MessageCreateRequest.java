package org.example.devopslearning.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageCreateRequest {

    @NotBlank
    private String recipientEmail;

    private Long courseId;

    private String subject;

    @NotBlank
    private String content;
}
