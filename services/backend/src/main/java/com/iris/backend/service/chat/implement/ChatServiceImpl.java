package com.iris.backend.service.chat.implement;

import java.util.List;

import org.springframework.stereotype.Service;

import com.iris.backend.client.ai.AiClient;
import com.iris.backend.client.ai.dto.AiChatRequestDTO;
import com.iris.backend.client.ai.dto.AiChatResponseDTO;
import com.iris.backend.client.ai.dto.AiMessageDTO;
import com.iris.backend.client.tts.TtsClient;
import com.iris.backend.client.tts.dto.TtsSynthesizeRequestDTO;
import com.iris.backend.dto.chat.ChatRequestDTO;
import com.iris.backend.service.chat.contract.ChatService;

@Service
public class ChatServiceImpl implements ChatService {

    private final AiClient aiClient;
    private final TtsClient ttsClient;

    public ChatServiceImpl(AiClient aiClient, TtsClient ttsClient) {
        this.aiClient = aiClient;
        this.ttsClient = ttsClient;
    }

    @Override
    public byte[] chat(ChatRequestDTO request) {
        List<AiMessageDTO> messages = request.getMessages().stream()
                .map(m -> new AiMessageDTO(m.getRole(), m.getContent()))
                .toList();

        AiChatResponseDTO aiResponse = aiClient.chat(
                new AiChatRequestDTO(messages, request.getMaxTokens()));

        return ttsClient.synthesize(
                new TtsSynthesizeRequestDTO(aiResponse.getReply(), request.getVoice()));
    }
}
