# Angular → React Migration Agent

An AI-powered agent that automatically migrates Angular application code to modern React (TypeScript) by submitting structured prompts to the **OpenCode** (OpenAI-compatible) API endpoint.

---

## Overview

The agent:

1. **Scans** an Angular project and categorises every source file (components, services, models, guards, routing, environments).
2. **Loads** the comprehensive migration rules from [`ANGULAR_TO_REACT_MIGRATION_RULES.md`](./ANGULAR_TO_REACT_MIGRATION_RULES.md) as context.
3. **Builds** a tailored prompt per file type and submits it to the OpenCode API.
4. **Writes** the generated React code to a structured output directory.
5. **Generates** a migration report documenting what was converted.

---

## Architecture

```
migration-agent/
├── ANGULAR_TO_REACT_MIGRATION_RULES.md   ← 20-section migration rulebook (R-XX-NN)
├── migrate.py                             ← CLI entry point
├── requirements.txt                       ← Python runtime dependencies
├── requirements-dev.txt                   ← Dev/test dependencies
├── .env.example                           ← Environment variable template
│
├── agent/                                 ← Core agent package
│   ├── __init__.py
│   ├── config.py           ← AgentConfig (reads env vars)
│   ├── opencode_client.py  ← OpenCode/OpenAI REST client (httpx, retry logic)
│   ├── prompt_builder.py   ← Builds system + user prompts per file type
│   ├── file_scanner.py     ← Discovers & categorises Angular source files
│   ├── output_writer.py    ← Writes React files + boilerplate scaffolding
│   └── migration_agent.py  ← Orchestrator: scan → prompt → generate → write
│
├── angular-salon-app/      ← Sample Angular source app (Salon Management)
│   └── src/
│       └── app/
│           ├── components/ (Login, Home, ServiceList, Booking, AppointmentList)
│           ├── services/   (AuthService, AppointmentService, SalonService)
│           ├── models/     (User, Appointment, SalonService)
│           ├── guards/     (AuthGuard)
│           └── app-routing.module.ts
│
└── tests/                  ← Pytest test suite
    ├── test_file_scanner.py
    ├── test_prompt_builder.py
    ├── test_output_writer.py
    └── test_opencode_client.py
```

---

## Quick Start

### 1. Install Dependencies

```bash
cd migration-agent
pip install -r requirements.txt
```

### 2. Configure API Key

```bash
cp .env.example .env
# Edit .env and set OPENCODE_API_KEY
```

Or set the environment variable directly:

```bash
export OPENCODE_API_KEY="your-api-key-here"
```

### 3. Run a Dry Run (no API calls)

Preview what the agent will migrate without spending API credits:

```bash
python migrate.py --dry-run
```

Sample output:
```
=== DRY RUN — No API calls will be made ===

Angular Project Scan — /path/to/angular-salon-app
  Components:   5
  Services:     3
  Models:       3
  Guards:       1
  Routing:      yes
  Environments: 2
  Total:        15 logical units

Components:
  LoginComponent: src/app/components/auth/login/login.component.ts, ...
  HomeComponent: ...
  ...
```

### 4. Run the Migration

```bash
python migrate.py \
  --angular-dir ./angular-salon-app \
  --output-dir ./react-salon-app
```

This generates a complete React app at `./react-salon-app/`.

### 5. Use the Generated React App

```bash
cd react-salon-app
npm install
npm run dev        # Start dev server on http://localhost:4200
npm run build      # Production build
npm run lint       # ESLint check
```

---

## CLI Options

```
usage: migrate.py [OPTIONS]

Options:
  --angular-dir   Path to the Angular project (default: ./angular-salon-app)
  --output-dir    Path for the React output (default: ./react-salon-app)
  --api-key       API key (overrides OPENCODE_API_KEY env var)
  --base-url      API base URL (overrides OPENCODE_BASE_URL)
  --model         Model name (overrides OPENCODE_MODEL)
  --no-report     Skip generating MIGRATION_REPORT.md
  --dry-run       Scan and log without calling the API
  --log-level     DEBUG | INFO | WARNING | ERROR (default: INFO)
  --help          Show help message
```

---

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `OPENCODE_API_KEY` | **Yes** | — | Your OpenCode / OpenAI API key |
| `OPENCODE_BASE_URL` | No | `https://api.openai.com/v1` | AI endpoint base URL |
| `OPENCODE_MODEL` | No | `gpt-4o` | Model to use for code generation |
| `MIGRATION_OUTPUT_DIR` | No | `./react-salon-app` | Default output directory |

### Using with OpenCode

Point the agent at the OpenCode endpoint:

```bash
export OPENCODE_API_KEY="your-opencode-api-key"
export OPENCODE_BASE_URL="https://opencode.ai/v1"   # or your OpenCode endpoint
export OPENCODE_MODEL="claude-3-5-sonnet"            # or your preferred model
python migrate.py
```

### Using with Azure OpenAI

```bash
export OPENCODE_API_KEY="your-azure-api-key"
export OPENCODE_BASE_URL="https://YOUR_RESOURCE.openai.azure.com/openai/deployments/YOUR_DEPLOYMENT"
export OPENCODE_MODEL="gpt-4o"
python migrate.py
```

---

## What Gets Migrated

