"""Application configuration and voice registry — all tunable values live here."""

from dataclasses import dataclass
from pathlib import Path

from core.voice_ref import VoiceRef

BASE_DIR = Path(__file__).resolve().parent.parent

_SOUNDS_DIR = BASE_DIR / "data" / "sounds"


def load_voices() -> dict[str, VoiceRef]:
    """
    Scan data/sounds/ for .wav/.txt pairs and return a voice registry.

    Each .wav file must have a matching .txt file with the exact transcription.
    Voices without transcription are skipped.
    Both files are gitignored — add your own samples locally, never commit them.

    Returns:
        Dict mapping short key (stem suffix, e.g. "4s") to VoiceRef.
    """
    voices: dict[str, VoiceRef] = {}
    for wav in sorted(_SOUNDS_DIR.glob("*.wav")):
        txt_file = wav.with_suffix(".txt")
        text = txt_file.read_text(encoding="utf-8").strip() if txt_file.exists() else ""
        if not text:
            continue
        key = wav.stem.rsplit("_", 1)[-1]
        voices[key] = VoiceRef(name=wav.stem, audio=wav, text=text)
    return voices


@dataclass
class TTSConfig:
    """
    Parameters that control TTS model loading and inference.

    Attributes:
        model: F5-TTS model version. "v1" uses raw Thai text; "v2" uses IPA phonemes.
        step: Number of ODE solver steps (NFE). Higher = better quality, slower.
        cfg: Classifier-free guidance strength. Higher = closer to reference voice.
        speed: Speech rate multiplier. 1.0 = normal speed.
        max_chars: Maximum Thai characters per inference chunk. Long text is split into batches.
        sample_rate: Output audio sample rate in Hz. F5-TTS outputs 24000 Hz.
        default_voice: Key from load_voices() to use when no voice is specified.
                       Falls back to first available voice if key is not found.
        sentence_silence: Seconds of silence inserted between sentences.
    """

    model: str = "v2"
    step: int = 28
    cfg: float = 2.0
    speed: float = 1.2
    max_chars: int = 150
    sample_rate: int = 24000
    default_voice: str = ""
    sentence_silence: float = 0.3


@dataclass
class AppConfig:
    """Top-level config loaded once at startup."""

    tts: TTSConfig


def load_config() -> AppConfig:
    """Build and return the default AppConfig."""
    return AppConfig(tts=TTSConfig())
