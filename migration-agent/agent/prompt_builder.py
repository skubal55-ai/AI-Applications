"""
Prompt builder for the Angular-to-React Migration Agent.

Constructs system and user prompts that include:
- The full migration rules from ANGULAR_TO_REACT_MIGRATION_RULES.md
- The Angular source code to be migrated
- Specific instructions based on the file type being converted
"""

from pathlib import Path
from typing import Optional

from .config import AgentConfig


class PromptBuilder:
    """Builds structured prompts for the migration agent."""

    def __init__(self, config: AgentConfig):
        self.config = config
        self._rules: Optional[str] = None

    @property
    def rules(self) -> str:
        """Lazily load and cache the migration rules document."""
        if self._rules is None:
            self._rules = Path(self.config.rules_file).read_text(encoding="utf-8")
        return self._rules

    def build_system_prompt(self) -> str:
        """
        Build the system prompt that establishes the AI's role and provides
        the complete migration rules as context.
        """
        return f"""You are an expert Angular-to-React migration engineer.

Your task is to convert Angular application code into idiomatic, production-quality
React (TypeScript) code following the rules and conventions defined in the migration
guide below.

## Core Requirements
- Generate ONLY the converted React/TypeScript code — no explanations, no markdown
  fences, no commentary outside of code comments.
- Strictly follow every applicable rule from the migration guide (R-XX-NN identifiers).
- Produce modern React code: functional components, hooks (not class components),
  TypeScript interfaces, async/await (not Observables), React Router v6, and
  react-hook-form for forms.
- All generated code must be syntactically valid and type-safe.
- Preserve all business logic exactly — only change Angular-specific patterns.
- Use consistent, readable formatting (2-space indentation, single quotes).

## Migration Rules Reference
{self.rules}
"""

    def build_component_prompt(
        self,
        ts_source: str,
        html_source: str,
        scss_source: str,
        component_name: str,
        additional_context: str = "",
    ) -> str:
        """
        Build the user prompt for migrating a full Angular component
        (TypeScript class + HTML template + optional SCSS).

        Args:
            ts_source:          Content of the .component.ts file.
            html_source:        Content of the .component.html file.
            scss_source:        Content of the .component.scss file (may be empty).
            component_name:     Human-readable name of the component, e.g. "LoginComponent".
            additional_context: Any extra context (sibling files, shared types, etc.).

        Returns:
            A user prompt string ready to be sent to the AI.
        """
        scss_section = (
            f"\n## Angular Component SCSS\n```scss\n{scss_source}\n```"
            if scss_source.strip()
            else ""
        )
        context_section = (
            f"\n## Additional Context\n{additional_context}"
            if additional_context.strip()
            else ""
        )

        return f"""Migrate the following Angular component to React (TypeScript).

Component name: **{component_name}**

## Angular Component TypeScript (.ts)
```typescript
{ts_source}
```

## Angular Component Template (.html)
```html
{html_source}
```
{scss_section}
{context_section}

## Output Requirements
1. Produce a single `.tsx` file containing the React functional component.
2. Include a separate CSS Module block (as a comment block at the bottom of the file
   labelled `/* === CSS MODULE: {self._to_pascal(component_name)}.module.scss === */`)
   if the Angular component had non-trivial styles.
3. Add a brief JSDoc comment at the top of the file referencing the Angular origin.
4. Export the component as `default`.
5. Do NOT include markdown code fences — output raw TypeScript/TSX only.
"""

    def build_service_prompt(
        self,
        ts_source: str,
        service_name: str,
        additional_context: str = "",
    ) -> str:
        """
        Build the user prompt for migrating an Angular service to a React service module.
        """
        context_section = (
            f"\n## Additional Context\n{additional_context}"
            if additional_context.strip()
            else ""
        )

        return f"""Migrate the following Angular service to a plain TypeScript service module
suitable for use in a React application (Promise-based, no RxJS, no Angular decorators).

Service name: **{service_name}**

## Angular Service TypeScript (.ts)
```typescript
{ts_source}
```
{context_section}

## Output Requirements
1. Produce a single `.ts` file (not `.tsx`) — no JSX.
2. Replace `HttpClient` with an `apiClient` Axios instance imported from `../services/apiClient`.
3. Replace all Observables with Promises (async/await).
4. Remove `@Injectable`, `constructor` DI, and all RxJS imports.
5. Export a named object literal (e.g., `export const userService = {{ ... }};`).
6. Add a brief JSDoc comment referencing the Angular origin.
7. Do NOT include markdown code fences — output raw TypeScript only.
"""

    def build_guard_prompt(self, ts_source: str, guard_name: str) -> str:
        """Build the user prompt for migrating an Angular route guard."""
        return f"""Migrate the following Angular route guard to a React ProtectedRoute component.

Guard name: **{guard_name}**

## Angular Guard TypeScript (.ts)
```typescript
{ts_source}
```

## Output Requirements
1. Produce a single `.tsx` file named `ProtectedRoute.tsx`.
2. Use `useNavigate` from `react-router-dom` for redirects.
3. Read auth state from a `useAuth()` hook (assume it returns `{{ isAuthenticated: boolean }}`).
4. Export the component as `default`.
5. Do NOT include markdown code fences — output raw TSX only.
"""

    def build_model_prompt(self, ts_source: str, model_name: str) -> str:
        """Build the user prompt for migrating Angular model/interface files."""
        return f"""Migrate the following Angular model/interface file to a React-compatible
TypeScript types file.

Model name: **{model_name}**

## Angular Model TypeScript (.ts)
```typescript
{ts_source}
```

## Output Requirements
1. Produce a single `.ts` file (not `.tsx`).
2. Keep all interfaces and types exactly as-is (no Angular-specific decorators to remove
   from plain interfaces — just verify and preserve).
3. Place the file in `src/types/` (add a comment at the top indicating the path).
4. Do NOT include markdown code fences — output raw TypeScript only.
"""

    def build_routing_prompt(
        self, ts_source: str, additional_context: str = ""
    ) -> str:
        """Build the user prompt for migrating Angular routing module to React Router v6."""
        context_section = (
            f"\n## Additional Context\n{additional_context}"
            if additional_context.strip()
            else ""
        )
        return f"""Migrate the following Angular routing module to a React Router v6 route
configuration embedded in App.tsx.

## Angular Routing Module TypeScript (.ts)
```typescript
{ts_source}
```
{context_section}

## Output Requirements
1. Produce the complete `App.tsx` file using `BrowserRouter`, `Routes`, and `Route`
   from `react-router-dom`.
2. Replace `canActivate: [AuthGuard]` with a `<ProtectedRoute>` wrapper component.
3. Replace lazy-loaded modules with `React.lazy()` + `<Suspense>`.
4. Include a `<Navigate>` for the default route redirect.
5. Export `App` as `default`.
6. Do NOT include markdown code fences — output raw TSX only.
"""

    def build_environment_prompt(self, ts_source: str) -> str:
        """Build prompt for migrating Angular environment files to .env files."""
        return f"""Convert the following Angular environment.ts file to the equivalent
React `.env` files and a `src/config/env.ts` helper module.

## Angular Environment File
```typescript
{ts_source}
```

## Output Requirements
Produce THREE sections separated by the comment `// --- FILE: <filename> ---`:

1. `.env.development`  — development environment variables (VITE_ prefix for Vite)
2. `.env.production`   — production environment variables
3. `src/config/env.ts` — TypeScript helper that reads `import.meta.env` values

Do NOT include markdown code fences — output raw text/TypeScript only.
"""

    @staticmethod
    def _to_pascal(name: str) -> str:
        """Convert any case string to PascalCase."""
        return "".join(word.capitalize() for word in name.replace("-", "_").split("_"))
