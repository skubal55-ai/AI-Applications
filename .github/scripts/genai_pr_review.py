#!/usr/bin/env python3
"""
GenAI-Powered PR Code Review
----------------------------
Fetches the diff of the current pull request, sends it to OpenAI's GPT-4o,
and posts the AI review as a pull-request comment via the GitHub REST API.

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

import openai
import requests

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------

OPENAI_API_KEY = os.environ.get("OPENAI_API_KEY", "")
GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN", "")
REPO_NAME = os.environ.get("REPO_NAME", "")
PR_NUMBER = os.environ.get("PR_NUMBER", "")
BASE_SHA = os.environ.get("BASE_SHA", "")
HEAD_SHA = os.environ.get("HEAD_SHA", "")

# Maximum characters of diff to send to the model (keep below context limit)
MAX_DIFF_CHARS = 24_000

REVIEW_SYSTEM_PROMPT = textwrap.dedent("""
    You are an expert software engineer conducting a thorough code review.
    Your feedback must be:
    - Actionable and specific (reference file names and line ranges when possible)
    - Categorised with clear severity levels: CRITICAL, WARNING, SUGGESTION, INFO
    - Written in a constructive, respectful tone

    Focus areas for this Spring Boot + Android codebase:
    1. Security vulnerabilities (injection, insecure defaults, exposed secrets)
    2. Correctness bugs (null dereferences, race conditions, wrong logic)
    3. Performance issues (N+1 queries, unindexed lookups, excessive memory)
    4. Code quality (SOLID principles, DRY, naming, test coverage gaps)
    5. Android-specific concerns (main-thread I/O, memory leaks, missing permissions)

    Respond in Markdown using this structure:
    ## 🤖 AI Code Review Summary
    ### [CRITICAL] …
    ### [WARNING] …
    ### [SUGGESTION] …
    ### [INFO] …
    ## Overall Assessment
    …
""").strip()


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def get_diff() -> str:
    """Return the git diff between base and head commits, trimmed to MAX_DIFF_CHARS."""
    try:
        result = subprocess.run(
            ["git", "diff", f"{BASE_SHA}...{HEAD_SHA}"],
            capture_output=True,
            text=True,
            check=True,
        )
        diff = result.stdout
    except subprocess.CalledProcessError as exc:
        print(f"[genai_pr_review] git diff failed: {exc.stderr}", file=sys.stderr)
        diff = ""

    if len(diff) > MAX_DIFF_CHARS:
        diff = diff[:MAX_DIFF_CHARS] + "\n\n[... diff truncated for brevity ...]"
    return diff


def call_openai(diff: str) -> str:
    """Send the diff to OpenAI and return the review text."""
    client = openai.OpenAI(api_key=OPENAI_API_KEY)

    user_message = (
        f"Please review the following pull request diff for the Salon Management App "
        f"(Spring Boot backend + Android client):\n\n```diff\n{diff}\n```"
    )

    response = client.chat.completions.create(
        model="gpt-4o",
        messages=[
            {"role": "system", "content": REVIEW_SYSTEM_PROMPT},
            {"role": "user", "content": user_message},
        ],
        max_tokens=2048,
        temperature=0.2,
    )
    return response.choices[0].message.content or ""


def post_pr_comment(body: str) -> None:
    """Post *body* as a comment on the GitHub pull request."""
    url = f"https://api.github.com/repos/{REPO_NAME}/issues/{PR_NUMBER}/comments"
    headers = {
        "Authorization": f"Bearer {GITHUB_TOKEN}",
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
    }
    resp = requests.post(url, json={"body": body}, headers=headers, timeout=30)
    if resp.status_code not in (200, 201):
        print(
            f"[genai_pr_review] Failed to post comment: {resp.status_code} {resp.text}",
            file=sys.stderr,
        )
    else:
        print("[genai_pr_review] PR comment posted successfully.")


def save_report(body: str, path: str = "ai-review-report.md") -> None:
    with open(path, "w", encoding="utf-8") as fh:
        fh.write(body)
    print(f"[genai_pr_review] Review saved to {path}")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> None:
    missing = [v for v in ("OPENAI_API_KEY", "GITHUB_TOKEN", "REPO_NAME", "PR_NUMBER")
               if not os.environ.get(v)]
    if missing:
        print(
            f"[genai_pr_review] Missing required env vars: {', '.join(missing)}. "
            "Skipping AI review.",
            file=sys.stderr,
        )
        save_report("## AI Review skipped\n\nRequired secrets not configured.")
        sys.exit(0)

    print("[genai_pr_review] Fetching diff …")
    diff = get_diff()
    if not diff.strip():
        print("[genai_pr_review] Empty diff — nothing to review.")
        save_report("## AI Review skipped\n\nNo code changes detected in this PR.")
        return

    print(f"[genai_pr_review] Diff length: {len(diff)} chars")
    print("[genai_pr_review] Calling OpenAI GPT-4o …")
    review = call_openai(diff)

    full_report = (
        f"{review}\n\n"
        "---\n"
        "*This review was generated automatically by [GenAI CI/CD Integration]"
        f"(https://github.com/{REPO_NAME}/blob/HEAD/.github/scripts/genai_pr_review.py). "
        "Treat it as a helpful second pair of eyes — always apply human judgment.*"
    )

    save_report(full_report)

    print("[genai_pr_review] Posting comment to PR …")
    post_pr_comment(full_report)


if __name__ == "__main__":
    main()
