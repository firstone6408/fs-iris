# services/ai

AI inference service for FS-Iris. Handles LLM loading, persona management, conversation history, and exposes inference via CLI and HTTP API.

---

## Architecture

Follows Clean Architecture — inner layers have no knowledge of outer layers.

```
┌─────────────────────────────────────┐
│           Presentation              │  CLI · FastAPI
├─────────────────────────────────────┤
│           Application               │  ChatService
├─────────────────────────────────────┤
│              Core                   │  Message · ConversationHistory · LLM Protocol · ChatResult
├─────────────────────────────────────┤
│          Infrastructure             │  LlamaCppLLM
└─────────────────────────────────────┘
          Config (cross-cutting)
```

**Dependency direction:** Presentation → Application → Core ← Infrastructure

`Core` defines the `LLM` Protocol. `Infrastructure` implements it. `Application` depends only on the Protocol — swapping the backend requires no changes to business logic.

---

## Directory Structure

```
services/ai/
├── config/
│   └── settings.py          # AppConfig, ModelConfig, SamplingConfig, ChatConfig
├── core/
│   ├── message.py           # Message TypedDict (role + content)
│   ├── conversation.py      # ConversationHistory — ordered message store
│   ├── llm.py               # LLM Protocol (interface)
│   └── chat_result.py       # ChatResult — structured inference output
├── infrastructure/
│   └── llama_cpp_llm.py     # LlamaCppLLM — llama-cpp-python implementation
├── application/
│   └── chat_service.py      # ChatService — orchestrates conversation
├── presentation/
│   ├── cli.py               # Interactive CLI (REPL)
│   └── api.py               # FastAPI router
├── data/
│   └── personas/            # Persona files (.md)
├── cli.py                   # CLI entry point
├── server.py                # API server entry point
└── requirements.txt
```

---

## Layers

### Config

Single load at startup. Access via namespaced sub-configs:

```python
config = load_config()
config.model.n_ctx            # ModelConfig  — LLM loading & runtime
config.sampling.temperature   # SamplingConfig — token generation
config.chat.no_think          # ChatConfig   — chat behavior
```

| Config | Responsibility |
|---|---|
| `ModelConfig` | Model path, context size, GPU layers, batching, memory |
| `SamplingConfig` | temperature, top_p, top_k, min_p, presence_penalty, seed, stop |
| `ChatConfig` | max_tokens, persona path, no_think |

---

### Core

Pure Python — no external dependencies. Defines the contracts every other layer depends on.

| File | Description |
|---|---|
| `message.py` | `Message` TypedDict with `role` and `content` |
| `conversation.py` | `ConversationHistory` — append/read/clear message list |
| `llm.py` | `LLM` Protocol — `chat(messages, max_tokens) → ChatResult` |
| `chat_result.py` | `ChatResult` — `reply`, `finish_reason`, `prompt_tokens`, `completion_tokens` |

---

### Infrastructure

Implements the `LLM` Protocol using llama-cpp-python.

**`LlamaCppLLM`**
- Loads GGUF model into VRAM on `__init__`
- Applies all `SamplingConfig` parameters on each call
- Strips `<think>...</think>` blocks from output (Qwen3 thinking mode)
- Returns `ChatResult` with token usage and finish reason

Swapping to another backend (vLLM, OpenAI, etc.) = write a new class that satisfies `LLM` Protocol. Nothing else changes.

---

### Application

**`ChatService`**
- Holds `ConversationHistory` for the session
- Prepends system prompt (persona) on every call
- Calls `LLM.chat()` and stores both user and assistant turns in history
- Returns reply as plain `str`

---

### Presentation

**CLI** (`cli.py` → `presentation/cli.py`)

Simple REPL. Reads stdin, prints reply to stdout. Exits on `exit`.

```bash
python cli.py
```

**HTTP API** (`server.py` → `presentation/api.py`)

Stateless — client manages conversation history and sends the full message list each request.

```bash
python server.py
# or
uvicorn server:app --host 0.0.0.0 --port 8000
```

| Method | Path | Description |
|---|---|---|
| `GET` | `/health` | Liveness check |
| `POST` | `/chat` | Send messages, receive reply |

Swagger UI: `http://localhost:8000/docs`

**POST /chat**

```json
// Request
{
  "messages": [
    { "role": "user", "content": "สวัสดี" },
    { "role": "assistant", "content": "สวัสดีค่ะ..." },
    { "role": "user", "content": "คุณชื่ออะไร" }
  ],
  "max_tokens": 512
}

// Response
{
  "reply": "ฉันชื่อ คุริสึ มาคิเสะ ค่ะ",
  "finish_reason": "stop",
  "prompt_tokens": 142,
  "completion_tokens": 18
}
```

`max_tokens` is optional — defaults to `config.chat.max_tokens` if omitted.

---

## Persona

Persona files are plain text/markdown loaded at startup as the system prompt.

```
data/personas/
└── makise_kurisu.md
```

To switch persona, update `config.chat.persona_path` in `config/settings.py`.

---

## Setup

```bash
# Install CUDA-accelerated llama-cpp-python
bash scripts/install_lib.sh

# Install remaining dependencies
pip install -r requirements.txt
```

---

## Runtime

Tested on:

| Model | Format | Quantization | Runtime |
|---|---|---|---|
| Qwen3 8B | GGUF | Q4_K_M | llama-cpp-python + CUDA |
