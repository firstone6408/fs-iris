package com.iris.backend.client.ai.implement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.iris.backend.client.ai.AiClient;
import com.iris.backend.client.ai.dto.AiChatRequestDTO;
import com.iris.backend.client.ai.dto.AiChatResponseDTO;

@Component
public class RestAiClient implements AiClient {

    private final RestTemplate restTemplate;

    public RestAiClient(@Value("${iris.ai.base-url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(baseUrl));
    }

    @Override
    public AiChatResponseDTO chat(AiChatRequestDTO request) {
        return restTemplate.postForObject("/chat", request, AiChatResponseDTO.class);
    }
}
