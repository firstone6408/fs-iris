"""F5-TTS-TH implementation of the TTS Protocol."""

import re

import numpy as np
from pythainlp.tokenize import sent_tokenize
from f5_tts_th.tts import TTS as _F5TTS
from f5_tts_th.utils_infer import preprocess_ref_audio_text, infer_process

from config.settings import TTSConfig
from core.voice_ref import VoiceRef


class F5TTSEngine:
    """
    TTS engine backed by F5-TTS-TH (zero-shot voice cloning).

    Loads the model once on init and reuses it across all `synthesize` calls.
    A default voice is set at construction time; individual calls can override it.

    Long text is split into sentences before inference. Each sentence is inferred
    sequentially, then concatenated with configurable silence between them.
    Reference audio preprocessing is cached per voice to avoid repeating it each call.

    Args:
        config: TTS inference parameters (model version, step, cfg, speed, etc.).
        default_voice: Fallback reference audio + transcription when no voice is passed to synthesize().
    """

    def __init__(self, config: TTSConfig, default_voice: VoiceRef) -> None:
        self._validate_voice(default_voice)
        self._config = config
        self._default_voice = default_voice
        self._tts = _F5TTS(model=config.model)
        # Cache preprocessed ref audio per voice name to avoid repeated disk I/O and trimming
        self._ref_cache: dict[str, tuple[str, str]] = {}

    def synthesize(self, text: str, voice: VoiceRef | None = None) -> np.ndarray:
        """
        Generate speech from Thai text.

        Splits text into sentences, infers each in parallel, then concatenates
        with silence gaps for natural pacing.

        Args:
            text: Thai text to convert to speech.
            voice: Reference voice to use. Falls back to default_voice if None.

        Returns:
            1-D float32 numpy array of the generated waveform at 24000 Hz.
        """
        v = voice or self._default_voice
        sentences = self._split_sentences(text)

        if len(sentences) <= 1:
            return self._infer(text.strip(), v)

        silence = np.zeros(
            int(self._config.sentence_silence * self._config.sample_rate),
            dtype=np.float32,
        )

        parts: list[np.ndarray] = []
        for i, sentence in enumerate(sentences):
            parts.append(self._infer(sentence, v))
            if i < len(sentences) - 1:
                parts.append(silence)

        return np.concatenate(parts)

    def _get_ref(self, voice: VoiceRef) -> tuple[str, str]:
        """Return preprocessed (ref_audio_path, ref_text), cached per voice."""
        if voice.name not in self._ref_cache:
            ref_audio, ref_text = preprocess_ref_audio_text(str(voice.audio), voice.text)
            self._ref_cache[voice.name] = (ref_audio, ref_text)
        return self._ref_cache[voice.name]

    def _infer(self, text: str, voice: VoiceRef) -> np.ndarray:
        ref_audio, ref_text = self._get_ref(voice)
        use_ipa = self._tts.model_type != "v1"
        wav, _, _ = infer_process(
            ref_audio,
            ref_text,
            text,
            self._tts.f5_model,
            self._tts.vocoder,
            mel_spec_type=self._tts.vocoder_name,
            nfe_step=self._config.step,
            cfg_strength=self._config.cfg,
            speed=self._config.speed,
            set_max_chars=self._config.max_chars,
            use_ipa=use_ipa,
        )
        return wav

    @staticmethod
    def _split_sentences(text: str) -> list[str]:
        # Step 1: split by newlines (LLM paragraph boundaries)
        paragraphs = [p.strip() for p in re.split(r"\n+", text) if p.strip()]
        sentences: list[str] = []
        for para in paragraphs:
            # Step 2: split after Thai sentence-ending particles followed by whitespace
            marked = re.sub(r"(ค่ะ|ครับ|นะคะ|นะครับ|คะ)\s+", r"\1||", para)
            chunks = [c.strip() for c in marked.split("||") if c.strip()]
            sentences.extend(chunks)
        return sentences

    @staticmethod
    def _validate_voice(voice: VoiceRef) -> None:
        if not voice.audio.exists():
            raise FileNotFoundError(f"Reference audio not found: {voice.audio}")
        if not voice.text.strip():
            raise ValueError(f"VoiceRef '{voice.name}' has no transcription text. Fill in voice_ref.text before using it.")
