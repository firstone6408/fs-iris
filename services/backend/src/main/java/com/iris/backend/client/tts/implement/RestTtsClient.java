package com.iris.backend.client.tts.implement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.iris.backend.client.tts.TtsClient;
import com.iris.backend.client.tts.dto.TtsSynthesizeRequestDTO;

@Component
public class RestTtsClient implements TtsClient {

    private final RestTemplate restTemplate;

    public RestTtsClient(@Value("${iris.tts.base-url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(baseUrl));
    }

    @Override
    public byte[] synthesize(TtsSynthesizeRequestDTO request) {
        return restTemplate.postForObject("/synthesize", request, byte[].class);
    }
}
