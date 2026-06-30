"""FastAPI server entry point — loads the model once at startup and serves HTTP requests."""

from contextlib import asynccontextmanager
from typing import AsyncGenerator

import uvicorn
from fastapi import FastAPI

from config.settings import load_config
from infrastructure.llama_cpp_llm import LlamaCppLLM
from presentation.api import router


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None, None]:
    """
    Manage the application lifecycle.

    On startup: load the persona from disk and initialize the LLM (loads model into VRAM).
    On shutdown: yielding ends and Python garbage-collects the LLM automatically.

    Both are stored in app.state so all request handlers can access them.
    """
    config = load_config()

    persona = config.chat.persona_path.read_text(encoding="utf-8").strip()
    app.state.system_prompt = "/no_think\n\n" + persona if config.chat.no_think else persona
    app.state.default_max_tokens = config.chat.max_tokens
    app.state.llm = LlamaCppLLM(config.model, config.sampling)

    yield


app = FastAPI(title="FS-Iris AI Service", lifespan=lifespan)
app.include_router(router)


if __name__ == "__main__":
    uvicorn.run("server:app", host="0.0.0.0", port=8000, reload=False)
