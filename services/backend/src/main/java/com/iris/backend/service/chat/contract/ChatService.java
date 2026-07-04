package com.iris.backend.service.chat.contract;

import com.iris.backend.dto.chat.ChatRequestDTO;

/**
 * Orchestrates a full voice response cycle: LLM inference → TTS synthesis.
 *
 * <p>Accepts a conversation from the caller, forwards it to the AI service,
 * and pipes the text reply through the TTS service, returning WAV audio bytes.
 */
public interface ChatService {

    /**
     * Process a chat request and return synthesized speech.
     *
     * @param request conversation history, optional max_tokens, and optional voice key
     * @return raw WAV audio bytes ready to stream to the client
     */
    byte[] chat(ChatRequestDTO request);
}
