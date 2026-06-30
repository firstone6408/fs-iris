"""FastAPI router exposing the LLM as an HTTP API."""

from typing import Literal

from fastapi import APIRouter, Request
from pydantic import BaseModel

from core.message import Message

router = APIRouter()


class MessageModel(BaseModel):
    """A single chat message sent by the client."""

    role: Literal["user", "assistant"]
    content: str


class ChatRequest(BaseModel):
    """
    Request body for POST /chat.

    The client is responsible for maintaining conversation history
    and sending the full message list on every request.

    Attributes:
        messages: Ordered list of user/assistant turns (no system message — the
                  server prepends the configured persona automatically).
        max_tokens: Maximum tokens the model may generate. Defaults to 512.
    """

    messages: list[MessageModel]
    max_tokens: int = 512


class ChatResponse(BaseModel):
    """
    Response body for POST /chat.

    Attributes:
        reply: The model's generated reply text.
        finish_reason: Why the model stopped — "stop" (natural end) or "length" (hit max_tokens).
        prompt_tokens: Tokens consumed by the input (system prompt + history).
        completion_tokens: Tokens generated in the reply.
    """

    reply: str
    finish_reason: str
    prompt_tokens: int
    completion_tokens: int


@router.get("/health")
def health() -> dict[str, str]:
    """Check that the server is running and the model is loaded."""
    return {"status": "ok"}


@router.post("/chat", response_model=ChatResponse)
def chat(body: ChatRequest, request: Request) -> ChatResponse:
    """
    Send a conversation to the LLM and return its reply.

    The server automatically prepends the configured system prompt
    (persona) before forwarding the messages to the model.

    Args:
        body: ChatRequest containing the message history and max_tokens.
        request: FastAPI request object used to access app.state (llm, system_prompt).

    Returns:
        ChatResponse with the model's reply and token usage info.
    """
    llm = request.app.state.llm
    system_prompt: str = request.app.state.system_prompt

    messages: list[Message] = [
        Message(role="system", content=system_prompt),
        *[Message(role=m.role, content=m.content) for m in body.messages],
    ]

    result = llm.chat(messages, max_tokens=body.max_tokens)
    return ChatResponse(
        reply=result.reply,
        finish_reason=result.finish_reason,
        prompt_tokens=result.prompt_tokens,
        completion_tokens=result.completion_tokens,
    )
