"""Tests for the PromptBuilder module."""

import pytest
from pathlib import Path

from agent.config import AgentConfig
from agent.prompt_builder import PromptBuilder


@pytest.fixture
def config():
    return AgentConfig()


@pytest.fixture
def builder(config):
    return PromptBuilder(config)


def test_system_prompt_contains_rules(builder):
    prompt = builder.build_system_prompt()
    assert "Angular" in prompt
    assert "React" in prompt
    assert "Migration Rules" in prompt


def test_system_prompt_contains_rule_ids(builder):
    prompt = builder.build_system_prompt()
    # Check for some known rule IDs from the migration guide
    assert "R-CM-01" in prompt
    assert "R-RT-01" in prompt


def test_component_prompt_contains_source(builder):
    ts_source = "export class LoginComponent implements OnInit {}"
    html_source = "<div>Login</div>"
    prompt = builder.build_component_prompt(
        ts_source=ts_source,
        html_source=html_source,
        scss_source="",
        component_name="LoginComponent",
    )
    assert ts_source in prompt
    assert html_source in prompt
    assert "LoginComponent" in prompt


def test_component_prompt_includes_scss_when_present(builder):
    scss = ".login-container { display: flex; }"
    prompt = builder.build_component_prompt(
        ts_source="...",
        html_source="...",
        scss_source=scss,
        component_name="LoginComponent",
    )
    assert scss in prompt


def test_component_prompt_excludes_scss_section_when_empty(builder):
    prompt = builder.build_component_prompt(
        ts_source="...",
        html_source="...",
        scss_source="",
        component_name="LoginComponent",
    )
    assert "Angular Component SCSS" not in prompt


def test_service_prompt_contains_source(builder):
    ts_source = "@Injectable({ providedIn: 'root' }) export class AuthService {}"
    prompt = builder.build_service_prompt(
        ts_source=ts_source,
        service_name="AuthService",
    )
    assert ts_source in prompt
    assert "AuthService" in prompt
    assert "apiClient" in prompt


def test_model_prompt_contains_source(builder):
    ts_source = "export interface User { id: number; name: string; }"
    prompt = builder.build_model_prompt(
        ts_source=ts_source,
        model_name="user.model",
    )
    assert ts_source in prompt


def test_guard_prompt_contains_source(builder):
    ts_source = "@Injectable() export class AuthGuard implements CanActivate {}"
    prompt = builder.build_guard_prompt(
        ts_source=ts_source,
        guard_name="AuthGuard",
    )
    assert ts_source in prompt
    assert "ProtectedRoute" in prompt


def test_routing_prompt_contains_source(builder):
    ts_source = "const routes: Routes = [{ path: 'home', component: HomeComponent }];"
    prompt = builder.build_routing_prompt(ts_source=ts_source)
    assert ts_source in prompt
    assert "App.tsx" in prompt


def test_environment_prompt_contains_source(builder):
    ts_source = "export const environment = { apiUrl: 'http://localhost:8080' };"
    prompt = builder.build_environment_prompt(ts_source=ts_source)
    assert ts_source in prompt
    assert ".env" in prompt


def test_pascal_conversion(builder):
    assert builder._to_pascal("login-component") == "LoginComponent"
    assert builder._to_pascal("user_card") == "UserCard"
    assert builder._to_pascal("Home") == "Home"
