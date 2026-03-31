"""
Output writer for the Angular-to-React Migration Agent.

Writes migrated React files to the output directory, maintaining a structured
React project layout. Also generates an Axios API client and auth context boilerplate.
"""

import logging
from pathlib import Path
from typing import Optional

logger = logging.getLogger(__name__)

# ─────────────────────────────────────────────────────────────────────────────
# Boilerplate files that are always written to the React project
# ─────────────────────────────────────────────────────────────────────────────

_AXIOS_CLIENT = """\
/**
 * Configured Axios instance for the Salon React App.
 * Sets the base URL from environment variables and attaches
 * the JWT auth token to every request automatically.
 */
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT token from localStorage before every request
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('salon_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 globally: clear token and redirect to login
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('salon_token');
      localStorage.removeItem('salon_user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
"""

_AUTH_CONTEXT = """\
/**
 * Authentication context for the Salon React App.
 * Provides isAuthenticated state and login/logout helpers
 * to all components via the useAuth() hook.
 */
import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import { User, AuthRequest, RegisterRequest } from '../types/user';
import { authService } from '../services/authService';

interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: AuthRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const stored = localStorage.getItem('salon_user');
    return stored ? JSON.parse(stored) : null;
  });
  const [isLoading, setIsLoading] = useState(false);

  const login = useCallback(async (credentials: AuthRequest) => {
    setIsLoading(true);
    try {
      const response = await authService.login(credentials);
      localStorage.setItem('salon_token', response.token);
      localStorage.setItem('salon_user', JSON.stringify(response.user));
      setUser(response.user);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const register = useCallback(async (data: RegisterRequest) => {
    setIsLoading(true);
    try {
      const response = await authService.register(data);
      localStorage.setItem('salon_token', response.token);
      localStorage.setItem('salon_user', JSON.stringify(response.user));
      setUser(response.user);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('salon_token');
    localStorage.removeItem('salon_user');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextValue => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
};
"""

_PROTECTED_ROUTE = """\
/**
 * ProtectedRoute wraps any route that requires authentication.
 * Migrated from Angular's AuthGuard (CanActivate).
 *
 * Migration rule: R-GA-01
 */
import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: 'CUSTOMER' | 'STAFF' | 'ADMIN';
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRole }) => {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && user?.role !== requiredRole) {
    return <Navigate to="/home" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
"""

_VITE_CONFIG = """\
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 4200,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
"""

_PACKAGE_JSON = """\
{
  "name": "salon-react-app",
  "version": "1.0.0",
  "description": "Salon Management React App — migrated from Angular by the Migration Agent",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "lint": "eslint src --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
    "test": "vitest"
  },
  "dependencies": {
    "axios": "^1.6.0",
    "clsx": "^2.1.0",
    "date-fns": "^3.0.0",
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-hook-form": "^7.49.0",
    "react-router-dom": "^6.21.0",
    "zustand": "^4.4.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "@typescript-eslint/eslint-plugin": "^6.14.0",
    "@typescript-eslint/parser": "^6.14.0",
    "@vitejs/plugin-react": "^4.2.0",
    "eslint": "^8.55.0",
    "eslint-plugin-react-hooks": "^4.6.0",
    "typescript": "^5.3.0",
    "vite": "^5.0.0",
    "vitest": "^1.0.0"
  }
}
"""

_TSCONFIG = """\
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
"""

_TSCONFIG_NODE = """\
{
  "compilerOptions": {
    "composite": true,
    "skipLibCheck": true,
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true
  },
  "include": ["vite.config.ts"]
}
"""

_ENV_DEVELOPMENT = """\
VITE_API_URL=http://localhost:8080/api
VITE_APP_NAME=Salon App
"""

_ENV_PRODUCTION = """\
VITE_API_URL=https://api.salonapp.com/api
VITE_APP_NAME=Salon App
"""

_MAIN_TSX = """\
import React from 'react';
import ReactDOM from 'react-dom/client';
import { AuthProvider } from './context/AuthContext';
import App from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </React.StrictMode>
);
"""

_INDEX_CSS = """\
/* Global styles — migrated from Angular's styles.scss */
:root {
  --primary: #6c3fc5;
  --primary-dark: #5a32a3;
  --secondary: #f5a623;
  --danger: #e53935;
  --success: #43a047;
  --text: #212121;
  --text-muted: #757575;
  --bg: #fafafa;
  --card-bg: #ffffff;
  --border: #e0e0e0;
  --radius: 8px;
  --shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

*,
*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, sans-serif;
  background: var(--bg);
  color: var(--text);
  line-height: 1.6;
}

a {
  color: var(--primary);
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}

button {
  cursor: pointer;
  font-family: inherit;
}

.btn-primary {
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  padding: 10px 20px;
  font-size: 1rem;
  transition: background 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background: var(--primary-dark);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: transparent;
  color: var(--primary);
  border: 1px solid var(--primary);
  border-radius: var(--radius);
  padding: 10px 20px;
  font-size: 1rem;
}

.btn-danger {
  background: var(--danger);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  padding: 8px 16px;
}

.error-alert {
  background: #ffebee;
  color: var(--danger);
  border: 1px solid #ffcdd2;
  border-radius: var(--radius);
  padding: 12px 16px;
  margin-bottom: 16px;
}

.success-alert {
  background: #e8f5e9;
  color: var(--success);
  border: 1px solid #c8e6c9;
  border-radius: var(--radius);
  padding: 12px 16px;
  margin-bottom: 16px;
}

.field-error {
  color: var(--danger);
  font-size: 0.85rem;
  margin-top: 4px;
}

input.invalid,
select.invalid {
  border-color: var(--danger);
}
"""

