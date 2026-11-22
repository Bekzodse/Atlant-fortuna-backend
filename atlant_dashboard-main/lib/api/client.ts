import axios, { type AxiosInstance, type AxiosError } from "axios";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "https://atlant-backend-production.up.railway.app/api/v1";

// Create axios instance
export const apiClient: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor - Add auth token to requests
apiClient.interceptors.request.use(
  (config) => {
    const token = getAuthToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log("Request interceptor - Token added:", {
        url: config.url,
        method: config.method,
        hasToken: true,
        tokenPreview: token.substring(0, 20) + "...",
      });
    } else {
      console.warn("Request interceptor - NO TOKEN FOUND for:", config.url);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Handle errors globally
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Token expired or invalid - redirect to login
      removeAuthToken();
      if (typeof window !== "undefined") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

// Token management functions
export const setAuthToken = (token: string) => {
  if (typeof window !== "undefined") {
    localStorage.setItem("auth_token", token);
  }
};

export const getAuthToken = (): string | null => {
  if (typeof window !== "undefined") {
    return localStorage.getItem("auth_token");
  }
  return null;
};

export const removeAuthToken = () => {
  if (typeof window !== "undefined") {
    localStorage.removeItem("auth_token");
  }
};

export const isAuthenticated = (): boolean => {
  return !!getAuthToken();
};
