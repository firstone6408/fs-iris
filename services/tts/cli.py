"""CLI entry point — wires config, voice, engine, service, and CLI together."""

import sys
from pathlib import Path

from config.settings import load_config, load_voices
from infrastructure.f5_tts_engine import F5TTSEngine
from application.tts_service import TTSService
from presentation.cli import CLI

BASE_DIR = Path(__file__).resolve().parent


def main() -> None:
    """
    Bootstrap and run a one-shot TTS generation.

    Usage:
        python cli.py <text to speak>
    """
    if len(sys.argv) < 2:
        raise SystemExit("Usage: python cli.py <text>")

    text = " ".join(sys.argv[1:])

    config = load_config()
    voices = load_voices()
    voice = voices.get(config.tts.default_voice) or next(iter(voices.values()), None)
    if voice is None:
        raise RuntimeError("No voice samples found in data/sounds/. Add .wav + .txt pairs.")
    engine = F5TTSEngine(config.tts, voice)
    service = TTSService(engine, sample_rate=config.tts.sample_rate)
    CLI(service, BASE_DIR / "output.wav").run(text)


if __name__ == "__main__":
    main()
