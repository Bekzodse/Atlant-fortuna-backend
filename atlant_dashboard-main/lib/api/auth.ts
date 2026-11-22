import { apiClient, setAuthToken, removeAuthToken } from "./client";
import type {
  AuthRequestDTO,
  LoginRequestDTO,
  LoginResponseDTO,
} from "@/types";

export const authApi = {
  /**
   * Register a new user
   */
  register: async (data: AuthRequestDTO): Promise<string> => {
    const response = await apiClient.post<string>("/auth/register", data);
    return response.data;
  },

  /**
   * Login user and get JWT token
   */
  login: async (data: LoginRequestDTO): Promise<LoginResponseDTO> => {
    const response = await apiClient.post<LoginResponseDTO>("/auth/login", data);
    const { token } = response.data;

    // Store token
    setAuthToken(token);

    return response.data;
  },

  /**
   * Logout user
   */
  logout: (): void => {
    removeAuthToken();
    if (typeof window !== "undefined") {
      window.location.href = "/login";
    }
  },
};
