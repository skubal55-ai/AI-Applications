"""Tests for the FileScanner module."""

import pytest
from pathlib import Path

from agent.file_scanner import FileScanner


ANGULAR_APP = Path(__file__).parent.parent / "angular-salon-app"


@pytest.fixture
def scanner():
    return FileScanner()


def test_scan_finds_components(scanner):
    scan = scanner.scan(ANGULAR_APP)
    assert len(scan.components) > 0, "Should discover at least one Angular component"


def test_scan_finds_services(scanner):
    scan = scanner.scan(ANGULAR_APP)
    assert len(scan.services) > 0, "Should discover at least one Angular service"


def test_scan_finds_models(scanner):
    scan = scanner.scan(ANGULAR_APP)
    assert len(scan.models) > 0, "Should discover at least one Angular model"


def test_scan_finds_guards(scanner):
    scan = scanner.scan(ANGULAR_APP)
    assert len(scan.guards) > 0, "Should discover at least one Angular guard"


def test_scan_finds_routing(scanner):
    scan = scanner.scan(ANGULAR_APP)
    assert scan.routing_file is not None, "Should discover the routing module"


def test_scan_finds_environment_files(scanner):
    scan = scanner.scan(ANGULAR_APP)
    assert len(scan.environment_files) > 0, "Should discover environment files"


def test_component_names_are_pascal_case(scanner):
    scan = scanner.scan(ANGULAR_APP)
    for component in scan.components:
        assert component.name[0].isupper(), (
            f"Component name '{component.name}' should start with uppercase"
        )
        assert component.name.endswith("Component"), (
            f"Component name '{component.name}' should end with 'Component'"
        )


def test_component_has_ts_source(scanner):
    scan = scanner.scan(ANGULAR_APP)
    for component in scan.components:
        assert component.ts_source, (
            f"Component {component.name} should have non-empty TypeScript source"
        )


def test_components_with_html_have_html_source(scanner):
    scan = scanner.scan(ANGULAR_APP)
    for component in scan.components:
        if component.html_path:
            assert component.html_source, (
                f"Component {component.name} html_path set but html_source is empty"
            )


def test_scan_raises_on_missing_dir(scanner):
    with pytest.raises(FileNotFoundError):
        scanner.scan("/nonexistent/path/to/angular/app")


def test_scan_summary_contains_counts(scanner):
    scan = scanner.scan(ANGULAR_APP)
    summary = scan.summary()
    assert "Components" in summary
    assert "Services" in summary
    assert "Models" in summary
    assert "Guards" in summary


def test_total_files_count(scanner):
    scan = scanner.scan(ANGULAR_APP)
    assert scan.total_files > 0
