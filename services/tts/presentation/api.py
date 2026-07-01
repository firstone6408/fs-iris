"""FastAPI router exposing TTS synthesis as an HTTP API."""

from fastapi import APIRouter, HTTPException, Request
from fastapi.responses import Response
from pydantic import BaseModel

router = APIRouter()


class SynthesizeRequest(BaseModel):
    """
    Request body for POST /synthesize.

    Attributes:
        text: Thai text to convert to speech.
        voice: Key into the voice registry (e.g. "4s", "9s"). Uses server default if omitted.
    """

    text: str
    voice: str | None = None


@router.get("/health")
def health() -> dict[str, str]:
    """Check that the server is running and the TTS model is loaded."""
    return {"status": "ok"}


@router.post("/synthesize")
def synthesize(body: SynthesizeRequest, request: Request) -> Response:
    """
    Convert Thai text to speech and return the audio as a WAV file.

    Args:
        body: SynthesizeRequest with text and optional voice key.
        request: FastAPI request used to access app.state (service, voices).

    Returns:
        audio/wav binary response containing the generated speech.

    Raises:
        400: If the requested voice key does not exist in the registry.
        400: If the requested voice has no transcription text configured.
    """
    from application.tts_service import TTSService
    from core.voice_ref import VoiceRef

    if not body.text.strip():
        raise HTTPException(status_code=400, detail="text cannot be empty")

    service: TTSService = request.app.state.service
    voices: dict[str, VoiceRef] = request.app.state.voices

    voice: VoiceRef | None = None
    if body.voice is not None:
        if body.voice not in voices:
            raise HTTPException(status_code=400, detail=f"Unknown voice: '{body.voice}'. Available: {list(voices)}")
        voice = voices[body.voice]
        if not voice.text.strip():
            raise HTTPException(status_code=400, detail=f"Voice '{body.voice}' has no transcription text configured.")

    wav_bytes = service.synthesize_wav_bytes(body.text, voice)
    return Response(content=wav_bytes, media_type="audio/wav")
