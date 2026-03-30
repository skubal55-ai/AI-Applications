#!/usr/bin/env python3
"""
GenAI Test Gap Analyser
-----------------------
Inspects changed source files in a pull request, identifies production
code that lacks corresponding test coverage, and uses GPT-4o to generate
concrete JUnit 5 / Mockito (backend) or Android JUnit (mobile) test stubs.

Required environment variables:
  OPENAI_API_KEY  – OpenAI secret key
  GITHUB_TOKEN    – GitHub Actions token (write: pull-requests)
  REPO_NAME       – e.g. "owner/repo"
  PR_NUMBER       – pull request number as a string
  BASE_SHA        – base commit SHA
  HEAD_SHA        – head commit SHA
"""

from __future__ import annotations

import os
import subprocess
import sys
import textwrap
from pathlib import Path

import openai
import requests

OPENAI_API_KEY = os.environ.get("OPENAI_API_KEY", "")
GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN", "")
REPO_NAME = os.environ.get("REPO_NAME", "")
PR_NUMBER = os.environ.get("PR_NUMBER", "")
BASE_SHA = os.environ.get("BASE_SHA", "")
HEAD_SHA = os.environ.get("HEAD_SHA", "")

MAX_FILE_CHARS = 6_000
MAX_FILES = 10

SYSTEM_PROMPT = textwrap.dedent("""
    You are a senior software engineer specialising in test-driven development.
    Given changed Java source files from a Spring Boot / Android project,
    produce ready-to-paste test code using:
    - JUnit 5 + Mockito for Spring Boot service/controller/utility classes
    - Android JUnit + Robolectric for Android Activity/ViewModel/Utility classes

    For every class you analyse:
    1. Identify methods that lack test coverage or have tricky edge cases.
    2. Write concrete test stubs with meaningful assertions (not empty @Test methods).
    3. Include all required imports.
    4. Annotate each test with a one-line comment explaining what it verifies.

    Respond in Markdown; use separate fenced code blocks (java) for each test class.
    Begin with a brief summary of the coverage gaps found.
""").strip()


def get_changed_java_files() -> list[str]:
    """Return paths of Java source files changed between base and head."""
    try:
        result = subprocess.run(
            ["git", "diff", "--name-only", "--diff-filter=AM", f"{BASE_SHA}...{HEAD_SHA}"],
            capture_output=True,
            text=True,
            check=True,
        )
    except subprocess.CalledProcessError as exc:
        print(f"[genai_test_suggestions] git diff failed: {exc.stderr}", file=sys.stderr)
        return []

    files = [
        f.strip()
        for f in result.stdout.splitlines()
        if f.strip().endswith(".java")
        and "/test/" not in f
        and "/androidTest/" not in f
    ]
    return files[:MAX_FILES]


def read_file_snippet(path: str) -> str:
    try:
        content = Path(path).read_text(encoding="utf-8", errors="replace")
        if len(content) > MAX_FILE_CHARS:
            content = content[:MAX_FILE_CHARS] + "\n// [... file truncated ...]"
        return content
    except OSError:
        return f"// Could not read {path}"


def build_user_message(files: list[str]) -> str:
    parts = [
        "Analyse the following changed Java files and generate test stubs for any "
        "untested or under-tested logic:\n"
    ]
    for path in files:
        snippet = read_file_snippet(path)
        parts.append(f"\n### `{path}`\n```java\n{snippet}\n```")
    return "\n".join(parts)


def call_openai(user_message: str) -> str:
    client = openai.OpenAI(api_key=OPENAI_API_KEY)
    response = client.chat.completions.create(
        model="gpt-4o",
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": user_message},
        ],
        max_tokens=3000,
        temperature=0.2,
    )
    return response.choices[0].message.content or ""


def post_pr_comment(body: str) -> None:
    url = f"https://api.github.com/repos/{REPO_NAME}/issues/{PR_NUMBER}/comments"
    headers = {
        "Authorization": f"Bearer {GITHUB_TOKEN}",
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
    }
    resp = requests.post(url, json={"body": body}, headers=headers, timeout=30)
    if resp.status_code not in (200, 201):
        print(f"[genai_test_suggestions] Failed to post comment: {resp.status_code}", file=sys.stderr)
    else:
        print("[genai_test_suggestions] Test-suggestion comment posted.")


def save_report(body: str, path: str = "ai-test-suggestions-report.md") -> None:
    with open(path, "w", encoding="utf-8") as fh:
        fh.write(body)
    print(f"[genai_test_suggestions] Report saved to {path}")


def main() -> None:
    missing = [v for v in ("OPENAI_API_KEY", "GITHUB_TOKEN", "REPO_NAME", "PR_NUMBER")
               if not os.environ.get(v)]
    if missing:
        msg = (
            "## AI Test Suggestions skipped\n\n"
            f"Required secrets not configured: {', '.join(missing)}"
        )
        save_report(msg)
        sys.exit(0)

    changed = get_changed_java_files()
    if not changed:
        msg = "## AI Test Suggestions\n\nNo production Java files changed — nothing to analyse."
        save_report(msg)
        return

    print(f"[genai_test_suggestions] Analysing {len(changed)} file(s): {changed}")
    user_message = build_user_message(changed)

    print("[genai_test_suggestions] Calling OpenAI …")
    suggestions = call_openai(user_message)

    full_report = (
        f"## 🧪 AI-Generated Test Suggestions\n\n"
        f"{suggestions}\n\n"
        "---\n"
        "*Auto-generated by [GenAI Test Suggestions]"
        f"(https://github.com/{REPO_NAME}/blob/HEAD/.github/scripts/genai_test_suggestions.py). "
        "Review, adapt, and integrate these stubs into your test suite.*"
    )

    save_report(full_report)
    post_pr_comment(full_report)


if __name__ == "__main__":
    main()
