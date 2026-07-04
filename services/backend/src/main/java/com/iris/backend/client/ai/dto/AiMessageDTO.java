package com.iris.backend.client.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiMessageDTO {

    private String role;
    private String content;
}
