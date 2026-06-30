"""LLM abstraction — depend on this Protocol, not on any specific library."""

from typing import Protocol
from core.message import Message


class LLM(Protocol):
    """
    Interface that any LLM backend must satisfy.

    Using a Protocol allows the rest of the application to stay decoupled
    from llama-cpp-python. Swapping to vLLM, OpenAI, or any other backend
    only requires writing a new class that implements this interface —
    no other code needs to change.
    """

    def chat(self, messages: list[Message], max_tokens: int = 512) -> str:
        """
        Send a list of messages to the model and return its text reply.

        Args:
            messages: The full conversation context, including the system prompt
                      followed by alternating user and assistant turns.
            max_tokens: Maximum number of tokens the model may generate in its reply.

        Returns:
            str: The model's generated reply as a plain string.
        """
        ...
