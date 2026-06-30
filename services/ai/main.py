"""Entry point — wires config, LLM, service, and CLI together then starts the app."""

from config.settings import load_config
from infrastructure.llama_cpp_llm import LlamaCppLLM
from application.chat_service import ChatService
from presentation.cli import CLI


def main() -> None:
    """
    Bootstrap and launch the Iris assistant.

    Steps:
    1. Load AppConfig (model path, GPU settings, persona path, etc.).
    2. Read the persona file and use its content as the system prompt.
    3. Instantiate LlamaCppLLM with the model config (loads the model into VRAM).
    4. Wrap the LLM in ChatService to manage conversation history.
    5. Hand ChatService to CLI and start the interactive loop.
    """
    config = load_config()
    persona = config.chat.persona_path.read_text(encoding="utf-8").strip()

    llm = LlamaCppLLM(config.model)
    chat_service = ChatService(llm, system_prompt=persona)
    CLI(chat_service).run()


if __name__ == "__main__":
    main()
