package com.iris.backend.client.ai;

import com.iris.backend.client.ai.dto.AiChatRequestDTO;
import com.iris.backend.client.ai.dto.AiChatResponseDTO;

/**
 * Client for the AI (LLM) service.
 *
 * <p>Sends a conversation to the AI service and returns the model's reply.
 */
public interface AiClient {

    /**
     * Send a chat request to the AI service.
     *
     * @param request messages and optional max_tokens
     * @return the model's reply with token usage info
     */
    AiChatResponseDTO chat(AiChatRequestDTO request);
}
