"""FastAPI server entry point — loads the TTS model once at startup and serves HTTP requests."""

from contextlib import asynccontextmanager
from typing import AsyncGenerator

import uvicorn
from fastapi import FastAPI

from config.settings import load_config, load_voices
from infrastructure.f5_tts_engine import F5TTSEngine
from application.tts_service import TTSService
from presentation.api import router


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None, None]:
    """
    Manage the application lifecycle.

    On startup: initialize F5TTSEngine with the default voice (loads model into VRAM).
    On shutdown: Python garbage-collects the engine and releases VRAM automatically.
    """
    config = load_config()
    voices = load_voices()
    default_voice = voices.get(config.tts.default_voice) or next(iter(voices.values()), None)
    if default_voice is None:
        raise RuntimeError("No voice samples found in data/sounds/. Add .wav + .txt pairs.")

    engine = F5TTSEngine(config.tts, default_voice)
    app.state.service = TTSService(engine, sample_rate=config.tts.sample_rate)
    app.state.voices = voices

    yield


app = FastAPI(title="FS-Iris TTS Service", lifespan=lifespan)
app.include_router(router)


if __name__ == "__main__":
    uvicorn.run("server:app", host="0.0.0.0", port=8001, reload=False)
