"""Application layer — orchestrates TTS synthesis and output."""

from pathlib import Path

import numpy as np
import soundfile as sf

from core.tts import TTS


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

    def synthesize(self, text: str) -> np.ndarray:
        """
        Generate a waveform from text.

        Args:
            text: Thai text to synthesize.

        Returns:
            1-D float32 numpy array of the audio waveform.
        """
        return self._engine.synthesize(text)

    def save(self, text: str, output_path: Path) -> None:
        """
        Synthesize text and write the result to a WAV file.

        Args:
            text: Thai text to synthesize.
            output_path: Destination .wav file path.
        """
        wav = self._engine.synthesize(text)
        sf.write(str(output_path), wav, self._sample_rate)
