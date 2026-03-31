# Angular to React Migration Agent

This directory contains a standalone migration agent that reads markdown-based migration rules, builds a conversion prompt, and sends it to OpenCode over HTTP.

## Files

- `angular_to_react_agent.py` - CLI agent implementation
- `angular_to_react_rules.md` - migration rules injected into every prompt
- `tests/test_angular_to_react_agent.py` - lightweight integration-style test with a fake OpenCode server

## Supported OpenCode Flow

The agent uses the documented OpenCode server session/message flow:

1. `GET /global/health` to verify the server is reachable
2. `POST /session` to create a session
3. `POST /session/{id}/message` to submit the migration prompt

The default base URL is `http://127.0.0.1:4096`, or you can override it with `OPENCODE_BASE_URL`.

## Usage

### Convert one Angular file

```bash
python3 agents/angular_to_react_agent.py \
  --source-file path/to/component.ts \
  --source-file path/to/component.html
```

### Convert inline code

```bash
python3 agents/angular_to_react_agent.py \
  --source-text "@Component({...}) export class ExampleComponent {}"
```

### Convert piped input

```bash
cat path/to/component.ts | python3 agents/angular_to_react_agent.py
```

### Customize OpenCode target

```bash
python3 agents/angular_to_react_agent.py \
  --source-file path/to/component.ts \
  --base-url http://127.0.0.1:4096 \
  --provider-id anthropic \
  --model-id claude-3-5-sonnet-20241022
```

### Save result to a file

```bash
python3 agents/angular_to_react_agent.py \
  --source-file path/to/component.ts \
  --output-file converted-component.tsx
```

## Authentication

If your OpenCode server is protected with basic auth, pass credentials explicitly:

```bash
python3 agents/angular_to_react_agent.py \
  --source-file path/to/component.ts \
  --username opencode \
  --password your-password
```

You can also rely on environment variables:

- `OPENCODE_BASE_URL`
- `OPENCODE_SERVER_USERNAME`
- `OPENCODE_SERVER_PASSWORD`

## Notes

- The agent is intentionally dependency-free and uses the Python standard library only.
- The markdown rules file can be expanded with project-specific Angular to React conventions without modifying the Python code.
