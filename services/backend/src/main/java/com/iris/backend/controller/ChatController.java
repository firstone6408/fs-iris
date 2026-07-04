package com.iris.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iris.backend.dto.chat.ChatRequestDTO;
import com.iris.backend.service.chat.contract.ChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * POST /api/chat/voice — send a conversation to the LLM and return the reply as WAV audio.
     *
     * <p>Flow: request → AI service (LLM) → TTS service → audio/wav response.
     */
    @PostMapping("/voice")
    public ResponseEntity<byte[]> voice(@Valid @RequestBody ChatRequestDTO request) {
        byte[] audio = chatService.chat(request);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav"))
                .body(audio);
    }
}
