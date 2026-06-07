import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { login as loginRequest, register as registerRequest } from '../api/authApi.js';
import { setAccessTokenProvider } from '../api/apiClient.js';
import { getMe } from '../api/userApi.js';

const STORAGE_KEY = 'quiz_live_auth';
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => readStoredAuth());
  const [bootstrapping, setBootstrapping] = useState(true);

  useEffect(() => {
    setAccessTokenProvider(() => auth?.accessToken || null);
    persistAuth(auth);
  }, [auth]);

  useEffect(() => {
    if (!auth?.accessToken) {
      setBootstrapping(false);
      return;
    }

    getMe()
      .then((user) => setAuth((current) => ({ ...current, user })))
      .catch(() => setAuth(null))
      .finally(() => setBootstrapping(false));
  }, []);

  const value = useMemo(() => ({
    auth,
    user: auth?.user || null,
    accessToken: auth?.accessToken || null,
    bootstrapping,
    isAuthenticated: Boolean(auth?.accessToken),
    async login(credentials) {
      const response = await loginRequest(credentials);
      setAuth(response);
      return response;
    },
    async register(payload) {
      const response = await registerRequest(payload);
      setAuth(response);
      return response;
    },
    logout() {
      setAuth(null);
    },
    updateUser(user) {
      setAuth((current) => current ? { ...current, user } : current);
    },
  }), [auth, bootstrapping]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }

  return context;
}

function readStoredAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function persistAuth(auth) {
  if (!auth) {
    localStorage.removeItem(STORAGE_KEY);
    return;
  }

  localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
}
