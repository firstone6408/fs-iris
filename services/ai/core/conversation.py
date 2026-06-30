"""In-memory store for the current conversation turn history."""

from core.message import Message


class ConversationHistory:
    """
    Maintains an ordered list of user and assistant messages for a single session.

    The system prompt is NOT stored here — it is prepended by ChatService
    on every call so it always appears first in the message list.
    """

    def __init__(self) -> None:
        """Initialize with an empty message list."""
        self._messages: list[Message] = []

    def add(self, role: str, content: str) -> None:
        """
        Append a new message to the history.

        Args:
            role: The sender of the message ("user" or "assistant").
            content: The text content of the message.
        """
        self._messages.append(Message(role=role, content=content))  # type: ignore[arg-type]

    def messages(self) -> list[Message]:
        """
        Return a copy of all messages in chronological order.

        Returns:
            list[Message]: Snapshot of the current conversation history.
        """
        return list(self._messages)

    def clear(self) -> None:
        """Remove all messages, resetting the conversation to a blank state."""
        self._messages.clear()
