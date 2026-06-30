# Changelog

All notable changes to FS-Iris will be documented here.

---

## [0.1.0] — 2026-06-30

### Models Tested

| Model | Format | Quantization | Runtime |
|---|---|---|---|
| Qwen3 8B | GGUF | Q4_K_M | llama-cpp-python + CUDA |

---

### Added

#### Architecture

- Restructured `services/ai/` from a single monolithic `main.py` into a layered clean architecture
  - `config/` — `AppConfig`, `ModelConfig`, `SamplingConfig`, `ChatConfig`
  - `core/` — `Message`, `ConversationHistory`, `LLM` Protocol, `ChatResult`
  - `infrastructure/` — `LlamaCppLLM` (llama-cpp-python)
  - `application/` — `ChatService`
  - `presentation/` — CLI, FastAPI router
- `ConversationHistory` — multi-turn conversation context (each turn now remembers prior messages)
- Makise Kurisu persona (`services/ai/data/personas/makise_kurisu.md`)

#### HTTP API

- `POST /chat` — send message history, receive model reply
- `GET /health` — liveness check
- LLM loaded once at startup via FastAPI `lifespan`
- Auto-generated Swagger UI at `/docs`

#### Inference

- `ChatResult` — structured return type with `reply`, `finish_reason`, `prompt_tokens`, `completion_tokens`
- `SamplingConfig` — `temperature`, `top_p`, `top_k`, `min_p`, `presence_penalty`, `seed`, `stop`
- `no_think` — suppresses Qwen3 chain-of-thought output via `/no_think` prefix; `<think>` blocks stripped automatically
- `AppConfig` — single load with namespaced sub-configs (`config.model`, `config.sampling`, `config.chat`)

---

### Fixed

- Persona path was pointing to a non-existent file

---

### Known Limitations

- No streaming support
- No memory or RAG
- No authentication on API endpoints
