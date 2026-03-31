"""Tests for the OutputWriter module."""

import pytest
import tempfile
from pathlib import Path

from agent.output_writer import OutputWriter


@pytest.fixture
def tmp_output(tmp_path):
    return OutputWriter(tmp_path)


def test_write_boilerplate_creates_api_client(tmp_output, tmp_path):
    tmp_output.write_boilerplate()
    assert (tmp_path / "src" / "services" / "apiClient.ts").exists()


def test_write_boilerplate_creates_auth_context(tmp_output, tmp_path):
    tmp_output.write_boilerplate()
    assert (tmp_path / "src" / "context" / "AuthContext.tsx").exists()


def test_write_boilerplate_creates_protected_route(tmp_output, tmp_path):
    tmp_output.write_boilerplate()
    assert (tmp_path / "src" / "components" / "ProtectedRoute.tsx").exists()


def test_write_boilerplate_creates_package_json(tmp_output, tmp_path):
    tmp_output.write_boilerplate()
    pkg = tmp_path / "package.json"
    assert pkg.exists()
    assert "react" in pkg.read_text()


def test_write_boilerplate_creates_env_files(tmp_output, tmp_path):
    tmp_output.write_boilerplate()
    assert (tmp_path / ".env.development").exists()
    assert (tmp_path / ".env.production").exists()


def test_write_boilerplate_creates_vite_config(tmp_output, tmp_path):
    tmp_output.write_boilerplate()
    assert (tmp_path / "vite.config.ts").exists()


def test_write_component_creates_correct_path(tmp_output, tmp_path):
    content = "const Login = () => <div>Login</div>; export default Login;"
    path = tmp_output.write_component("LoginComponent", content)
    assert path == tmp_path / "src" / "components" / "Login" / "Login.tsx"
    assert path.read_text() == content


def test_write_service_creates_correct_path(tmp_output, tmp_path):
    content = "export const authService = {};"
    path = tmp_output.write_service("AuthService", content)
    assert path == tmp_path / "src" / "services" / "authService.ts"
    assert path.read_text() == content


def test_write_type_creates_correct_path(tmp_output, tmp_path):
    content = "export interface User { id: number; }"
    path = tmp_output.write_type("user.model", content)
    assert path == tmp_path / "src" / "types" / "user.ts"


def test_write_app_creates_app_tsx(tmp_output, tmp_path):
    content = "const App = () => <div>App</div>; export default App;"
    path = tmp_output.write_app(content)
    assert path == tmp_path / "src" / "App.tsx"


def test_write_report_creates_markdown(tmp_output, tmp_path):
    report = "# Migration Report\n\nAll good."
    path = tmp_output.write_report(report)
    assert path == tmp_path / "MIGRATION_REPORT.md"
    assert "Migration Report" in path.read_text()


def test_parse_env_sections_extracts_multiple_files(tmp_output, tmp_path):
    content = (
        "// --- FILE: .env.development ---\n"
        "VITE_API_URL=http://localhost:8080\n"
        "// --- FILE: src/config/env.ts ---\n"
        "export const API_URL = import.meta.env.VITE_API_URL;\n"
    )
    sections = OutputWriter._parse_env_sections(content)
    assert ".env.development" in sections
    assert "src/config/env.ts" in sections
    assert "VITE_API_URL" in sections[".env.development"]


def test_pascal_to_component_path():
    assert OutputWriter._pascal_to_component_path("LoginComponent") == "Login/Login.tsx"
    assert OutputWriter._pascal_to_component_path("ServiceListComponent") == "ServiceList/ServiceList.tsx"


def test_service_filename():
    assert OutputWriter._service_filename("AuthService") == "authService.ts"
    assert OutputWriter._service_filename("AppointmentService") == "appointmentService.ts"
