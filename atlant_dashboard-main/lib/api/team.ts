import { apiClient } from "./client";
import type {
  TeamMemberRequestDTO,
  TeamMemberResponseDTO,
  Language,
} from "@/types";
import { AxiosError } from "axios";

export const teamApi = {
  /**
   * Get all team members (Public - with language filtering)
   */
  getAll: async (language?: Language): Promise<TeamMemberResponseDTO[]> => {
    const config = language
      ? { headers: { "Accept-Language": language } }
      : undefined;

    const response = await apiClient.get<TeamMemberResponseDTO[]>(
      "/team/public",
      config
    );
    return response.data;
  },

  /**
   * Get all team members (Admin - no language filtering, requires auth)
   */
  getAllAdmin: async (): Promise<TeamMemberResponseDTO[]> => {
    const response = await apiClient.get<TeamMemberResponseDTO[]>("/team/admin");
    return response.data;
  },

  /**
   * Create a new team member (Admin)
   */
  create: async (data: TeamMemberRequestDTO): Promise<TeamMemberResponseDTO> => {
    console.log("Creating team member with data:", data);
    try {
      const response = await apiClient.post<TeamMemberResponseDTO>("/team/admin", data);
      console.log("Team member created successfully:", response.data);
      return response.data;
    } catch (error) {
      const axiosError = error as AxiosError<{ message?: string }>;
      console.error("Team member creation error:", {
        status: axiosError.response?.status,
        statusText: axiosError.response?.statusText,
        data: axiosError.response?.data,
        requestData: data,
      });
      throw error;
    }
  },

  /**
   * Update a team member (Admin)
   */
  update: async (
    id: string,
    data: TeamMemberRequestDTO
  ): Promise<TeamMemberResponseDTO> => {
    const response = await apiClient.put<TeamMemberResponseDTO>(
      `/team/admin/${id}`,
      data
    );
    return response.data;
  },

  /**
   * Delete a team member (Admin)
   */
  delete: async (id: string): Promise<string> => {
    const response = await apiClient.delete<string>(`/team/admin/${id}`);
    return response.data;
  },
};
