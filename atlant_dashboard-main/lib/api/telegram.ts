import { apiClient } from "./client";
import type {
  TelegramBotRequestDTO,
  TelegramBotResponseDTO,
  TelegramChatRequestDTO,
  TelegramChatResponseDTO,
  TelegramChatUpdateDTO,
} from "@/types";

export const telegramApi = {
  /**
   * List all configured bot tokens (masked)
   */
  getBots: async (): Promise<TelegramBotResponseDTO[]> => {
    const response = await apiClient.get<TelegramBotResponseDTO[]>("/telegram/admin/bots");
    return response.data;
  },

  /**
   * Add a new bot token (does not remove existing ones)
   */
  createBot: async (data: TelegramBotRequestDTO): Promise<TelegramBotResponseDTO> => {
    const response = await apiClient.post<TelegramBotResponseDTO>("/telegram/admin/bots", data);
    return response.data;
  },

  /**
   * Mark a bot as the one used to send messages
   */
  activateBot: async (id: string): Promise<TelegramBotResponseDTO> => {
    const response = await apiClient.patch<TelegramBotResponseDTO>(`/telegram/admin/bots/${id}/activate`);
    return response.data;
  },

  /**
   * Remove a bot token
   */
  deleteBot: async (id: string): Promise<string> => {
    const response = await apiClient.delete<string>(`/telegram/admin/bots/${id}`);
    return response.data;
  },

  /**
   * List all configured chat groups
   */
  getChats: async (): Promise<TelegramChatResponseDTO[]> => {
    const response = await apiClient.get<TelegramChatResponseDTO[]>("/telegram/admin/chats");
    return response.data;
  },

  /**
   * Add a new chat group
   */
  createChat: async (data: TelegramChatRequestDTO): Promise<TelegramChatResponseDTO> => {
    const response = await apiClient.post<TelegramChatResponseDTO>("/telegram/admin/chats", data);
    return response.data;
  },

  /**
   * Update label/groupId/active state of a chat group
   */
  updateChat: async (id: string, data: TelegramChatUpdateDTO): Promise<TelegramChatResponseDTO> => {
    const response = await apiClient.patch<TelegramChatResponseDTO>(`/telegram/admin/chats/${id}`, data);
    return response.data;
  },

  /**
   * Remove a chat group
   */
  deleteChat: async (id: string): Promise<string> => {
    const response = await apiClient.delete<string>(`/telegram/admin/chats/${id}`);
    return response.data;
  },

  /**
   * Send a test message to a chat group
   */
  testChat: async (id: string): Promise<string> => {
    const response = await apiClient.post<string>(`/telegram/admin/chats/${id}/test`);
    return response.data;
  },
};
