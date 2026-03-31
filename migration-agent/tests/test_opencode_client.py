"""Tests for the OpenCodeClient module — uses httpx mocking."""

import json
import pytest
import httpx

from agent.config import AgentConfig
from agent.opencode_client import OpenCodeClient, OpenCodeError


@pytest.fixture
def cfg():
    config = AgentConfig()
    config.api_key = "test-key-123"
    config.base_url = "https://api.openai.com/v1"
    config.model = "gpt-4o"
    config.max_retries = 2
    config.retry_backoff = 0.01  # fast retries in tests
    return config


def _make_response(content: str, status_code: int = 200) -> httpx.Response:
    body = {
        "choices": [{"message": {"role": "assistant", "content": content}}]
    }
    return httpx.Response(
        status_code=status_code,
        content=json.dumps(body).encode(),
        headers={"Content-Type": "application/json"},
    )


def _make_error_response(status_code: int, message: str) -> httpx.Response:
    body = {"error": {"message": message}}
    return httpx.Response(
        status_code=status_code,
        content=json.dumps(body).encode(),
        headers={"Content-Type": "application/json"},
    )


def test_complete_returns_content(cfg):
    transport = httpx.MockTransport(
        lambda request: _make_response("const App = () => <div />;")
    )
    client = OpenCodeClient(cfg)
    client._client = httpx.Client(
        base_url=cfg.base_url,
        transport=transport,
        headers={"Authorization": f"Bearer {cfg.api_key}"},
    )
    result = client.complete("System prompt", "User prompt")
    assert result == "const App = () => <div />;"


def test_complete_raises_on_400(cfg):
    transport = httpx.MockTransport(
        lambda request: _make_error_response(400, "Bad Request")
    )
    client = OpenCodeClient(cfg)
    client._client = httpx.Client(
        base_url=cfg.base_url,
        transport=transport,
        headers={"Authorization": f"Bearer {cfg.api_key}"},
    )
    with pytest.raises(OpenCodeError) as exc_info:
        client.complete("System", "User")
    assert exc_info.value.status_code == 400


def test_complete_raises_on_401(cfg):
    transport = httpx.MockTransport(
        lambda request: _make_error_response(401, "Unauthorized")
    )
    client = OpenCodeClient(cfg)
    client._client = httpx.Client(
        base_url=cfg.base_url,
        transport=transport,
        headers={"Authorization": f"Bearer {cfg.api_key}"},
    )
    with pytest.raises(OpenCodeError) as exc_info:
        client.complete("System", "User")
    assert exc_info.value.status_code == 401


def test_complete_includes_model_in_payload(cfg):
    captured = {}

    def handler(request: httpx.Request) -> httpx.Response:
        captured["body"] = json.loads(request.content)
        return _make_response("ok")

    transport = httpx.MockTransport(handler)
    client = OpenCodeClient(cfg)
    client._client = httpx.Client(
        base_url=cfg.base_url,
        transport=transport,
        headers={"Authorization": f"Bearer {cfg.api_key}"},
    )
    client.complete("sys", "usr")
    assert captured["body"]["model"] == cfg.model


def test_complete_includes_messages_in_payload(cfg):
    captured = {}

    def handler(request: httpx.Request) -> httpx.Response:
        captured["body"] = json.loads(request.content)
        return _make_response("ok")

    transport = httpx.MockTransport(handler)
    client = OpenCodeClient(cfg)
    client._client = httpx.Client(
        base_url=cfg.base_url,
        transport=transport,
        headers={"Authorization": f"Bearer {cfg.api_key}"},
    )
    client.complete("My system prompt", "My user prompt")
    messages = captured["body"]["messages"]
    assert messages[0] == {"role": "system", "content": "My system prompt"}
    assert messages[1] == {"role": "user", "content": "My user prompt"}


def test_context_manager_closes_client(cfg):
    transport = httpx.MockTransport(
        lambda request: _make_response("ok")
    )
    with OpenCodeClient(cfg) as client:
        client._client = httpx.Client(
            base_url=cfg.base_url,
            transport=transport,
            headers={"Authorization": f"Bearer {cfg.api_key}"},
        )
        assert client is not None
