#!/usr/bin/env python3
"""
GenAI Security Scanner
----------------------
Performs AI-powered static security analysis using GPT-4o.

Two scan modes:
  diff  – analyse only files changed in the current PR / push
  full  – scan the entire codebase (scheduled weekly runs)

Findings are posted as a PR comment (diff mode) and always saved as
ai-security-report.md.  Critical finding count is exported as a GitHub
Actions step output so the workflow can fail the build when necessary.

Required environment variables:
  OPENAI_API_KEY  – OpenAI secret key
  GITHUB_TOKEN    – GitHub Actions token
  REPO_NAME       – e.g. "owner/repo"
  PR_NUMBER       – pull request number (empty string for push/schedule)
  BASE_SHA        – base commit SHA (empty string for schedule)
  HEAD_SHA        – head commit SHA
  SCAN_MODE       – "diff" or "full"
"""

from __future__ import annotations

import os
import re
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
SCAN_MODE = os.environ.get("SCAN_MODE", "diff")

MAX_FILE_CHARS = 8_000
MAX_FILES = 15

# ---------------------------------------------------------------------------
# Prompts
# ---------------------------------------------------------------------------

SYSTEM_PROMPT = textwrap.dedent("""
    You are a security-focused code auditor specialising in Java web applications
    (Spring Boot) and Android mobile apps.

    Analyse the provided source code for security vulnerabilities and rank each
    finding by severity using OWASP categories where applicable:

    CRITICAL  – immediate exploitable risk (SQL injection, hardcoded secrets,
                broken authentication, remote code execution vectors)
    HIGH      – significant risk requiring prompt remediation (missing auth checks,
                insecure direct object reference, sensitive data exposure)
    MEDIUM    – moderate risk (CSRF not configured, weak cryptography, verbose
                error messages, missing input validation)
    LOW       – minor or defence-in-depth concerns (logging sensitive data,
                missing security headers, overly permissive CORS)
    INFO      – best-practice recommendations with no direct exploitability

    For every finding provide:
    - **Severity** label
    - **File & approximate line** (if determinable)
    - **Description** of the vulnerability
    - **Proof-of-concept** attack scenario (one sentence)
    - **Remediation** with a concrete code snippet

    Output in Markdown. Begin with an executive summary table.
""").strip()


# ---------------------------------------------------------------------------
# File collection
# ---------------------------------------------------------------------------

SECURITY_RELEVANT_EXTENSIONS = {".java", ".xml", ".yml", ".yaml", ".properties", ".json"}
SKIP_DIRS = {"test", "androidTest", "build", "target", ".git", "node_modules"}


def get_changed_files() -> list[str]:
    if not BASE_SHA:
        return []
    try:
        result = subprocess.run(
            ["git", "diff", "--name-only", "--diff-filter=AM", f"{BASE_SHA}...{HEAD_SHA}"],
            capture_output=True, text=True, check=True,
        )
    except subprocess.CalledProcessError as exc:
        print(f"[genai_security_scan] git diff failed: {exc.stderr}", file=sys.stderr)
        return []
    return [
        f.strip() for f in result.stdout.splitlines()
        if Path(f.strip()).suffix in SECURITY_RELEVANT_EXTENSIONS
        and not any(skip in f for skip in SKIP_DIRS)
    ][:MAX_FILES]


def get_all_source_files() -> list[str]:
    root = Path(".")
    files: list[str] = []
    for ext in SECURITY_RELEVANT_EXTENSIONS:
        for p in root.rglob(f"*{ext}"):
            parts = set(p.parts)
            if parts & SKIP_DIRS:
                continue
            files.append(str(p))
            if len(files) >= MAX_FILES:
                return files
    return files


def read_file_snippet(path: str) -> str:
    try:
        content = Path(path).read_text(encoding="utf-8", errors="replace")
        if len(content) > MAX_FILE_CHARS:
            content = content[:MAX_FILE_CHARS] + "\n// [... file truncated ...]"
        return content
    except OSError:
        return f"// Could not read {path}"


