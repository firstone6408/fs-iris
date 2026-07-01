"""TTS Protocol — the interface all TTS engines must satisfy."""

from typing import Protocol
import numpy as np


class TTS(Protocol):
    """
    Abstract interface for a Text-to-Speech engine.

    Any class that implements `synthesize` satisfies this Protocol,
    enabling the application layer to swap TTS backends without code changes.
    """

    def synthesize(self, text: str) -> np.ndarray:
        """
        Convert text to a waveform.

        Args:
            text: Thai text to synthesize.

        Returns:
            1-D float32 numpy array representing the audio waveform.
        """
        ...
