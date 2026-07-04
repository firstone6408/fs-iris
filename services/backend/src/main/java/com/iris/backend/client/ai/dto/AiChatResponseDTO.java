package com.iris.backend.client.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class AiChatResponseDTO {

    private String reply;

    @JsonProperty("finish_reason")
    private String finishReason;

    @JsonProperty("prompt_tokens")
    private int promptTokens;

    @JsonProperty("completion_tokens")
    private int completionTokens;
}
