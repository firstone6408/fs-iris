"""F5-TTS-TH implementation of the TTS Protocol."""

import numpy as np
from f5_tts_th.tts import TTS as _F5TTS

from config.settings import TTSConfig
from core.voice_ref import VoiceRef


class F5TTSEngine:
    """
    TTS engine backed by F5-TTS-TH (zero-shot voice cloning).

    Loads the model once on init and reuses it across all `synthesize` calls.
    A default voice is set at construction time; individual calls can override it.

    Args:
        config: TTS inference parameters (model version, step, cfg, speed, etc.).
        default_voice: Fallback reference audio + transcription when no voice is passed to synthesize().
    """

    def __init__(self, config: TTSConfig, default_voice: VoiceRef) -> None:
        self._validate_voice(default_voice)
        self._config = config
        self._default_voice = default_voice
        self._tts = _F5TTS(model=config.model)

    def synthesize(self, text: str, voice: VoiceRef | None = None) -> np.ndarray:
        """
        Generate speech from Thai text.

        Args:
            text: Thai text to convert to speech.
            voice: Reference voice to use. Falls back to default_voice if None.

        Returns:
            1-D float32 numpy array of the generated waveform at 24000 Hz.
        """
        v = voice or self._default_voice
        wav: np.ndarray = self._tts.infer(
            ref_audio=str(v.audio),
            ref_text=v.text,
            gen_text=text,
            step=self._config.step,
            cfg=self._config.cfg,
            speed=self._config.speed,
            max_chars=self._config.max_chars,
        )
        return wav

    @staticmethod
    def _validate_voice(voice: VoiceRef) -> None:
        if not voice.audio.exists():
            raise FileNotFoundError(f"Reference audio not found: {voice.audio}")
        if not voice.text.strip():
            raise ValueError(f"VoiceRef '{voice.name}' has no transcription text. Fill in voice_ref.text before using it.")
