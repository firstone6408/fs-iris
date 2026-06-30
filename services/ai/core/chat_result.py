"""Result returned by any LLM backend after a chat completion."""

from dataclasses import dataclass


@dataclass
class ChatResult:
    """
    Structured output from a single LLM call.

    Attributes:
        reply: The model's generated text with thinking blocks stripped.
        finish_reason: Why the model stopped generating.
                       Common values: "stop" (natural end), "length" (hit max_tokens).
        prompt_tokens: Number of tokens consumed by the input (system prompt + history).
        completion_tokens: Number of tokens generated in the reply.
    """

    reply: str
    finish_reason: str
    prompt_tokens: int
    completion_tokens: int
