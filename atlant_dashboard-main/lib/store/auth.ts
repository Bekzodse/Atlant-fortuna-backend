import { create } from "zustand";
import { authApi, isAuthenticated } from "@/lib/api";
import type { LoginRequestDTO, AuthRequestDTO } from "@/types";
import { AxiosError } from "axios";

interface AuthState {
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (credentials: LoginRequestDTO) => Promise<void>;
  register: (data: AuthRequestDTO) => Promise<void>;
  logout: () => void;
  checkAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  isAuthenticated: isAuthenticated(),
  isLoading: false,
  error: null,

  login: async (credentials: LoginRequestDTO) => {
    set({ isLoading: true, error: null });
    try {
      await authApi.login(credentials);
      set({ isAuthenticated: true, isLoading: false });
    } catch (error) {
      const axiosError = error as AxiosError<{ message?: string }>;
      set({
        isLoading: false,
        error: axiosError.response?.data?.message || "Login failed",
      });
      throw error;
    }
  },

  register: async (data: AuthRequestDTO) => {
    set({ isLoading: true, error: null });
    try {
      await authApi.register(data);
      set({ isLoading: false });
    } catch (error) {
      const axiosError = error as AxiosError<{ message?: string }>;
      set({
        isLoading: false,
        error: axiosError.response?.data?.message || "Registration failed",
      });
      throw error;
    }
  },

  logout: () => {
    authApi.logout();
    set({ isAuthenticated: false });
  },

  checkAuth: () => {
    set({ isAuthenticated: isAuthenticated() });
  },
}));
