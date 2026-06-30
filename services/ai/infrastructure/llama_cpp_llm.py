"""llama-cpp-python implementation of the LLM Protocol."""

from typing import cast

from llama_cpp import Llama
from llama_cpp.llama_types import CreateChatCompletionResponse

from config.settings import ModelConfig
from core.message import Message


class LlamaCppLLM:
    """
    Concrete LLM backed by llama-cpp-python.

    Loads a GGUF model on init and exposes `chat` to match the LLM Protocol.
    All llama.cpp-specific details are contained here — no other module
    needs to import from llama_cpp directly.
    """

    def __init__(self, config: ModelConfig) -> None:
        """
        Load the GGUF model and configure the inference runtime.

        Args:
            config: Model path and llama.cpp tuning parameters (GPU layers, context size, etc.).
        """
        self._llm = Llama(
            model_path=str(config.path),
            n_ctx=config.n_ctx,
            n_gpu_layers=config.n_gpu_layers,
            flash_attn=config.flash_attn,
            n_threads=config.n_threads,
            n_batch=config.n_batch,
            n_ubatch=config.n_ubatch,
            use_mmap=config.use_mmap,
            use_mlock=config.use_mlock,
            verbose=config.verbose,
        )

    def chat(self, messages: list[Message], max_tokens: int = 512) -> str:
        """
        Run a chat completion and return the model's reply as a string.

        Args:
            messages: Full conversation context (system prompt + history + latest user turn).
            max_tokens: Maximum tokens the model may generate. Defaults to 512.

        Returns:
            str: Generated reply text, or empty string if the model produces no content.
        """
        raw = self._llm.create_chat_completion(
            messages=messages,  # type: ignore[arg-type]
            max_tokens=max_tokens,
            stream=False,
        )
        response = cast(CreateChatCompletionResponse, raw)
        return response["choices"][0]["message"]["content"] or ""