_INDEX_HTML = """\
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Salon App</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
"""


class OutputWriter:
    """Writes migrated React files to the configured output directory."""

    def __init__(self, output_dir: str | Path):
        self.output_dir = Path(output_dir)

    def write_boilerplate(self) -> None:
        """Write all scaffolding files that don't require AI generation."""
        self._write("src/services/apiClient.ts", _AXIOS_CLIENT)
        self._write("src/context/AuthContext.tsx", _AUTH_CONTEXT)
        self._write("src/components/ProtectedRoute.tsx", _PROTECTED_ROUTE)
        self._write("vite.config.ts", _VITE_CONFIG)
        self._write("package.json", _PACKAGE_JSON)
        self._write("tsconfig.json", _TSCONFIG)
        self._write("tsconfig.node.json", _TSCONFIG_NODE)
        self._write(".env.development", _ENV_DEVELOPMENT)
        self._write(".env.production", _ENV_PRODUCTION)
        self._write("src/main.tsx", _MAIN_TSX)
        self._write("src/index.css", _INDEX_CSS)
        self._write("index.html", _INDEX_HTML)
        logger.info("Boilerplate files written to %s", self.output_dir)

    def write_component(self, component_name: str, content: str) -> Path:
        """Write a migrated React component file."""
        filename = self._pascal_to_component_path(component_name)
        path = self._write(f"src/components/{filename}", content)
        return path

    def write_service(self, service_name: str, content: str) -> Path:
        """Write a migrated React service file."""
        filename = self._service_filename(service_name)
        path = self._write(f"src/services/{filename}", content)
        return path

    def write_type(self, model_name: str, content: str) -> Path:
        """Write a migrated TypeScript types file."""
        base = model_name.replace(".model", "").replace("-", "_")
        path = self._write(f"src/types/{base}.ts", content)
        return path

    def write_guard(self, _guard_name: str, content: str) -> Path:
        """Write the ProtectedRoute component (guards become ProtectedRoute)."""
        path = self._write("src/components/ProtectedRoute.tsx", content)
        return path

    def write_app(self, content: str) -> Path:
        """Write the root App.tsx file."""
        path = self._write("src/App.tsx", content)
        return path

    def write_env(self, content: str) -> None:
        """Parse and write environment configuration output from AI."""
        sections = self._parse_env_sections(content)
        for filename, file_content in sections.items():
            self._write(filename, file_content)

    def write_report(self, report: str) -> Path:
        """Write the migration report markdown file."""
        path = self._write("MIGRATION_REPORT.md", report)
        return path

    # ─────────────────────────────────────────────────────────────────────────
    # Internal helpers
    # ─────────────────────────────────────────────────────────────────────────

    def _write(self, relative_path: str, content: str) -> Path:
        full_path = self.output_dir / relative_path
        full_path.parent.mkdir(parents=True, exist_ok=True)
        full_path.write_text(content, encoding="utf-8")
        logger.info("  Written: %s", relative_path)
        return full_path

    @staticmethod
    def _pascal_to_component_path(name: str) -> str:
        """LoginComponent → Login/Login.tsx"""
        base = name.replace("Component", "")
        return f"{base}/{base}.tsx"

    @staticmethod
    def _service_filename(name: str) -> str:
        """AuthService → authService.ts (camelCase)"""
        base = name.replace("Service", "")
        return f"{base[0].lower()}{base[1:]}Service.ts"

    @staticmethod
    def _parse_env_sections(content: str) -> dict:
        """
        Parse AI output that contains multiple file sections separated by
        `// --- FILE: <filename> ---` markers.
        """
        import re
        sections: dict = {}
        pattern = re.compile(r"//\s*---\s*FILE:\s*(.+?)\s*---\s*\n([\s\S]*?)(?=//\s*---\s*FILE:|$)")
        for match in pattern.finditer(content):
            filename = match.group(1).strip()
            file_content = match.group(2).strip()
            sections[filename] = file_content
        if not sections:
            # Fallback: treat entire output as a single env helper
            sections["src/config/env.ts"] = content
        return sections
