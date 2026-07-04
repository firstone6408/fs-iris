package com.iris.backend.dto.chat;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ChatRequestDTO {

    @NotEmpty(message = "messages must not be empty")
    private List<@Valid MessageDTO> messages;

    private Integer maxTokens;
    private String voice;

    @Getter
    public static class MessageDTO {

        @NotBlank(message = "role is required")
        private String role;

        @NotBlank(message = "content is required")
        private String content;
    }
}