| Angular Artifact | React Output |
|-----------------|--------------|
| `*.component.ts` + `*.component.html` + `*.component.scss` | `src/components/<Name>/<Name>.tsx` |
| `*.service.ts` | `src/services/<name>Service.ts` |
| `*.model.ts` | `src/types/<name>.ts` |
| `*.guard.ts` | `src/components/ProtectedRoute.tsx` |
| `app-routing.module.ts` | `src/App.tsx` |
| `environment.ts` | `.env.development` + `.env.production` + `src/config/env.ts` |

### Boilerplate Generated Automatically (no API required)

| File | Purpose |
|------|---------|
| `src/services/apiClient.ts` | Configured Axios instance with JWT interceptor |
| `src/context/AuthContext.tsx` | Auth state via React Context + `useAuth()` hook |
| `src/components/ProtectedRoute.tsx` | Route guard equivalent |
| `vite.config.ts` | Vite build config with `@` path alias |
| `package.json` | All React dependencies (React 18, React Router v6, etc.) |
| `tsconfig.json` | Strict TypeScript config |
| `src/main.tsx` | App entry point |
| `src/index.css` | Global styles |
| `index.html` | HTML entry point |

---

## Migration Rules

The agent's prompts embed all 20 sections of [`ANGULAR_TO_REACT_MIGRATION_RULES.md`](./ANGULAR_TO_REACT_MIGRATION_RULES.md):

| # | Section | Key Rules |
|---|---------|-----------|
| 1 | Project Structure | R-PS-01 → R-PS-08 |
| 2 | Component Migration | R-CM-01 → R-CM-10 |
| 3 | Templates & JSX | R-TJ-01 → R-TJ-13 |
| 4 | Data Binding | R-DB-01 → R-DB-08 |
| 5 | Lifecycle Hooks | R-LH-01 → R-LH-06 |
| 6 | Services & DI | R-DI-01 → R-DI-07 |
| 7 | Routing | R-RT-01 → R-RT-10 |
| 8 | Forms | R-FM-01 → R-FM-10 |
| 9 | HTTP & API Calls | R-HTTP-01 → R-HTTP-09 |
| 10 | Pipes | R-PP-01 → R-PP-08 |
| 11 | Directives | R-DIR-01 → R-DIR-06 |
| 12 | State Management | R-SM-01 → R-SM-08 |
| 13 | Modules & Lazy Loading | R-ML-01 → R-ML-07 |
| 14 | Guards & Authentication | R-GA-01 → R-GA-06 |
| 15 | Styling | R-ST-01 → R-ST-08 |
| 16 | Observables vs Promises | R-OB-01 → R-OB-06 |
| 17 | TypeScript Interfaces | R-TS-01 → R-TS-07 |
| 18 | Testing | R-TEST-01 → R-TEST-07 |
| 19 | Environment Configuration | R-ENV-01 → R-ENV-04 |
| 20 | Code Quality | R-QC-01 → R-QC-10 |

---

## Running Tests

```bash
pip install -r requirements-dev.txt
pytest tests/ -v
```

To run with coverage:

```bash
pytest tests/ --cov=agent --cov-report=term-missing
```

---

## How the OpenCode API Call Works

For each Angular file, the agent:

1. Builds a **system prompt** embedding the full migration rules document.
2. Builds a **user prompt** containing the Angular source code and specific output requirements.
3. Posts to `POST {OPENCODE_BASE_URL}/chat/completions` with:

```json
{
  "model": "gpt-4o",
  "messages": [
    { "role": "system", "content": "<migration rules + instructions>" },
    { "role": "user", "content": "<angular source code + task>" }
  ],
  "max_tokens": 4096,
  "temperature": 0.1
}
```

4. Extracts the assistant's response text (the generated React code).
5. Writes it to the appropriate file in the React output directory.

---

## Example: Component Migration

**Input** (`login.component.ts` + `login.component.html`):

```typescript
// Angular
@Component({ selector: 'app-login', templateUrl: './login.component.html' })
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  constructor(private fb: FormBuilder, private authService: AuthService) {}
  ngOnInit() {
    this.loginForm = this.fb.group({ email: ['', [Validators.required, Validators.email]] });
  }
  onSubmit() { /* ... */ }
}
```

**Output** (`src/components/Login/Login.tsx`):

```tsx
// React (generated by agent)
import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

interface LoginFormData { email: string; password: string; }

const Login: React.FC = () => {
  const { register, handleSubmit, formState: { errors } } = useForm<LoginFormData>();
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated) navigate('/dashboard');
  }, [isAuthenticated, navigate]);

  const onSubmit = async (data: LoginFormData) => { await login(data); };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input type="email" {...register('email', { required: 'Email required', pattern: { value: /^\S+@\S+$/i, message: 'Invalid email' } })} />
      {errors.email && <span>{errors.email.message}</span>}
      <button type="submit">Sign In</button>
    </form>
  );
};

export default Login;
```

---

## Tech Stack

### Migration Agent (Python)
- Python 3.11+
- `httpx` — async-capable HTTP client for OpenCode API calls
- `python-dotenv` — `.env` file loading
- `pytest` — test framework

### Generated React App
- React 18 + TypeScript
- Vite (build tool)
- React Router v6 (routing)
- react-hook-form (forms)
- Axios (HTTP)
- Zustand (state management)
- date-fns (date utilities)
- clsx (conditional classNames)

---

## License

MIT
