"""Business logic for managing a conversation with the LLM."""

from core.conversation import ConversationHistory
from core.llm import LLM
from core.message import Message


class ChatService:
    """
    Orchestrates a multi-turn conversation between the user and an LLM.

    Prepends the system prompt on every call so the model always has its persona,
    and stores each user/assistant turn in ConversationHistory so the model
    can reference earlier messages.
    """

    def __init__(self, llm: LLM, system_prompt: str) -> None:
        """
        Args:
            llm: Any object that satisfies the LLM Protocol (e.g. LlamaCppLLM).
            system_prompt: Persona/instruction text sent as the system message on every request.
        """
        self._llm = llm
        self._system_prompt = system_prompt
        self._history = ConversationHistory()

    def chat(self, user_input: str, max_tokens: int = 512) -> str:
        """
        Process one user turn and return the model's reply.

        Adds the user message to history, calls the LLM with the full context
        (system prompt + history), then stores the reply before returning it.

        Args:
            user_input: Text typed by the user.
            max_tokens: Maximum tokens the model may generate. Defaults to 512.

        Returns:
            str: The model's reply.
        """
        self._history.add("user", user_input)

        messages: list[Message] = [
            Message(role="system", content=self._system_prompt),
            *self._history.messages(),
        ]

        result = self._llm.chat(messages, max_tokens=max_tokens)
        self._history.add("assistant", result.reply)
        return result.reply

    def clear_history(self) -> None:
        """Reset conversation history while keeping the system prompt intact."""
        self._history.clear()
