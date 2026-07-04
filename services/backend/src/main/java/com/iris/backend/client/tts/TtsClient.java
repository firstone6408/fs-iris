package com.iris.backend.client.tts;

import com.iris.backend.client.tts.dto.TtsSynthesizeRequestDTO;

/**
 * Client for the TTS service.
 *
 * <p>Sends text to the TTS service and returns WAV audio bytes.
 */
public interface TtsClient {

    /**
     * Synthesize speech from text.
     *
     * @param request text and optional voice key
     * @return raw WAV audio bytes
     */
    byte[] synthesize(TtsSynthesizeRequestDTO request);
}
