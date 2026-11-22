import { apiClient } from "./client";
import type { CarouselRequestDTO, CarouselResponseDTO } from "@/types";

export const carouselApi = {
  /**
   * Get all active carousel items (Public)
   */
  getActive: async (): Promise<CarouselResponseDTO[]> => {
    const response = await apiClient.get<CarouselResponseDTO[]>("/carousel/public");
    return response.data;
  },

  /**
   * Get all carousel items (Admin)
   */
  getAll: async (): Promise<CarouselResponseDTO[]> => {
    const response = await apiClient.get<CarouselResponseDTO[]>("/carousel/admin");
    return response.data;
  },

  /**
   * Create a new carousel entry (Admin) - Just upload file
   */
  create: async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await apiClient.post<string>("/carousel/admin", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  },

  /**
   * Toggle carousel active status (Admin)
   */
  toggleActive: async (id: string): Promise<CarouselResponseDTO> => {
    const response = await apiClient.put<CarouselResponseDTO>(
      `/carousel/admin/${id}/toggle`
    );
    return response.data;
  },

  /**
   * Delete a carousel entry (Admin)
   */
  delete: async (id: string): Promise<string> => {
    const response = await apiClient.delete<string>(`/carousel/admin/${id}`);
    return response.data;
  },
};
