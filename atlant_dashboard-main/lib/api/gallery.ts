import { apiClient } from "./client";
import type { GalleryPhotoDTO } from "@/types";

export const galleryApi = {
  /**
   * Get all gallery photos
   */
  getAll: async (): Promise<GalleryPhotoDTO[]> => {
    const response = await apiClient.get<GalleryPhotoDTO[]>("/gallery/public");
    return response.data;
  },

  /**
   * Upload a photo to gallery (Admin)
   */
  upload: async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await apiClient.post<string>(
      "/gallery/admin/upload",
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );
    return response.data;
  },

  /**
   * Delete a photo from gallery (Admin)
   */
  delete: async (id: string): Promise<string> => {
    const response = await apiClient.delete<string>(`/gallery/admin/${id}`);
    return response.data;
  },
};
