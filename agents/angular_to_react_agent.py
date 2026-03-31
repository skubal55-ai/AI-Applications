#!/usr/bin/env python3
"""Markdown-driven Angular to React migration agent for OpenCode."""

from __future__ import annotations

import argparse
import base64
import json
import os
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any
from urllib import error, request


DEFAULT_BASE_URL = os.environ.get("OPENCODE_BASE_URL", "http://127.0.0.1:4096")
DEFAULT_TIMEOUT_SECONDS = 120
DEFAULT_RULES_PATH = Path(__file__).with_name("angular_to_react_rules.md")


@dataclass(frozen=True)
class SourceInput:
    label: str
    content: str


class OpenCodeError(RuntimeError):
    """Raised when the OpenCode server returns an unexpected response."""


class OpenCodeClient:
    def __init__(
        self,
        base_url: str,
        timeout_seconds: int = DEFAULT_TIMEOUT_SECONDS,
        username: str | None = None,
        password: str | None = None,
    ) -> None:
        self.base_url = base_url.rstrip("/")
        self.timeout_seconds = timeout_seconds
        self.username = username or os.environ.get("OPENCODE_SERVER_USERNAME", "opencode")
        self.password = password or os.environ.get("OPENCODE_SERVER_PASSWORD")

    def healthcheck(self) -> dict[str, Any]:
        response = self._request("GET", "/global/health")
        if not isinstance(response, dict) or not response.get("healthy"):
            raise OpenCodeError(f"OpenCode healthcheck failed: {response!r}")
        return response

    def create_session(self, title: str) -> str:
        response = self._request("POST", "/session", {"title": title})
        session_id = response.get("id") if isinstance(response, dict) else None
        if not isinstance(session_id, str) or not session_id:
            raise OpenCodeError(f"OpenCode session response missing id: {response!r}")
        return session_id

    def send_prompt(
        self,
        session_id: str,
        prompt: str,
        *,
        provider_id: str | None = None,
        model_id: str | None = None,
        agent_id: str | None = None,
    ) -> dict[str, Any]:
        payload: dict[str, Any] = {
            "parts": [
                {
                    "type": "text",
                    "text": prompt,
                }
            ]
        }
        if provider_id or model_id:
            if not provider_id or not model_id:
                raise OpenCodeError("Both provider_id and model_id are required when selecting a model.")
            payload["model"] = {"providerID": provider_id, "modelID": model_id}
        if agent_id:
            payload["agent"] = agent_id
        response = self._request("POST", f"/session/{session_id}/message", payload)
        if not isinstance(response, dict):
            raise OpenCodeError(f"Unexpected OpenCode message response: {response!r}")
        return response

    def _request(self, method: str, path: str, payload: dict[str, Any] | None = None) -> Any:
        endpoint = f"{self.base_url}{path}"
        headers = {"Accept": "application/json"}
        body = None
        if payload is not None:
            headers["Content-Type"] = "application/json"
            body = json.dumps(payload).encode("utf-8")

        http_request = request.Request(endpoint, data=body, method=method, headers=headers)
        if self.password:
            raw_credentials = f"{self.username}:{self.password}".encode("utf-8")
            token = base64.b64encode(raw_credentials).decode("ascii")
            http_request.add_header("Authorization", f"Basic {token}")

        try:
            with request.urlopen(http_request, timeout=self.timeout_seconds) as response:
                raw_response = response.read().decode("utf-8")
        except error.HTTPError as exc:
            details = exc.read().decode("utf-8", errors="replace")
            raise OpenCodeError(
                f"OpenCode request failed with status {exc.code} for {path}: {details}"
            ) from exc
        except error.URLError as exc:
            raise OpenCodeError(f"Could not reach OpenCode at {endpoint}: {exc.reason}") from exc

        if not raw_response.strip():
            return None

        try:
            return json.loads(raw_response)
        except json.JSONDecodeError:
            return raw_response


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Convert Angular source into React by sending a markdown-guided prompt to OpenCode."
    )
    parser.add_argument(
        "--rules-file",
        default=str(DEFAULT_RULES_PATH),
        help="Path to the markdown file that defines Angular-to-React migration rules.",
    )
    parser.add_argument(
        "--source-file",
        action="append",
        default=[],
        help="Path to an Angular source file. Repeat the flag to include multiple files.",
    )
    parser.add_argument(
        "--source-text",
        help="Inline Angular source text. If omitted, the script also accepts piped stdin.",
    )
    parser.add_argument(
        "--stdin-label",
        default="stdin-input",
        help="Label to use when Angular source is read from stdin.",
    )
    parser.add_argument(
        "--source-text-label",
        default="inline-input",
        help="Label to use for the --source-text input.",
    )
    parser.add_argument(
        "--extra-instructions",
        default="",
        help="Additional migration instructions appended after the markdown rules.",
    )
    parser.add_argument(
        "--base-url",
        default=DEFAULT_BASE_URL,
        help="Base URL for the OpenCode server.",
    )
    parser.add_argument(
        "--provider-id",
        help="Optional provider ID for OpenCode model selection.",
    )
    parser.add_argument(
        "--model-id",
        help="Optional model ID for OpenCode model selection.",
    )
    parser.add_argument(
        "--agent-id",
        help="Optional OpenCode agent ID to run the prompt under.",
    )
    parser.add_argument(
        "--username",
        help="Optional basic-auth username for the OpenCode server.",
    )
    parser.add_argument(
        "--password",
        help="Optional basic-auth password for the OpenCode server.",
    )
    parser.add_argument(
        "--session-title",
        default="Angular to React Migration",
        help="Session title used when creating the OpenCode conversation.",
    )
    parser.add_argument(
        "--output-file",
        help="Optional file path where the converted React output should be written.",
    )
    parser.add_argument(
        "--print-prompt",
        action="store_true",
        help="Print the generated prompt before sending it to OpenCode.",
    )
    parser.add_argument(
        "--skip-healthcheck",
        action="store_true",
        help="Skip the initial GET /global/health check.",
    )
    return parser.parse_args()


