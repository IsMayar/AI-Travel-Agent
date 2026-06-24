import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

const STORAGE_KEY = "aiTravelAgentAuth";

export interface AuthUser {
  id: number;
  fullName: string;
  email: string;
}

export interface AuthResponse {
  token: string;
  user: AuthUser;
}

interface AuthState {
  token: string | null;
  user: AuthUser | null;
}

function loadInitialAuthState(): AuthState {
  const emptyState: AuthState = {
    token: null,
    user: null,
  };

  try {
    const storedValue = localStorage.getItem(STORAGE_KEY);
    if (!storedValue) {
      return emptyState;
    }

    const parsedValue = JSON.parse(storedValue) as AuthState;
    if (!parsedValue.token || !parsedValue.user) {
      return emptyState;
    }

    return parsedValue;
  } catch {
    return emptyState;
  }
}

function persistAuthState(state: AuthState) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

const authSlice = createSlice({
  name: "auth",
  initialState: loadInitialAuthState(),
  reducers: {
    setCredentials: (state, action: PayloadAction<AuthResponse>) => {
      state.token = action.payload.token;
      state.user = action.payload.user;
      persistAuthState({
        token: action.payload.token,
        user: action.payload.user,
      });
    },
    setUser: (state, action: PayloadAction<AuthUser>) => {
      state.user = action.payload;
      persistAuthState({
        token: state.token,
        user: action.payload,
      });
    },
    logout: (state) => {
      state.token = null;
      state.user = null;
      localStorage.removeItem(STORAGE_KEY);
    },
  },
});

export const { logout, setCredentials, setUser } = authSlice.actions;
export const authReducer = authSlice.reducer;
