"""F5-TTS-TH implementation of the TTS Protocol."""

import numpy as np
from f5_tts_th.tts import TTS as _F5TTS

from config.settings import TTSConfig
from core.voice_ref import VoiceRef


class F5TTSEngine:
    """
    TTS engine backed by F5-TTS-TH (zero-shot voice cloning).

    Loads the model once on init and reuses it across all `synthesize` calls.
    Voice identity is fixed at construction time via a VoiceRef.

    Args:
        config: TTS inference parameters (model version, step, cfg, speed, etc.).
        voice: Reference audio + transcription used for voice cloning.
    """

    def __init__(self, config: TTSConfig, voice: VoiceRef) -> None:
        if not voice.audio.exists():
            raise FileNotFoundError(f"Reference audio not found: {voice.audio}")
        if not voice.text.strip():
            raise ValueError(f"VoiceRef '{voice.name}' has no transcription text. Fill in voice_ref.text before using it.")

        self._config = config
        self._voice = voice
        self._tts = _F5TTS(model=config.model)

    def synthesize(self, text: str) -> np.ndarray:
        """
        Generate speech from Thai text using the configured reference voice.

        Args:
            text: Thai text to convert to speech.

        Returns:
            1-D float32 numpy array of the generated waveform at 24000 Hz.
        """
        wav: np.ndarray = self._tts.infer(
            ref_audio=str(self._voice.audio),
            ref_text=self._voice.text,
            gen_text=text,
            step=self._config.step,
            cfg=self._config.cfg,
            speed=self._config.speed,
            max_chars=self._config.max_chars,
        )
        return wav
