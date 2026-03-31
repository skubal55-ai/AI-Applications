"""
File scanner for the Angular-to-React Migration Agent.

Discovers and categorizes Angular source files in a given project directory,
grouping related .ts, .html, and .scss files by component.
"""

import logging
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, List, Optional

logger = logging.getLogger(__name__)


@dataclass
class AngularComponent:
    """Represents a single Angular component (may have .ts, .html, and .scss parts)."""
    name: str           # e.g. "LoginComponent"
    ts_path: Path
    html_path: Optional[Path] = None
    scss_path: Optional[Path] = None

    @property
    def ts_source(self) -> str:
        return self.ts_path.read_text(encoding="utf-8")

    @property
    def html_source(self) -> str:
        return self.html_path.read_text(encoding="utf-8") if self.html_path else ""

    @property
    def scss_source(self) -> str:
        return self.scss_path.read_text(encoding="utf-8") if self.scss_path else ""


@dataclass
class AngularService:
    """Represents an Angular service file."""
    name: str           # e.g. "AuthService"
    ts_path: Path

    @property
    def ts_source(self) -> str:
        return self.ts_path.read_text(encoding="utf-8")


@dataclass
class AngularModel:
    """Represents an Angular model/interface file."""
    name: str           # e.g. "user.model"
    ts_path: Path

    @property
    def ts_source(self) -> str:
        return self.ts_path.read_text(encoding="utf-8")


@dataclass
class AngularGuard:
    """Represents an Angular route guard."""
    name: str
    ts_path: Path

    @property
    def ts_source(self) -> str:
        return self.ts_path.read_text(encoding="utf-8")


@dataclass
class AngularProjectScan:
    """Result of scanning an Angular project."""
    root: Path
    components: List[AngularComponent] = field(default_factory=list)
    services: List[AngularService] = field(default_factory=list)
    models: List[AngularModel] = field(default_factory=list)
    guards: List[AngularGuard] = field(default_factory=list)
    routing_file: Optional[Path] = None
    environment_files: List[Path] = field(default_factory=list)
    other_ts_files: List[Path] = field(default_factory=list)

    @property
    def total_files(self) -> int:
        return (
            len(self.components)
            + len(self.services)
            + len(self.models)
            + len(self.guards)
            + (1 if self.routing_file else 0)
            + len(self.environment_files)
        )

    def summary(self) -> str:
        return (
            f"Angular Project Scan — {self.root}\n"
            f"  Components:   {len(self.components)}\n"
            f"  Services:     {len(self.services)}\n"
            f"  Models:       {len(self.models)}\n"
            f"  Guards:       {len(self.guards)}\n"
            f"  Routing:      {'yes' if self.routing_file else 'no'}\n"
            f"  Environments: {len(self.environment_files)}\n"
            f"  Total:        {self.total_files} logical units"
        )


class FileScanner:
    """Scans an Angular project directory and returns a structured scan result."""

    def scan(self, project_root: str | Path) -> AngularProjectScan:
        root = Path(project_root).resolve()
        if not root.exists():
            raise FileNotFoundError(f"Angular project root not found: {root}")

        scan = AngularProjectScan(root=root)

        # Collect all .ts files under src/
        src_dir = root / "src"
        if not src_dir.exists():
            src_dir = root  # fallback: scan from root

        all_ts = sorted(src_dir.rglob("*.ts"))
        all_html = sorted(src_dir.rglob("*.html"))
        all_scss = sorted(src_dir.rglob("*.scss"))

        # Build lookup maps for quick pairing
        html_by_stem: Dict[str, Path] = {p.stem: p for p in all_html}
        scss_by_stem: Dict[str, Path] = {
            p.stem: p for p in all_scss if not p.name.endswith("styles.scss")
        }

        for ts_path in all_ts:
            name = ts_path.name

            # Skip spec/test files
            if ".spec." in name:
                continue

            if name.endswith(".component.ts"):
                comp_stem = ts_path.stem  # e.g. "login.component"
                base_stem = comp_stem.replace(".component", "")
                component_name = self._to_class_name(base_stem) + "Component"

                html_path = html_by_stem.get(comp_stem)
                scss_path = scss_by_stem.get(comp_stem)

                scan.components.append(
                    AngularComponent(
                        name=component_name,
                        ts_path=ts_path,
                        html_path=html_path,
                        scss_path=scss_path,
                    )
                )
                logger.debug("Found component: %s", component_name)

            elif name.endswith(".service.ts"):
                service_stem = ts_path.stem.replace(".service", "")
                service_name = self._to_class_name(service_stem) + "Service"
                scan.services.append(AngularService(name=service_name, ts_path=ts_path))
                logger.debug("Found service: %s", service_name)

            elif name.endswith(".model.ts"):
                model_name = ts_path.stem  # e.g. "user.model"
                scan.models.append(AngularModel(name=model_name, ts_path=ts_path))
                logger.debug("Found model: %s", model_name)

            elif name.endswith(".guard.ts"):
                guard_stem = ts_path.stem.replace(".guard", "")
                guard_name = self._to_class_name(guard_stem) + "Guard"
                scan.guards.append(AngularGuard(name=guard_name, ts_path=ts_path))
                logger.debug("Found guard: %s", guard_name)

            elif "routing" in name.lower() and name.endswith(".ts"):
                scan.routing_file = ts_path
                logger.debug("Found routing file: %s", ts_path)

            elif "environment" in name.lower() and name.endswith(".ts"):
                scan.environment_files.append(ts_path)
                logger.debug("Found environment file: %s", ts_path)

            else:
                scan.other_ts_files.append(ts_path)

        return scan

    @staticmethod
    def _to_class_name(kebab_or_snake: str) -> str:
        """Convert kebab-case or snake_case to PascalCase."""
        return "".join(
            word.capitalize()
            for word in kebab_or_snake.replace("-", "_").split("_")
        )
