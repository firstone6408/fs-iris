"""Domain object representing a reference audio sample paired with its transcription."""

from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class VoiceRef:
    """
    A reference voice sample used for zero-shot voice cloning.

    F5-TTS clones the speaker's voice by comparing the audio against its
    transcription. The text must match the audio exactly — any mismatch
    degrades voice quality.

    Attributes:
        name: Human-readable identifier for this sample.
        audio: Absolute path to the .wav reference audio file (4–15 seconds).
        text: Exact Thai transcription of the audio content.
    """

    name: str
    audio: Path
    text: str
