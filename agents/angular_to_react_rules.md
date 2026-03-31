# Angular to React Migration Rules

Use these rules whenever converting Angular source code into React code.

## Primary Objective

Produce idiomatic, production-ready React code that preserves the Angular feature behavior while removing Angular-specific constructs.

## General Requirements

1. Keep the same business behavior, validations, and user-visible strings unless the source is clearly broken.
2. Prefer function components and React hooks over class components.
3. Use TypeScript-compatible React patterns when source types are available.
4. Return complete React code, not partial snippets, unless the request explicitly asks for a fragment.
5. Preserve accessibility attributes and improve semantics where Angular templates were vague.
6. Do not include Angular imports, decorators, modules, dependency injection, or template syntax in the final output.

## Angular to React Mapping

### Components

- Convert `@Component` classes into React function components.
- Convert Angular `@Input()` values into React props.
- Convert Angular `@Output()` event emitters into callback props.
- Replace Angular lifecycle hooks:
  - `ngOnInit` -> `useEffect(..., [])`
  - `ngOnChanges` -> `useEffect` with relevant dependencies
  - `ngOnDestroy` -> effect cleanup functions

### Templates

- Convert `*ngIf` into conditional rendering.
- Convert `*ngFor` into `Array.map`.
- Convert `[class.foo]`, `[style.bar]`, and `[ngClass]` into standard React `className` / `style` expressions.
- Convert `[value]`, `[checked]`, and property bindings into JSX props.
- Convert `(click)`, `(change)`, and event bindings into React event handlers like `onClick`, `onChange`.
- Convert `[(ngModel)]` into controlled React state with `useState`.
- Convert pipes into plain helper functions or memoized derived values.

### Services and Dependency Injection

- Replace Angular dependency injection with explicit imports, hooks, context, or function parameters.
- Keep service calls asynchronous and return promises where appropriate.
- If an Angular service acts like shared state, prefer React context or a dedicated custom hook.

### Routing

- Replace Angular Router concepts with React Router style patterns.
- Convert `routerLink` to `Link` or `useNavigate`.
- Convert route params access into React Router hooks such as `useParams`.

### Forms

- Convert template-driven forms into controlled components.
- Convert reactive forms into React state plus validation helpers.
- Preserve validation logic and error messaging.

### RxJS and Async

- Replace simple observable subscriptions with `useEffect` plus async functions.
- If the Angular code relies heavily on streams, produce a practical React equivalent and explain any assumptions in comments only when necessary.
- Ensure subscriptions and listeners are cleaned up.

### Styling

- Keep CSS class names where possible so existing styles remain reusable.
- Use `className` instead of `class`.
- Keep inline styles only when needed.

## Output Format

1. Start with a brief summary of what was converted.
2. Provide the converted React code in fenced code blocks.
3. If multiple source files are needed, clearly label each file.
4. Mention any migration assumptions after the code in a short bullet list.
