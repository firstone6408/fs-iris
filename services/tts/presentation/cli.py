"""CLI presenter — runs a one-shot TTS synthesis and reports timing."""

import time
from pathlib import Path

from application.tts_service import TTSService


class CLI:
    """
    Command-line interface for TTS synthesis.

    Accepts text, generates audio, saves to file, and prints timing.

    Args:
        service: TTSService used to perform synthesis.
        output_path: Path where the output .wav file will be written.
    """

    def __init__(self, service: TTSService, output_path: Path) -> None:
        self._service = service
        self._output_path = output_path

    def run(self, text: str) -> None:
        """
        Synthesize the given text and save to the configured output path.

        Args:
            text: Thai text to convert to speech.
        """
        print(f"Generating: {text!r}")
        start = time.perf_counter()
        self._service.save(text, self._output_path)
        elapsed = time.perf_counter() - start
        print(f"Saved → {self._output_path}  ({elapsed:.3f}s)")
