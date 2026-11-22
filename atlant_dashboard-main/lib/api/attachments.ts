import axios, { AxiosError } from "axios";
import { getAuthToken } from "./client";
import type { AttachDTO } from "@/types";

const ATTACH_BASE_URL = "https://atlant-backend-production.up.railway.app";

export const attachmentsApi = {
  /**
   * Upload a file (Admin)
   */
  upload: async (file: File): Promise<AttachDTO> => {
    const formData = new FormData();
    formData.append("file", file);

    const token = getAuthToken();

    console.log("Upload Debug Info:", {
      url: `${ATTACH_BASE_URL}/attach/admin/upload`,
      hasToken: !!token,
      tokenPreview: token ? `${token.substring(0, 20)}...` : "none",
      fileName: file.name,
      fileSize: file.size,
      fileType: file.type
    });

    try {
      const response = await axios.post<AttachDTO>(
        `${ATTACH_BASE_URL}/attach/admin/upload`,
        formData,
        {
          headers: {
            Authorization: token ? `Bearer ${token}` : "",
          },
        }
      );
      console.log("Upload success:", response.data);
      return response.data;
    } catch (error) {
      const axiosError = error as AxiosError<{ message?: string }>;
      console.error("Upload error details:", {
        status: axiosError.response?.status,
        statusText: axiosError.response?.statusText,
        data: axiosError.response?.data,
        headers: axiosError.response?.headers,
      });
      throw error;
    }
  },

  /**
   * Get file URL
   */
  getFileUrl: (fileId: string): string => {
    return `${ATTACH_BASE_URL}/attach/open/${fileId}`;
  },

  /**
   * Get download URL
   */
  getDownloadUrl: (fileId: string): string => {
    return `${ATTACH_BASE_URL}/attach/download/${fileId}`;
  },

  /**
   * Delete a file (Admin)
   */
  delete: async (fileId: string): Promise<string> => {
    const token = getAuthToken();
    const response = await axios.delete<string>(
      `${ATTACH_BASE_URL}/attach/admin/delete/${fileId}`,
      {
        headers: {
          Authorization: token ? `Bearer ${token}` : "",
        },
      }
    );
    return response.data;
  },
};
