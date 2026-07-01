"""Application layer — orchestrates TTS synthesis and output."""

from __future__ import annotations

from pathlib import Path
from typing import TYPE_CHECKING

import numpy as np
import soundfile as sf

from core.tts import TTS

if TYPE_CHECKING:
    from core.voice_ref import VoiceRef


class TTSService:
    """
    High-level TTS service used by the presentation layer.

    Wraps a TTS engine and adds output utilities (save to file).
    Does not know which engine is used — depends only on the TTS Protocol.

    Args:
        engine: Any object satisfying the TTS Protocol.
        sample_rate: Audio sample rate for WAV output. Must match the engine's output rate.
    """

    def __init__(self, engine: TTS, sample_rate: int = 24000) -> None:
        self._engine = engine
        self._sample_rate = sample_rate

    def synthesize(self, text: str, voice: VoiceRef | None = None) -> np.ndarray:
        """
        Generate a waveform from text.

        Args:
            text: Thai text to synthesize.
            voice: Reference voice override. Uses engine's default if None.

        Returns:
            1-D float32 numpy array of the audio waveform.
        """
        return self._engine.synthesize(text, voice)

    def synthesize_wav_bytes(self, text: str, voice: VoiceRef | None = None) -> bytes:
        """
        Synthesize text and return the result as WAV bytes.

        Useful for HTTP responses where writing to a file is not needed.

        Args:
            text: Thai text to synthesize.
            voice: Reference voice override. Uses engine's default if None.

        Returns:
            WAV file contents as bytes.
        """
        import io
        wav = self._engine.synthesize(text, voice)
        buffer = io.BytesIO()
        sf.write(buffer, wav, self._sample_rate, format="WAV")
        buffer.seek(0)
        return buffer.read()

    def save(self, text: str, output_path: Path, voice: VoiceRef | None = None) -> None:
        """
        Synthesize text and write the result to a WAV file.

        Args:
            text: Thai text to synthesize.
            output_path: Destination .wav file path.
            voice: Reference voice override. Uses engine's default if None.
        """
        wav = self._engine.synthesize(text, voice)
        sf.write(str(output_path), wav, self._sample_rate)
