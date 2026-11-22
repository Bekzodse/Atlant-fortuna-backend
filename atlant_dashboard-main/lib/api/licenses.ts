import { apiClient } from "./client";
import type { LicenseRequestDTO, LicenseResponseDTO } from "@/types";

export const licensesApi = {
  /**
   * Get all licenses
   */
  getAll: async (): Promise<LicenseResponseDTO[]> => {
    const response = await apiClient.get<LicenseResponseDTO[]>("/licenses/public");
    return response.data;
  },

  /**
   * Get license by ID
   */
  getById: async (id: string): Promise<LicenseResponseDTO> => {
    const response = await apiClient.get<LicenseResponseDTO>(
      `/licenses/public/${id}`
    );
    return response.data;
  },

  /**
   * Create a new license (Admin)
   */
  create: async (data: LicenseRequestDTO): Promise<LicenseResponseDTO> => {
    const response = await apiClient.post<LicenseResponseDTO>("/licenses/admin", data);
    return response.data;
  },

  /**
   * Delete a license (Admin)
   */
  delete: async (id: string): Promise<string> => {
    const response = await apiClient.delete<string>(`/licenses/admin/${id}`);
    return response.data;
  },
};
