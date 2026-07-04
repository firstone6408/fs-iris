package com.iris.backend.client.tts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TtsSynthesizeRequestDTO {

    private String text;
    private String voice;
}
