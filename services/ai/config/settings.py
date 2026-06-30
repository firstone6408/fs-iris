"""Application configuration — all tunable values live here, not scattered in code."""

import os
from dataclasses import dataclass, field
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent


@dataclass
class ModelConfig:
    """
    Configuration for loading and running the GGUF model via llama-cpp-python.

    Attributes:
        path: Absolute path to the .gguf model file.
        n_ctx: Token context window size. Higher = more memory, longer conversations.
        n_gpu_layers: Number of layers to offload to GPU. -1 = offload all layers.
        flash_attn: Enable Flash Attention for faster inference (requires compatible GPU).
        n_threads: CPU threads used for inference. Defaults to all available cores.
        n_batch: Batch size for prompt processing (tokens per batch).
        n_ubatch: Micro-batch size inside each batch for memory management.
        use_mmap: Memory-map the model file instead of loading it fully into RAM.
        use_mlock: Lock model memory pages to prevent OS from swapping them to disk.
        verbose: Print llama.cpp internal logs to stdout when True.
    """

    path: Path
    n_ctx: int = 4096
    n_gpu_layers: int = -1
    flash_attn: bool = True
    n_threads: int = field(default_factory=lambda: os.cpu_count() or 4)
    n_batch: int = 1024
    n_ubatch: int = 512
    use_mmap: bool = True
    use_mlock: bool = True
    verbose: bool = False


@dataclass
class ChatConfig:
    """
    Configuration for chat behavior.

    Attributes:
        max_tokens: Maximum number of tokens the model can generate per reply.
        persona_path: Path to the persona text file used as the system prompt.
    """

    max_tokens: int = 512
    persona_path: Path = field(
        default_factory=lambda: BASE_DIR / "data" / "personas" / "makise_kurisu.md"
    )


@dataclass
class AppConfig:
    """
    Top-level application configuration that groups all sub-configs.

    Attributes:
        model: Settings for the LLM model (path, GPU layers, context size, etc.).
        chat: Settings for chat behavior (max tokens, persona file path).
    """

    model: ModelConfig
    chat: ChatConfig


def load_config() -> AppConfig:
    """
    Build and return the default AppConfig for the application.

    Resolves all paths relative to the services/ai directory so the app
    can be launched from any working directory.

    Returns:
        AppConfig: Ready-to-use configuration with model and chat settings.
    """
    return AppConfig(
        model=ModelConfig(
            path=BASE_DIR / "models" / "qwen3-8b-q4_k_m.gguf",
        ),
        chat=ChatConfig(),
    )
