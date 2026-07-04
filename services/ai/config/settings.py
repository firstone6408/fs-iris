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
    n_batch: int = 4096
    n_ubatch: int = 2048
    use_mmap: bool = True
    use_mlock: bool = True
    verbose: bool = False


@dataclass
class SamplingConfig:
    """
    Sampling parameters that control how the model generates tokens.

    Attributes:
        temperature: Randomness of output. Higher = more creative, lower = more focused.
        top_p: Nucleus sampling threshold. Keeps tokens whose cumulative probability >= top_p.
        top_k: Limits token selection to the top-k most likely tokens at each step.
        min_p: Minimum probability threshold relative to the top token (0.0 = disabled).
        presence_penalty: Penalizes tokens that have already appeared, reducing repetition.
        seed: Random seed for reproducibility. -1 = random seed each run.
        stop: List of strings that cause the model to stop generating when encountered.
    """

    temperature: float = 0.7
    top_p: float = 0.8
    top_k: int = 20
    min_p: float = 0.0
    presence_penalty: float = 1.5
    seed: int = -1
    stop: list[str] = field(default_factory=lambda: [])


@dataclass
class ChatConfig:
    """
    Configuration for chat behavior.

    Attributes:
        max_tokens: Maximum number of tokens the model can generate per reply.
        persona_path: Path to the persona text file used as the system prompt.
        no_think: Prepend /no_think to the system prompt to disable Qwen3 thinking mode.
    """

    max_tokens: int = 2048
    persona_path: Path = field(
        default_factory=lambda: BASE_DIR / "data" / "personas" / "makise_kurisu_voice.md"
    )
    no_think: bool = True


@dataclass
class AppConfig:
    """
    Top-level config loaded once at startup. Always access settings through sub-configs:

        config = load_config()
        config.model.n_ctx           # LLM loading and runtime parameters
        config.sampling.temperature  # token sampling parameters
        config.chat.no_think         # chat behavior settings

    Attributes:
        model: LLM loading and runtime parameters.
        sampling: Token sampling parameters (temperature, top_p, etc.).
        chat: Chat behavior settings (persona path, max_tokens, no_think).
    """

    model: ModelConfig
    sampling: SamplingConfig
    chat: ChatConfig


def load_config() -> AppConfig:
    """
    Build and return the default AppConfig for the application.

    Resolves all paths relative to the services/ai directory so the app
    can be launched from any working directory.

    Returns:
        AppConfig: Ready-to-use configuration with model, sampling, and chat settings.
    """
    return AppConfig(
        model=ModelConfig(
            path=BASE_DIR / "models" / "gemma-4-E2B-it-Q6_K.gguf",
        ),
        sampling=SamplingConfig(),
        chat=ChatConfig(),
    )
