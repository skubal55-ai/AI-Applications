#!/usr/bin/env python3
"""
GenAI Release Notes Generator
------------------------------
Collects the git log between two refs, summarises commit messages, and
uses GPT-4o to produce human-readable, categorised release notes.

Required environment variables:
  OPENAI_API_KEY  – OpenAI secret key
  GITHUB_TOKEN    – GitHub Actions token
  REPO_NAME       – e.g. "owner/repo"
  FROM_TAG        – starting ref (exclusive)
  TO_TAG          – ending ref (inclusive, e.g. HEAD or v1.2.0)
  RELEASE_TAG     – tag name for the new release (used in the heading)
"""

from __future__ import annotations

import os
import subprocess
import sys
import textwrap

import openai
import requests

OPENAI_API_KEY = os.environ.get("OPENAI_API_KEY", "")
GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN", "")
REPO_NAME = os.environ.get("REPO_NAME", "")
FROM_TAG = os.environ.get("FROM_TAG", "")
TO_TAG = os.environ.get("TO_TAG", "HEAD")
RELEASE_TAG = os.environ.get("RELEASE_TAG", TO_TAG)

MAX_COMMITS = 200

SYSTEM_PROMPT = textwrap.dedent("""
    You are a technical writer creating release notes for a Salon Management App
    (Spring Boot API + Android client).

    Given a list of git commit messages, produce polished, user-friendly release
    notes structured as:

    ## What's New
    (new features, grouped thematically)

    ## Improvements
    (refactors, performance, UX enhancements)

    ## Bug Fixes
    (bugs resolved)

    ## Security
    (security patches, dependency updates)

    ## Breaking Changes
    (only if present; API changes, config changes)

    Rules:
    - Exclude trivial commits: merge commits, version bumps, "fix typo", "wip"
    - Use present tense ("Add", "Fix", "Improve")
    - Each bullet max 120 characters
    - Group related commits into a single bullet where sensible
    - Add a one-sentence intro paragraph after the main heading
""").strip()


def get_commit_log() -> str:
    if not FROM_TAG:
        return ""
    try:
        result = subprocess.run(
            [
                "git", "log",
                f"{FROM_TAG}..{TO_TAG}",
                "--pretty=format:- %s (%h)",
                "--no-merges",
                f"--max-count={MAX_COMMITS}",
            ],
            capture_output=True, text=True, check=True,
        )
        return result.stdout.strip()
    except subprocess.CalledProcessError as exc:
        print(f"[genai_release_notes] git log failed: {exc.stderr}", file=sys.stderr)
        return ""


def call_openai(commit_log: str) -> str:
    client = openai.OpenAI(api_key=OPENAI_API_KEY)
    user_message = (
        f"Generate release notes for **{RELEASE_TAG}** based on these commits "
        f"(since {FROM_TAG}):\n\n{commit_log}"
    )
    response = client.chat.completions.create(
        model="gpt-4o",
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": user_message},
        ],
        max_tokens=1500,
        temperature=0.3,
    )
    return response.choices[0].message.content or ""


def save_notes(content: str, path: str = "release-notes.md") -> None:
    with open(path, "w", encoding="utf-8") as fh:
        fh.write(content)
    print(f"[genai_release_notes] Saved to {path}")


def main() -> None:
    if not OPENAI_API_KEY:
        save_notes("## Release Notes\n\nAI generation skipped — OPENAI_API_KEY not set.")
        sys.exit(0)

    print(f"[genai_release_notes] Collecting commits from {FROM_TAG} to {TO_TAG} …")
    commit_log = get_commit_log()

    if not commit_log:
        save_notes(f"## {RELEASE_TAG}\n\nNo commits found in this range.")
        return

    line_count = commit_log.count("\n") + 1
    print(f"[genai_release_notes] {line_count} commit(s) found. Calling OpenAI …")

    notes = call_openai(commit_log)

    full_notes = (
        f"# Release Notes – {RELEASE_TAG}\n\n"
        f"{notes}\n\n"
        "---\n"
        "*Release notes generated with AI assistance.*"
    )
    save_notes(full_notes)
    print("[genai_release_notes] Done.")


if __name__ == "__main__":
    main()
