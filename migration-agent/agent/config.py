"""
Configuration for the Angular-to-React Migration Agent.

Set the following environment variables:
  OPENCODE_API_KEY   - Your OpenCode/OpenAI API key
  OPENCODE_BASE_URL  - Base URL for the AI endpoint (defaults to OpenCode)
  OPENCODE_MODEL     - Model name to use (defaults to gpt-4o)
"""

import os
from dataclasses import dataclass, field
from typing import Optional


@dataclass
class AgentConfig:
    # OpenCode / AI endpoint configuration
    api_key: str = field(
        default_factory=lambda: os.environ.get("OPENCODE_API_KEY", "")
    )
    base_url: str = field(
        default_factory=lambda: os.environ.get(
            "OPENCODE_BASE_URL", "https://api.openai.com/v1"
        )
    )
    model: str = field(
        default_factory=lambda: os.environ.get("OPENCODE_MODEL", "gpt-4o")
    )

    # Migration rules document path
    rules_file: str = field(
        default_factory=lambda: os.path.join(
            os.path.dirname(os.path.dirname(__file__)),
            "ANGULAR_TO_REACT_MIGRATION_RULES.md",
        )
    )

    # Output directory for migrated React app
    output_dir: str = field(
        default_factory=lambda: os.environ.get(
            "MIGRATION_OUTPUT_DIR",
            os.path.join(os.path.dirname(os.path.dirname(__file__)), "react-salon-app"),
        )
    )

    # AI generation parameters
    max_tokens: int = 4096
    temperature: float = 0.1  # Low temperature for deterministic code generation
    request_timeout: int = 120  # seconds

    # Retry configuration
    max_retries: int = 3
    retry_backoff: float = 2.0  # exponential backoff multiplier

    # Whether to include comments explaining the migration in output files
    include_migration_comments: bool = True

    # Whether to generate a migration report
    generate_report: bool = True

    def validate(self) -> None:
        if not self.api_key:
            raise ValueError(
                "OPENCODE_API_KEY environment variable is not set. "
                "Please set it to your API key."
            )
        if not os.path.exists(self.rules_file):
            raise FileNotFoundError(
                f"Migration rules file not found: {self.rules_file}"
            )


# Global config instance
config = AgentConfig()
