"""llama-cpp-python implementation of the LLM Protocol."""

import re
from typing import cast

from llama_cpp import Llama
from llama_cpp.llama_types import CreateChatCompletionResponse

from config.settings import ModelConfig, SamplingConfig
from core.chat_result import ChatResult
from core.message import Message

_THINK_PATTERN = re.compile(r"<think>.*?</think>\s*", re.DOTALL)


def _strip_thinking(text: str) -> str:
    """Remove <think>...</think> blocks emitted by reasoning models (e.g. Qwen3)."""
    return _THINK_PATTERN.sub("", text).strip()


class LlamaCppLLM:
    """
    Concrete LLM backed by llama-cpp-python.

    Loads a GGUF model on init and exposes `chat` to match the LLM Protocol.
    All llama.cpp-specific details are contained here — no other module
    needs to import from llama_cpp directly.
    """

    def __init__(self, config: ModelConfig, sampling: SamplingConfig) -> None:
        """
        Load the GGUF model and configure the inference runtime.

        Args:
            config: Model path and llama.cpp tuning parameters (GPU layers, context size, etc.).
            sampling: Token sampling parameters (temperature, top_p, top_k, etc.).
        """
        self._sampling = sampling
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

    def chat(self, messages: list[Message], max_tokens: int = 512) -> ChatResult:
        """
        Run a chat completion and return a structured result.

        Args:
            messages: Full conversation context (system prompt + history + latest user turn).
            max_tokens: Maximum tokens the model may generate. Defaults to 512.

        Returns:
            ChatResult: Reply text (thinking blocks stripped), finish reason, and token counts.
        """
        raw = self._llm.create_chat_completion(
            messages=messages,  # type: ignore[arg-type]
            max_tokens=max_tokens,
            stream=False,
            temperature=self._sampling.temperature,
            top_p=self._sampling.top_p,
            top_k=self._sampling.top_k,
            min_p=self._sampling.min_p,
            presence_penalty=self._sampling.presence_penalty,
            seed=self._sampling.seed,
            stop=self._sampling.stop or None,
        )
        response = cast(CreateChatCompletionResponse, raw)
        usage = response.get("usage") or {}

        return ChatResult(
            reply=_strip_thinking(response["choices"][0]["message"]["content"] or ""),
            finish_reason=response["choices"][0].get("finish_reason") or "unknown",
            prompt_tokens=usage.get("prompt_tokens", 0),
            completion_tokens=usage.get("completion_tokens", 0),
        )
