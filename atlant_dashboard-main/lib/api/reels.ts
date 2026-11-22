import { apiClient } from "./client";
import type { ReelDTO } from "@/types";

export const reelsApi = {
  getAll: async (): Promise<ReelDTO[]> => {
    const response = await apiClient.get<ReelDTO[]>("/reels/public");
    return response.data;
  },

  upload: async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append("file", file);
    const response = await apiClient.post<string>("/reels/admin/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return response.data;
  },

  delete: async (id: string): Promise<string> => {
    const response = await apiClient.delete<string>(`/reels/admin/${id}`);
    return response.data;
  },
};
