"""TTS Protocol — the interface all TTS engines must satisfy."""

from __future__ import annotations

from typing import TYPE_CHECKING, Protocol

import numpy as np

if TYPE_CHECKING:
    from core.voice_ref import VoiceRef


class TTS(Protocol):
    """
    Abstract interface for a Text-to-Speech engine.

    Any class that implements `synthesize` satisfies this Protocol,
    enabling the application layer to swap TTS backends without code changes.
    """

    def synthesize(self, text: str, voice: VoiceRef | None = None) -> np.ndarray:
        """
        Convert text to a waveform.

        Args:
            text: Thai text to synthesize.
            voice: Reference voice to use. Falls back to the engine's default voice if None.

        Returns:
            1-D float32 numpy array representing the audio waveform.
        """
        ...
