#!/usr/bin/env python3
"""
Angular → React Migration Agent — CLI Entry Point

Usage:
    python migrate.py [OPTIONS]

Options:
    --angular-dir   Path to the Angular project to migrate (default: ./angular-salon-app)
    --output-dir    Path for the generated React project (default: ./react-salon-app)
    --api-key       OpenCode/OpenAI API key (overrides OPENCODE_API_KEY env var)
    --base-url      API base URL (overrides OPENCODE_BASE_URL env var)
    --model         Model name (overrides OPENCODE_MODEL env var)
    --no-report     Skip generating the migration report
    --dry-run       Scan and log without calling the API
    --log-level     Logging level: DEBUG | INFO | WARNING (default: INFO)
    --help          Show this help message

Environment Variables:
    OPENCODE_API_KEY       Your API key (required)
    OPENCODE_BASE_URL      API base URL (default: https://api.openai.com/v1)
    OPENCODE_MODEL         Model name (default: gpt-4o)
    MIGRATION_OUTPUT_DIR   Default output directory
"""

import argparse
import logging
import os
import sys
from pathlib import Path

# Ensure the project root is on sys.path
sys.path.insert(0, str(Path(__file__).parent))

from agent.config import AgentConfig
from agent.file_scanner import FileScanner
from agent.migration_agent import MigrationAgent


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Angular → React Migration Agent",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    parser.add_argument(
        "--angular-dir",
        default=str(Path(__file__).parent / "angular-salon-app"),
        help="Path to the Angular project root (default: ./angular-salon-app)",
    )
    parser.add_argument(
        "--output-dir",
        default=None,
        help="Path for the generated React project (default: ./react-salon-app)",
    )
    parser.add_argument(
        "--api-key",
        default=None,
        help="OpenCode/OpenAI API key (overrides OPENCODE_API_KEY)",
    )
    parser.add_argument(
        "--base-url",
        default=None,
        help="API base URL (overrides OPENCODE_BASE_URL)",
    )
    parser.add_argument(
        "--model",
        default=None,
        help="Model name to use (overrides OPENCODE_MODEL)",
    )
    parser.add_argument(
        "--no-report",
        action="store_true",
        help="Skip generating the migration report markdown file",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Scan the Angular project and log what would be migrated without calling the API",
    )
    parser.add_argument(
        "--log-level",
        choices=["DEBUG", "INFO", "WARNING", "ERROR"],
        default="INFO",
        help="Logging verbosity (default: INFO)",
    )
    return parser.parse_args()


def setup_logging(level: str) -> None:
    logging.basicConfig(
        level=getattr(logging, level),
        format="%(asctime)s [%(levelname)s] %(message)s",
        datefmt="%H:%M:%S",
    )


def dry_run(angular_dir: Path) -> None:
    """Scan and print what would be migrated, without calling the AI API."""
    print("\n=== DRY RUN — No API calls will be made ===\n")
    scanner = FileScanner()
    scan = scanner.scan(angular_dir)
    print(scan.summary())
    print("\nComponents:")
    for c in scan.components:
        parts = [str(c.ts_path.relative_to(angular_dir))]
        if c.html_path:
            parts.append(str(c.html_path.relative_to(angular_dir)))
        if c.scss_path:
            parts.append(str(c.scss_path.relative_to(angular_dir)))
        print(f"  {c.name}: {', '.join(parts)}")
    print("\nServices:")
    for s in scan.services:
        print(f"  {s.name}: {s.ts_path.relative_to(angular_dir)}")
    print("\nModels:")
    for m in scan.models:
        print(f"  {m.name}: {m.ts_path.relative_to(angular_dir)}")
    print("\nGuards:")
    for g in scan.guards:
        print(f"  {g.name}: {g.ts_path.relative_to(angular_dir)}")
    if scan.routing_file:
        print(f"\nRouting: {scan.routing_file.relative_to(angular_dir)}")
    if scan.environment_files:
        print("\nEnvironments:")
        for e in scan.environment_files:
            print(f"  {e.relative_to(angular_dir)}")
    print("\n=== End Dry Run ===")


def main() -> int:
    args = parse_args()
    setup_logging(args.log_level)

    angular_dir = Path(args.angular_dir).resolve()
    if not angular_dir.exists():
        print(f"Error: Angular project directory not found: {angular_dir}", file=sys.stderr)
        return 1

    # Dry run — no API needed
    if args.dry_run:
        dry_run(angular_dir)
        return 0

    # Build config from CLI args + env vars
    cfg = AgentConfig()
    if args.api_key:
        cfg.api_key = args.api_key
    elif not cfg.api_key:
        # Allow running without key for dry runs; block for actual migration
        print(
            "Error: OPENCODE_API_KEY is not set.\n"
            "Set it via the --api-key flag or the OPENCODE_API_KEY environment variable.",
            file=sys.stderr,
        )
        return 1

    if args.base_url:
        cfg.base_url = args.base_url
    if args.model:
        cfg.model = args.model
    if args.output_dir:
        cfg.output_dir = args.output_dir
    if args.no_report:
        cfg.generate_report = False

    # Run migration
    agent = MigrationAgent(cfg)
    try:
        run = agent.migrate(
            angular_project=angular_dir,
            react_output=args.output_dir,
        )
    except (FileNotFoundError, ValueError) as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 1

    # Print summary
    print(f"\n{'=' * 60}")
    print(f"Migration complete!")
    print(f"  Succeeded: {len(run.successes)}")
    print(f"  Failed:    {len(run.failures)}")
    print(f"  Output:    {run.react_output}")
    if run.failures:
        print("\nFailed files:")
        for f in run.failures:
            print(f"  - {f.source_file}: {f.error}")
    print(f"{'=' * 60}\n")

    return 0 if not run.failures else 2


if __name__ == "__main__":
    sys.exit(main())
