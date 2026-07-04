package com.iris.backend.client.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiChatRequestDTO {

    private List<AiMessageDTO> messages;

    @JsonProperty("max_tokens")
    private Integer maxTokens;
}
