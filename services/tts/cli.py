"""CLI entry point — wires config, voice, engine, service, and CLI together."""

import argparse
import time
from datetime import datetime
from pathlib import Path

from config.settings import load_config, load_voices
from infrastructure.f5_tts_engine import F5TTSEngine
from application.tts_service import TTSService
from presentation.cli import CLI

BASE_DIR = Path(__file__).resolve().parent
OUTPUTS_DIR = BASE_DIR / "outputs"


def main() -> None:
    """
    Bootstrap and run a one-shot TTS generation.

    Usage:
        python cli.py <text to speak>
        python cli.py --voice 9s <text to speak>

    Steps:
        1. Load AppConfig (model version, inference params, default voice key).
        2. Resolve the VoiceRef from load_voices() (scans data/sounds/).
        3. Load F5TTSEngine (loads model into VRAM).
        4. Wrap in TTSService for output handling.
        5. Run CLI with the given text, write timestamped output to outputs/.
    """
    parser = argparse.ArgumentParser(description="FS-Iris TTS")
    parser.add_argument("text", nargs="+", help="Text to synthesize")
    parser.add_argument("--voice", default="", metavar="KEY",
                        help="Voice key (e.g. '4s', '9s'). Uses first available if omitted.")
    args = parser.parse_args()

    text = " ".join(args.text)

    OUTPUTS_DIR.mkdir(exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_path = OUTPUTS_DIR / f"{timestamp}.wav"

    start = time.perf_counter()

    config = load_config()
    voices = load_voices()

    if args.voice:
        if args.voice not in voices:
            raise SystemExit(f"Unknown voice '{args.voice}'. Available: {list(voices)}")
        voice = voices[args.voice]
    else:
        voice = voices.get(config.tts.default_voice) or next(iter(voices.values()), None)
        if voice is None:
            raise RuntimeError("No voice samples found in data/sounds/. Add .wav + .txt pairs.")

    engine = F5TTSEngine(config.tts, voice)
    service = TTSService(engine, sample_rate=config.tts.sample_rate)
    CLI(service, output_path).run(text)

    total = time.perf_counter() - start
    print(f"Total (incl. model load): {total:.3f}s")


if __name__ == "__main__":
    main()
