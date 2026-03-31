"""
OpenCode API client for the Angular-to-React Migration Agent.

Wraps the OpenAI-compatible REST API used by OpenCode, with retry logic,
error handling, and structured prompt building.
"""

import json
import logging
import time
from typing import Optional

import httpx

from .config import AgentConfig

logger = logging.getLogger(__name__)


class OpenCodeError(Exception):
    """Raised when the OpenCode API returns an error."""

    def __init__(self, status_code: int, message: str):
        self.status_code = status_code
        super().__init__(f"OpenCode API error {status_code}: {message}")


class OpenCodeClient:
    """
    Thin wrapper around the OpenCode / OpenAI-compatible Chat Completions endpoint.

    Supports:
    - Synchronous chat completion requests
    - Automatic retries with exponential backoff
    - Streaming (optional)
    """

    CHAT_COMPLETIONS_PATH = "/chat/completions"

    def __init__(self, config: AgentConfig):
        self.config = config
        self._client = httpx.Client(
            base_url=config.base_url.rstrip("/"),
            headers={
                "Authorization": f"Bearer {config.api_key}",
                "Content-Type": "application/json",
            },
            timeout=config.request_timeout,
        )

    def complete(
        self,
        system_prompt: str,
        user_prompt: str,
        *,
        max_tokens: Optional[int] = None,
        temperature: Optional[float] = None,
    ) -> str:
        """
        Send a chat completion request and return the assistant's text response.

        Args:
            system_prompt: The system message that sets the AI's role and context.
            user_prompt:   The user message containing the specific task.
            max_tokens:    Override config max_tokens if provided.
            temperature:   Override config temperature if provided.

        Returns:
            The text content of the first assistant choice.

        Raises:
            OpenCodeError: On non-2xx API responses after all retries.
        """
        payload = {
            "model": self.config.model,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            "max_tokens": max_tokens or self.config.max_tokens,
            "temperature": temperature if temperature is not None else self.config.temperature,
        }

        last_error: Optional[Exception] = None
        for attempt in range(1, self.config.max_retries + 1):
            try:
                logger.debug(
                    "OpenCode request (attempt %d/%d) model=%s",
                    attempt,
                    self.config.max_retries,
                    self.config.model,
                )
                response = self._client.post(
                    self.CHAT_COMPLETIONS_PATH,
                    content=json.dumps(payload),
                )

                if response.status_code == 429:
                    # Rate limited – wait and retry
                    wait = self.config.retry_backoff ** attempt
                    logger.warning("Rate limited. Waiting %.1fs before retry.", wait)
                    time.sleep(wait)
                    last_error = OpenCodeError(429, "Rate limit exceeded")
                    continue

                if response.status_code >= 400:
                    body = response.text
                    raise OpenCodeError(response.status_code, body)

                data = response.json()
                return data["choices"][0]["message"]["content"]

            except (httpx.TimeoutException, httpx.NetworkError) as exc:
                wait = self.config.retry_backoff ** attempt
                logger.warning(
                    "Network error on attempt %d: %s. Retrying in %.1fs.",
                    attempt,
                    exc,
                    wait,
                )
                time.sleep(wait)
                last_error = exc

        raise last_error or RuntimeError("All retries exhausted with no error captured.")

    def close(self) -> None:
        """Close the underlying HTTP client."""
        self._client.close()

    def __enter__(self):
        return self

    def __exit__(self, *args):
        self.close()
