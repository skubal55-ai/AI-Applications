# Angular to React Migration Rules

This document defines the comprehensive ruleset used by the **Angular-to-React Migration Agent** when converting Angular application code into modern React (TypeScript) code.

---

## Table of Contents

1. [Project Structure](#1-project-structure)
2. [Component Migration](#2-component-migration)
3. [Templates & JSX](#3-templates--jsx)
4. [Data Binding](#4-data-binding)
5. [Lifecycle Hooks](#5-lifecycle-hooks)
6. [Services & Dependency Injection](#6-services--dependency-injection)
7. [Routing](#7-routing)
8. [Forms](#8-forms)
9. [HTTP & API Calls](#9-http--api-calls)
10. [Pipes](#10-pipes)
11. [Directives](#11-directives)
12. [State Management](#12-state-management)
13. [Modules & Lazy Loading](#13-modules--lazy-loading)
14. [Guards & Authentication](#14-guards--authentication)
15. [Styling](#15-styling)
16. [Observables vs Promises](#16-observables-vs-promises)
17. [TypeScript Interfaces & Models](#17-typescript-interfaces--models)
18. [Testing](#18-testing)
19. [Environment Configuration](#19-environment-configuration)
20. [Code Quality & Conventions](#20-code-quality--conventions)

---

## 1. Project Structure

### Angular Structure
```
src/
  app/
    components/
    services/
    models/
    guards/
    pipes/
    modules/
  assets/
  environments/
```

### React Equivalent Structure
```
src/
  components/
  pages/
  hooks/
  services/
  types/
  context/
  utils/
  assets/
  config/
public/
```

### Rules
- **R-PS-01**: Angular `app/` maps to React `src/`
- **R-PS-02**: Angular `components/` retains same name in React
- **R-PS-03**: Angular `services/` maps to React `services/` (plain TS files, no `Injectable`)
- **R-PS-04**: Angular `models/` maps to React `types/` (interfaces only)
- **R-PS-05**: Angular `guards/` maps to React route-level components or custom hooks in `hooks/`
- **R-PS-06**: Angular `pipes/` maps to React `utils/` (pure utility functions)
- **R-PS-07**: Angular `modules/` → removed; React uses component trees and `context/`
- **R-PS-08**: Each Angular feature module becomes a folder under `pages/` or `components/`

---

## 2. Component Migration

### Angular Component
```typescript
// user-card.component.ts
import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { User } from '../models/user';

@Component({
  selector: 'app-user-card',
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.scss']
})
export class UserCardComponent implements OnInit {
  @Input() user: User;
  @Output() selected = new EventEmitter<User>();

  ngOnInit(): void {
    console.log('Component initialized');
  }

  onSelect(): void {
    this.selected.emit(this.user);
  }
}
```

### React Equivalent
```typescript
// UserCard.tsx
import React, { useEffect } from 'react';
import { User } from '../types/user';

interface UserCardProps {
  user: User;
  onSelected: (user: User) => void;
}

const UserCard: React.FC<UserCardProps> = ({ user, onSelected }) => {
  useEffect(() => {
    console.log('Component initialized');
  }, []);

  return (
    <div className="user-card" onClick={() => onSelected(user)}>
      {/* template content */}
    </div>
  );
};

export default UserCard;
```

### Rules
- **R-CM-01**: Angular `@Component` decorator → React functional component with explicit `interface Props`
- **R-CM-02**: Angular `@Input()` properties → destructured props in function signature
- **R-CM-03**: Angular `@Output() EventEmitter` → callback props prefixed with `on` (e.g., `onSelected`)
- **R-CM-04**: Component selectors (kebab-case) → PascalCase JSX element names
- **R-CM-05**: Separate `.html` and `.ts` files → single `.tsx` file
- **R-CM-06**: Separate `.scss` file → CSS Module (`.module.scss`) imported as `styles` or inline Tailwind classes
- **R-CM-07**: Class-based component state → `useState` hook
- **R-CM-08**: `this.property` references → direct variable references
- **R-CM-09**: Constructor injection → top-level service imports or custom hooks
- **R-CM-10**: Always export components as `default` unless named exports are intentional

---

## 3. Templates & JSX

### Angular Template
```html
<!-- user-list.component.html -->
<div class="container">
  <h1>{{ title }}</h1>
  <app-user-card
    *ngFor="let user of users; trackBy: trackById"
    [user]="user"
    (selected)="onUserSelected($event)">
  </app-user-card>
  <p *ngIf="users.length === 0">No users found</p>
  <ng-container *ngIf="isLoading; else content">
    <app-spinner></app-spinner>
  </ng-container>
  <ng-template #content>
    <div>Content here</div>
  </ng-template>
</div>
```

### React Equivalent
```tsx
// UserList.tsx
return (
  <div className="container">
    <h1>{title}</h1>
    {users.map((user) => (
      <UserCard
        key={user.id}
        user={user}
        onSelected={onUserSelected}
      />
    ))}
    {users.length === 0 && <p>No users found</p>}
    {isLoading ? <Spinner /> : <div>Content here</div>}
  </div>
);
```

### Rules
- **R-TJ-01**: Angular `{{ expression }}` interpolation → React `{expression}` in JSX
- **R-TJ-02**: `*ngFor="let item of items"` → `.map((item) => ...)` with `key` prop using unique ID
- **R-TJ-03**: `*ngFor trackBy` → `key` prop in React (use stable ID, not array index unless no ID available)
- **R-TJ-04**: `*ngIf="condition"` → `{condition && <Component />}` or ternary `{condition ? <A /> : <B />}`
- **R-TJ-05**: `*ngIf="cond; else tmpl"` + `<ng-template>` → ternary expression in JSX
- **R-TJ-06**: `*ngSwitch` / `*ngSwitchCase` → JavaScript `switch` in a render helper function or JSX ternaries
- **R-TJ-07**: `[class]` and `[ngClass]` → `className` with template literals or `clsx`/`classnames` library
- **R-TJ-08**: `[style]` and `[ngStyle]` → `style={{ property: value }}` (camelCase properties)
- **R-TJ-09**: `<ng-container>` → React Fragment `<>...</>` or `<React.Fragment>`
- **R-TJ-10**: `<ng-template>` → extracted component or render props pattern
- **R-TJ-11**: `<ng-content>` (content projection) → `{children}` prop or named slot props
- **R-TJ-12**: HTML attributes: `class` → `className`, `for` → `htmlFor`, `tabindex` → `tabIndex`
- **R-TJ-13**: Self-closing tags are required in JSX: `<input />`, `<br />`, `<img />`

---

## 4. Data Binding

### Angular Binding Types
```html
<!-- Property binding -->
<input [value]="name" />
<!-- Event binding -->
<button (click)="onClick()">Click</button>
<!-- Two-way binding -->
<input [(ngModel)]="name" />
<!-- Attribute binding -->
<td [attr.colspan]="numCols">...</td>
```

### React Equivalent
```tsx
{/* Property binding */}
<input value={name} onChange={(e) => setName(e.target.value)} />
{/* Event binding */}
<button onClick={onClick}>Click</button>
{/* Two-way binding (controlled input) */}
<input value={name} onChange={(e) => setName(e.target.value)} />
{/* Attribute binding */}
<td colSpan={numCols}>...</td>
```

### Rules
- **R-DB-01**: `[property]="expr"` → `property={expr}` (JSX attribute)
- **R-DB-02**: `(event)="handler($event)"` → `onEvent={handler}` or `onEvent={(e) => handler(e)}`
- **R-DB-03**: `[(ngModel)]="field"` → controlled input: `value={field}` + `onChange={(e) => setField(e.target.value)}`
- **R-DB-04**: `[attr.name]` → camelCase attribute directly, e.g., `colSpan`, `rowSpan`, `aria-label`
- **R-DB-05**: `(keyup.enter)="handler()"` → `onKeyDown={(e) => e.key === 'Enter' && handler()}`
- **R-DB-06**: `(submit)` on forms → `onSubmit={(e) => { e.preventDefault(); handler(); }}`
- **R-DB-07**: `[disabled]="condition"` → `disabled={condition}` (boolean)
- **R-DB-08**: `[hidden]="condition"` → `{!condition && <Element />}` or `style={{ display: condition ? 'none' : undefined }}`

---

## 5. Lifecycle Hooks

### Angular Hooks → React Equivalents

| Angular Hook | React Equivalent |
|---|---|
| `ngOnInit()` | `useEffect(() => { ... }, [])` |
| `ngOnDestroy()` | Cleanup function returned from `useEffect` |
| `ngOnChanges(changes)` | `useEffect(() => { ... }, [dep1, dep2])` |
| `ngAfterViewInit()` | `useEffect(() => { ... }, [])` with `ref` |
| `ngAfterContentInit()` | `useEffect(() => { ... }, [])` |
| `ngDoCheck()` | Custom comparison in `useEffect` or `useMemo` |
| `ngAfterViewChecked()` | `useEffect` (runs after every render by default) |
| Constructor initialization | Inline variable initialization or `useMemo` |

### Rules
- **R-LH-01**: `ngOnInit` → `useEffect` with empty dependency array `[]`
- **R-LH-02**: `ngOnDestroy` → `return () => { cleanup }` inside `useEffect`
- **R-LH-03**: `ngOnChanges` watching specific inputs → `useEffect` with those values in the dependency array
- **R-LH-04**: `ngAfterViewInit` → `useEffect` with `[]` and `useRef` for DOM access
- **R-LH-05**: Computed values in `ngOnChanges` → `useMemo` or derived state
- **R-LH-06**: Do not call hooks conditionally; respect the [Rules of Hooks](https://react.dev/reference/rules/rules-of-hooks)

---

## 6. Services & Dependency Injection

### Angular Service
```typescript
// user.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user';

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiUrl = '/api/users';

  constructor(private http: HttpClient) {}

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  createUser(user: User): Observable<User> {
    return this.http.post<User>(this.apiUrl, user);
  }
}
```

### React Equivalent
```typescript
// services/userService.ts
import apiClient from './apiClient';
import { User } from '../types/user';

const API_URL = '/api/users';

export const userService = {
  getUsers: (): Promise<User[]> =>
    apiClient.get<User[]>(API_URL).then((res) => res.data),

  getUserById: (id: number): Promise<User> =>
    apiClient.get<User>(`${API_URL}/${id}`).then((res) => res.data),

  createUser: (user: Partial<User>): Promise<User> =>
    apiClient.post<User>(API_URL, user).then((res) => res.data),
};
```

### Rules
- **R-DI-01**: `@Injectable({ providedIn: 'root' })` → plain exported object or class (singleton via module scope)
- **R-DI-02**: Constructor injection → direct import at the top of the service or component file
- **R-DI-03**: `HttpClient` → `axios` instance (configured `apiClient`) or `fetch`
- **R-DI-04**: Observable-returning methods → Promise-returning methods (use `async/await`)
- **R-DI-05**: Services that manage shared state → convert to React Context + Provider
- **R-DI-06**: Services used only within one component → convert to a custom hook `useXxx`
- **R-DI-07**: Interceptors (`HTTP_INTERCEPTORS`) → Axios request/response interceptors in `apiClient.ts`

---

## 7. Routing

### Angular Routing
```typescript
// app-routing.module.ts
const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'users/:id', component: UserDetailComponent },
  {
    path: 'admin',
    canActivate: [AuthGuard],
    loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule)
  },
  { path: '**', component: NotFoundComponent }
];
```

### React Equivalent (React Router v6)
```tsx
// App.tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/home" replace />} />
        <Route path="/home" element={<Home />} />
        <Route path="/users/:id" element={<UserDetail />} />
        <Route path="/admin" element={<ProtectedRoute><Admin /></ProtectedRoute>} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  );
}
```

### Rules
- **R-RT-01**: `RouterModule.forRoot(routes)` → `<BrowserRouter>` wrapping `<Routes>` in `App.tsx`
- **R-RT-02**: `{ path: 'x', component: C }` → `<Route path="/x" element={<C />} />`
- **R-RT-03**: `{ path: '', redirectTo: '/home' }` → `<Route path="/" element={<Navigate to="/home" replace />} />`
- **R-RT-04**: `{ path: '**', component: NotFound }` → `<Route path="*" element={<NotFound />} />`
- **R-RT-05**: Route params (`:id`) are accessed via `useParams()` hook instead of `ActivatedRoute`
- **R-RT-06**: `canActivate: [AuthGuard]` → `<ProtectedRoute>` wrapper component checking auth state
- **R-RT-07**: Lazy loading (`loadChildren`) → `React.lazy()` + `<Suspense fallback={<Loader />}>`
- **R-RT-08**: `routerLink` directive → `<Link to="/path">` or `<NavLink to="/path">`
- **R-RT-09**: `this.router.navigate(['/path'])` → `useNavigate()` hook: `const navigate = useNavigate(); navigate('/path')`
- **R-RT-10**: `queryParams` → `useSearchParams()` hook

---

## 8. Forms

### Angular Reactive Form
```typescript
// login.component.ts
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

export class LoginComponent {
  loginForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      const { email, password } = this.loginForm.value;
      // submit logic
    }
  }
}
```

### React Equivalent (React Hook Form)
```tsx
// Login.tsx
import { useForm } from 'react-hook-form';

interface LoginFormData {
  email: string;
  password: string;
}

const Login: React.FC = () => {
  const { register, handleSubmit, formState: { errors } } = useForm<LoginFormData>();

  const onSubmit = (data: LoginFormData) => {
    // submit logic
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input
        type="email"
        {...register('email', {
          required: 'Email is required',
          pattern: { value: /^\S+@\S+$/i, message: 'Invalid email' }
        })}
      />
      {errors.email && <span>{errors.email.message}</span>}
      <input
        type="password"
        {...register('password', {
          required: 'Password is required',
          minLength: { value: 6, message: 'Minimum 6 characters' }
        })}
      />
      {errors.password && <span>{errors.password.message}</span>}
      <button type="submit">Login</button>
    </form>
  );
};
```

### Rules
- **R-FM-01**: `ReactiveFormsModule` / `FormBuilder` → `react-hook-form` (`useForm`)
- **R-FM-02**: `FormsModule` / `ngModel` → controlled inputs with `useState`
- **R-FM-03**: `FormGroup` → `useForm<FormDataInterface>()`
- **R-FM-04**: `Validators.required` → `{ required: 'Field is required' }` in `register` options
- **R-FM-05**: `Validators.email` → regex pattern in `register` options
- **R-FM-06**: `Validators.minLength(n)` → `{ minLength: { value: n, message: '...' } }`
- **R-FM-07**: `form.get('field').errors` → `formState.errors.field`
- **R-FM-08**: `form.valid` → `formState.isValid`
- **R-FM-09**: `formGroup.patchValue(data)` → `reset(data)` or `setValue('field', value)`
- **R-FM-10**: Form arrays (`FormArray`) → `useFieldArray` from react-hook-form

---

## 9. HTTP & API Calls

### Angular HTTP
```typescript
// In component
this.userService.getUsers()
  .pipe(
    catchError(err => { this.error = err.message; return EMPTY; }),
    finalize(() => this.loading = false)
  )
  .subscribe(users => this.users = users);
```

### React Equivalent
```tsx
// Using custom hook
const useUsers = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    userService.getUsers()
      .then(setUsers)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  return { users, loading, error };
};
```

### Rules
- **R-HTTP-01**: `HttpClient.get/post/put/delete` → `axios.get/post/put/delete` or `fetch`
- **R-HTTP-02**: RxJS `.pipe(map(...))` → `.then((res) => res.data)` in Promise chain
- **R-HTTP-03**: RxJS `catchError` → `.catch((err) => ...)` or `try/catch` in `async/await`
- **R-HTTP-04**: RxJS `finalize` → `.finally(() => setLoading(false))`
- **R-HTTP-05**: HTTP calls in `ngOnInit` → `useEffect` with empty dependency array
- **R-HTTP-06**: Repeated HTTP patterns → extract into custom hooks (`useUsers`, `useAppointments`, etc.)
- **R-HTTP-07**: HTTP headers → set in Axios instance interceptors (`apiClient.ts`)
- **R-HTTP-08**: Authorization token → set in Axios interceptor from `localStorage` or Context
- **R-HTTP-09**: Use `TanStack Query` (`react-query`) for data-fetching with caching, refetching, and loading states (preferred over raw hooks for CRUD-heavy pages)

---

## 10. Pipes

### Angular Pipes
```html
{{ price | currency:'USD' }}
{{ date | date:'dd/MM/yyyy' }}
{{ name | uppercase }}
{{ text | slice:0:100 }}
{{ data | json }}
{{ value | async }}
```

### React Equivalents (utility functions)
```tsx
// utils/formatters.ts
export const formatCurrency = (value: number, currency = 'USD') =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(value);

export const formatDate = (date: Date | string, locale = 'en-US') =>
  new Intl.DateTimeFormat(locale, { day: '2-digit', month: '2-digit', year: 'numeric' }).format(new Date(date));

// In JSX
{formatCurrency(price)}
{formatDate(date)}
{name.toUpperCase()}
{text.slice(0, 100)}
{JSON.stringify(data, null, 2)}
// async pipe → resolved via useState/useEffect or react-query
```

### Rules
- **R-PP-01**: Each Angular pipe → a pure utility function in `utils/formatters.ts` or `utils/helpers.ts`
- **R-PP-02**: `currency` pipe → `Intl.NumberFormat` with `style: 'currency'`
- **R-PP-03**: `date` pipe → `Intl.DateTimeFormat` or `date-fns` library function
- **R-PP-04**: `uppercase`/`lowercase` → `.toUpperCase()`/`.toLowerCase()` inline
- **R-PP-05**: `slice` pipe → `Array.slice()` or `String.slice()` inline
- **R-PP-06**: `json` pipe (debugging) → `JSON.stringify(value, null, 2)` inside `<pre>` tag
- **R-PP-07**: `async` pipe → `useState` + `useEffect` or `react-query`'s `useQuery`
- **R-PP-08**: Custom pipes → pure utility functions exported from `utils/`

---

## 11. Directives

### Angular Directives → React Patterns

| Angular | React Equivalent |
|---|---|
| `*ngFor` | `.map()` with `key` prop |
| `*ngIf` | Conditional rendering (`&&` or ternary) |
| `*ngSwitch` | `switch` statement in render function |
| `[ngClass]` | `className` with `clsx` or template literals |
| `[ngStyle]` | `style={{ ... }}` object |
| `ngModel` | Controlled input with `value` + `onChange` |
| Custom attribute directive | Custom hook or higher-order component (HOC) |
| Custom structural directive | Render props or custom component |

### Rules
- **R-DIR-01**: Structural directives (`*ng...`) → JavaScript expressions in JSX
- **R-DIR-02**: Attribute directives that modify DOM → `useRef` + `useEffect` or styled-components
- **R-DIR-03**: Custom directives that add behavior → custom hooks
- **R-DIR-04**: `HostListener` → event handlers on the JSX element directly
- **R-DIR-05**: `HostBinding` → `className`, `style`, or other props on the JSX element
- **R-DIR-06**: `Renderer2` DOM manipulation → direct DOM via `useRef` or React state

---

## 12. State Management

### Angular (NgRx) → React (Zustand or Context)

| NgRx Concept | React Equivalent |
|---|---|
| `Store` | Zustand store or React Context |
| `Action` | Zustand action function |
| `Reducer` | Zustand state update function |
| `Effect` | Custom hook with API call + store update |
| `Selector` | Computed value or Zustand slice selector |

### Rules
- **R-SM-01**: Simple local state → `useState`
- **R-SM-02**: Derived/computed state → `useMemo`
- **R-SM-03**: Shared state across a feature → React Context + `useReducer`
- **R-SM-04**: Global app state (auth, user, theme) → Context API or Zustand
- **R-SM-05**: Complex server state (CRUD) → TanStack Query (`react-query`)
- **R-SM-06**: NgRx Store → Zustand (preferred for simplicity) or Redux Toolkit
- **R-SM-07**: NgRx Effects → custom hooks combining `useEffect` + service calls
- **R-SM-08**: BehaviorSubject in services → Zustand store slice or Context value

---

## 13. Modules & Lazy Loading

### Angular Module
```typescript
@NgModule({
  declarations: [HomeComponent, HeaderComponent],
  imports: [CommonModule, RouterModule],
  exports: [HeaderComponent]
})
export class HomeModule {}
```

### React Equivalent
```tsx
// No module file needed
// Lazy loading in React Router
const Home = React.lazy(() => import('./pages/Home'));
const Header = React.lazy(() => import('./components/Header'));
```

### Rules
- **R-ML-01**: `NgModule` → no equivalent; React has no module system at the framework level
- **R-ML-02**: `declarations` → components are simply imported where used
- **R-ML-03**: `imports` (Angular modules) → npm package imports at the top of files
- **R-ML-04**: `exports` → standard ES6 `export`
- **R-ML-05**: `providers` in module → Context Providers in component tree
- **R-ML-06**: Lazy-loaded modules → `React.lazy(() => import('./pages/FeaturePage'))` + `<Suspense>`
- **R-ML-07**: `SharedModule` → shared components exported individually and imported directly

---

## 14. Guards & Authentication

### Angular Guard
```typescript
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isLoggedIn()) {
      return true;
    }
    this.router.navigate(['/login']);
    return false;
  }
}
```

### React Equivalent
```tsx
// components/ProtectedRoute.tsx
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
};
```

### Rules
- **R-GA-01**: `CanActivate` guard → `<ProtectedRoute>` wrapper component
- **R-GA-02**: Auth state → React Context (`AuthContext`) with `useAuth` custom hook
- **R-GA-03**: `CanDeactivate` guard → `useBeforeUnload` or custom prompt logic
- **R-GA-04**: Role-based access → extend `ProtectedRoute` to accept `requiredRole` prop
- **R-GA-05**: JWT token storage → `localStorage` (same as Angular); accessed via `authService.ts`
- **R-GA-06**: Token refresh logic → Axios interceptor in `apiClient.ts`

---

## 15. Styling

### Rules
- **R-ST-01**: Global `styles.scss` → `src/index.css` or `src/styles/global.css`
- **R-ST-02**: Component `.scss` files → CSS Modules: `Component.module.scss`, imported as `import styles from './Component.module.scss'`
- **R-ST-03**: `[ngClass]` → `className={clsx({ [styles.active]: isActive })}`
- **R-ST-04**: CSS variables defined in Angular → same variables in `:root {}` in `global.css`
- **R-ST-05**: Angular Material → MUI (`@mui/material`) with equivalent component mapping
- **R-ST-06**: Bootstrap (Angular) → Bootstrap (React) via `react-bootstrap` or plain CSS classes
- **R-ST-07**: SCSS `@import` → same SCSS syntax works in React with `sass` package
- **R-ST-08**: Tailwind CSS is an acceptable alternative to component-scoped CSS

---

## 16. Observables vs Promises

### RxJS → Async/Await or React Query

| RxJS Operator | JS/React Equivalent |
|---|---|
| `map` | `.then(data => transform(data))` |
| `filter` | `.then(data => data.filter(...))` |
| `switchMap` | `await` in sequence; abort previous with `AbortController` |
| `mergeMap` | `Promise.all()` |
| `catchError` | `.catch()` or `try/catch` |
| `finalize` | `.finally()` |
| `tap` | Side-effect in `.then()` |
| `debounceTime` | `useDebounce` custom hook |
| `distinctUntilChanged` | Compare previous value in `useEffect` |
| `combineLatest` | `Promise.all()` |
| `forkJoin` | `Promise.all()` |
| `Subject` | `useState` setter or Zustand action |
| `BehaviorSubject` | Zustand store or Context value |
| `takeUntil(destroy$)` | `useEffect` cleanup / AbortController |

### Rules
- **R-OB-01**: All RxJS Observables → Promises (async/await) unless using react-query
- **R-OB-02**: `subscribe()` calls → `await` or `.then()` chains
- **R-OB-03**: `pipe(operator1, operator2)` → chained `.then()` or composed async functions
- **R-OB-04**: `Subject` / `BehaviorSubject` for event bus → Zustand store or Context
- **R-OB-05**: `takeUntil(this.destroy$)` pattern → `AbortController` in `useEffect` cleanup
- **R-OB-06**: `debounceTime` + `distinctUntilChanged` for search inputs → `useDebounce` hook

---

## 17. TypeScript Interfaces & Models

### Rules
- **R-TS-01**: Angular `models/user.ts` → React `types/user.ts`
- **R-TS-02**: `export interface User { ... }` → same syntax; no changes needed
- **R-TS-03**: Remove Angular-specific decorators (`@Column`, `@Entity` if any leaked to frontend)
- **R-TS-04**: `enum` types → TypeScript `enum` or `const` object with `as const`
- **R-TS-05**: Use strict TypeScript: `"strict": true` in `tsconfig.json`
- **R-TS-06**: Generic API response types: `ApiResponse<T>` pattern preserved
- **R-TS-07**: Do not use `any`; prefer `unknown` and type guards

---

## 18. Testing

### Angular → React Testing

| Angular | React Equivalent |
|---|---|
| `TestBed` | N/A (no equivalent needed) |
| `ComponentFixture` | `render()` from `@testing-library/react` |
| `detectChanges()` | Not needed; DOM updates are synchronous in RTL |
| `async`/`fakeAsync` | `act()` or `waitFor()` |
| Jasmine matchers | Jest matchers |
| `spyOn` | `jest.spyOn()` or `jest.fn()` |

### Rules
- **R-TEST-01**: Testing framework → Jest + React Testing Library (`@testing-library/react`)
- **R-TEST-02**: `TestBed.configureTestingModule` → not needed; use `render()` from RTL
- **R-TEST-03**: `fixture.debugElement.query(By.css(...))` → `screen.getByRole(...)` or `screen.getByText(...)`
- **R-TEST-04**: `fixture.detectChanges()` → not needed
- **R-TEST-05**: Jasmine spies → `jest.fn()` and `jest.spyOn()`
- **R-TEST-06**: Service mocks → `jest.mock('../services/userService')`
- **R-TEST-07**: Test file naming: `Component.test.tsx` or `Component.spec.tsx`

---

## 19. Environment Configuration

### Angular
```typescript
// environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

### React Equivalent
```
# .env.development
REACT_APP_API_URL=http://localhost:8080/api

# Access in code
const API_URL = process.env.REACT_APP_API_URL;
```

### Rules
- **R-ENV-01**: `environment.ts` → `.env` files (`.env.development`, `.env.production`)
- **R-ENV-02**: Vite projects use `VITE_` prefix: `VITE_API_URL`; CRA uses `REACT_APP_` prefix
- **R-ENV-03**: `environment.production` flag → `import.meta.env.MODE === 'production'` (Vite) or `process.env.NODE_ENV === 'production'` (CRA)
- **R-ENV-04**: Never commit `.env` files with secrets; always have `.env.example`

---

## 20. Code Quality & Conventions

### Rules
- **R-QC-01**: Use Vite (`npm create vite@latest -- --template react-ts`) as the build tool (preferred over CRA)
- **R-QC-02**: Enable ESLint with `@typescript-eslint` and `eslint-plugin-react-hooks`
- **R-QC-03**: Use Prettier for code formatting
- **R-QC-04**: File naming: PascalCase for components (`UserCard.tsx`), camelCase for hooks (`useUsers.ts`) and utilities (`formatDate.ts`)
- **R-QC-05**: One component per file
- **R-QC-06**: Custom hooks must start with `use` prefix
- **R-QC-07**: Avoid `useEffect` for derived state; use `useMemo` or compute inline instead
- **R-QC-08**: Prefer named exports for utilities/hooks; default exports for page/feature components
- **R-QC-09**: Keep components small and focused; extract sub-components when JSX exceeds ~80 lines
- **R-QC-10**: Use absolute imports configured via `tsconfig.json` `paths` (e.g., `@/components/...`)

---

## Migration Checklist

Use this checklist when migrating each Angular component/feature:

- [ ] Identify all `@Input()` / `@Output()` → convert to props
- [ ] Map template syntax (`*ngFor`, `*ngIf`, etc.) → JSX expressions
- [ ] Replace `ngModel` with controlled inputs
- [ ] Replace `Observable` methods → `Promise` / `async-await`
- [ ] Remove `@Injectable` and `@NgModule` decorators
- [ ] Replace RxJS operators with Promise equivalents
- [ ] Move lifecycle logic to `useEffect`
- [ ] Replace Router service with `useNavigate` / `useParams`
- [ ] Replace pipes with utility functions
- [ ] Replace Angular Material components with MUI equivalents
- [ ] Move environment variables to `.env` files
- [ ] Write/update tests with Jest + React Testing Library
- [ ] Verify TypeScript types are preserved and `strict` mode passes

---

*Last updated: 2026-03-31 | Version 1.0*