def read_text_file(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except FileNotFoundError as exc:
        raise OpenCodeError(f"Required file not found: {path}") from exc


def collect_source_inputs(args: argparse.Namespace) -> list[SourceInput]:
    collected: list[SourceInput] = []

    for raw_path in args.source_file:
        path = Path(raw_path)
        collected.append(SourceInput(label=str(path), content=read_text_file(path)))

    if args.source_text:
        collected.append(SourceInput(label=args.source_text_label, content=args.source_text))

    if not collected and not sys.stdin.isatty():
        stdin_content = sys.stdin.read()
        if stdin_content.strip():
            collected.append(SourceInput(label=args.stdin_label, content=stdin_content))

    if not collected:
        raise OpenCodeError(
            "No Angular source was provided. Use --source-file, --source-text, or pipe source code via stdin."
        )

    return collected


def infer_code_block_language(label: str) -> str:
    suffix = Path(label).suffix.lower()
    mapping = {
        ".ts": "ts",
        ".tsx": "tsx",
        ".js": "js",
        ".jsx": "jsx",
        ".html": "html",
        ".css": "css",
        ".scss": "scss",
        ".json": "json",
    }
    return mapping.get(suffix, "text")


def build_prompt(rules_markdown: str, sources: list[SourceInput], extra_instructions: str = "") -> str:
    source_sections: list[str] = []
    for source in sources:
        code_block_language = infer_code_block_language(source.label)
        source_sections.append(
            "\n".join(
                [
                    f"### Source: {source.label}",
                    f"```{code_block_language}",
                    source.content.rstrip(),
                    "```",
                ]
            )
        )

    prompt_sections = [
        "You are a migration agent that converts Angular code into production-ready React code.",
        "Follow the migration rules exactly, preserve behavior, and return the final answer in the format requested by the rules.",
        "",
        "## Migration Rules",
        rules_markdown.strip(),
        "",
        "## Conversion Task",
        "Convert every Angular input below into equivalent React output.",
        "Keep file boundaries clear, preserve business logic, and mention assumptions only after the converted code.",
        "",
        "## Angular Inputs",
        "\n\n".join(source_sections),
    ]

    if extra_instructions.strip():
        prompt_sections.extend(["", "## Extra Instructions", extra_instructions.strip()])

    return "\n".join(prompt_sections).strip() + "\n"


def extract_response_text(response: dict[str, Any]) -> str:
    parts = response.get("parts", [])
    if not isinstance(parts, list):
        raise OpenCodeError(f"OpenCode response did not include a parts list: {response!r}")

    text_segments: list[str] = []
    for part in parts:
        if not isinstance(part, dict):
            continue

        if isinstance(part.get("text"), str):
            text_segments.append(part["text"])
            continue

        if isinstance(part.get("content"), str):
            text_segments.append(part["content"])
            continue

        data = part.get("data")
        if isinstance(data, dict) and isinstance(data.get("text"), str):
            text_segments.append(data["text"])

    if text_segments:
        return "\n".join(segment.rstrip() for segment in text_segments if segment.strip()).strip() + "\n"

    info = response.get("info")
    if isinstance(info, dict) and isinstance(info.get("structured_output"), (dict, list)):
        return json.dumps(info["structured_output"], indent=2) + "\n"

    raise OpenCodeError(f"OpenCode response did not contain text output: {response!r}")


def write_output(output_path: str, content: str) -> None:
    path = Path(output_path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def main() -> int:
    args = parse_args()
    rules_markdown = read_text_file(Path(args.rules_file))
    source_inputs = collect_source_inputs(args)
    prompt = build_prompt(rules_markdown, source_inputs, args.extra_instructions)

    if args.print_prompt:
        print(prompt, file=sys.stderr)

    client = OpenCodeClient(
        base_url=args.base_url,
        username=args.username,
        password=args.password,
    )

    if not args.skip_healthcheck:
        client.healthcheck()

    session_id = client.create_session(args.session_title)
    response = client.send_prompt(
        session_id,
        prompt,
        provider_id=args.provider_id,
        model_id=args.model_id,
        agent_id=args.agent_id,
    )
    output = extract_response_text(response)

    if args.output_file:
        write_output(args.output_file, output)

    sys.stdout.write(output)
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except OpenCodeError as exc:
        print(f"error: {exc}", file=sys.stderr)
        raise SystemExit(1) from exc