# ---------------------------------------------------------------------------
# OpenAI
# ---------------------------------------------------------------------------

def call_openai(file_map: dict[str, str]) -> str:
    client = openai.OpenAI(api_key=OPENAI_API_KEY)

    parts = ["Perform a security audit on the following files:\n"]
    for path, content in file_map.items():
        ext = Path(path).suffix.lstrip(".") or "text"
        parts.append(f"\n### `{path}`\n```{ext}\n{content}\n```")

    user_message = "\n".join(parts)

    response = client.chat.completions.create(
        model="gpt-4o",
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": user_message},
        ],
        max_tokens=3000,
        temperature=0.1,
    )
    return response.choices[0].message.content or ""


# ---------------------------------------------------------------------------
# GitHub helpers
# ---------------------------------------------------------------------------

def post_pr_comment(body: str) -> None:
    if not PR_NUMBER:
        return
    url = f"https://api.github.com/repos/{REPO_NAME}/issues/{PR_NUMBER}/comments"
    headers = {
        "Authorization": f"Bearer {GITHUB_TOKEN}",
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
    }
    resp = requests.post(url, json={"body": body}, headers=headers, timeout=30)
    if resp.status_code not in (200, 201):
        print(f"[genai_security_scan] PR comment failed: {resp.status_code}", file=sys.stderr)
    else:
        print("[genai_security_scan] Security report posted to PR.")


def set_step_output(name: str, value: str) -> None:
    """Write a GitHub Actions step output."""
    output_file = os.environ.get("GITHUB_OUTPUT")
    if output_file:
        with open(output_file, "a", encoding="utf-8") as fh:
            fh.write(f"{name}={value}\n")
    else:
        print(f"[genai_security_scan] Step output {name}={value}")


def count_critical(report: str) -> int:
    return len(re.findall(r"\bCRITICAL\b", report, re.IGNORECASE))


def save_report(body: str, path: str = "ai-security-report.md") -> None:
    with open(path, "w", encoding="utf-8") as fh:
        fh.write(body)
    print(f"[genai_security_scan] Report saved to {path}")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> None:
    missing = [v for v in ("OPENAI_API_KEY",) if not os.environ.get(v)]
    if missing:
        msg = (
            "## AI Security Scan skipped\n\n"
            f"Required secrets not configured: {', '.join(missing)}"
        )
        save_report(msg)
        set_step_output("critical_count", "0")
        sys.exit(0)

    if SCAN_MODE == "full":
        print("[genai_security_scan] Full scan mode — collecting all source files …")
        file_paths = get_all_source_files()
    else:
        print("[genai_security_scan] Diff scan mode — collecting changed files …")
        file_paths = get_changed_files()

    if not file_paths:
        msg = "## AI Security Scan\n\nNo relevant files to scan."
        save_report(msg)
        set_step_output("critical_count", "0")
        return

    print(f"[genai_security_scan] Scanning {len(file_paths)} file(s) …")
    file_map = {p: read_file_snippet(p) for p in file_paths}

    print("[genai_security_scan] Calling OpenAI GPT-4o …")
    findings = call_openai(file_map)

    critical_count = count_critical(findings)
    set_step_output("critical_count", str(critical_count))

    mode_label = "Full Codebase Scan" if SCAN_MODE == "full" else "PR Diff Scan"
    full_report = (
        f"## 🔒 AI Security Report ({mode_label})\n\n"
        f"{findings}\n\n"
        "---\n"
        "*Auto-generated by [GenAI Security Scanner]"
        f"(https://github.com/{REPO_NAME}/blob/HEAD/.github/scripts/genai_security_scan.py). "
        "Validate all findings manually before acting on them.*"
    )

    save_report(full_report)
    if PR_NUMBER:
        post_pr_comment(full_report)

    if critical_count > 0:
        print(
            f"[genai_security_scan] ⚠️  {critical_count} CRITICAL finding(s) detected!",
            file=sys.stderr,
        )


if __name__ == "__main__":
    main()
