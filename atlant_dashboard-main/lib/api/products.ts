import { apiClient } from "./client";
import type {
  ProductRequestDTO,
  ProductResponseDTO,
  ProductCategory,
} from "@/types";

export const productsApi = {
  /**
   * Get all products (Public)
   */
  getAll: async (): Promise<ProductResponseDTO[]> => {
    const response = await apiClient.get<ProductResponseDTO[]>("/products/public");
    return response.data;
  },

  /**
   * Get all products (Admin) - includes all multi-language fields
   */
  getAllAdmin: async (): Promise<ProductResponseDTO[]> => {
    const response = await apiClient.get<ProductResponseDTO[]>("/products/admin");
    return response.data;
  },

  /**
   * Get product by ID (Public)
   */
  getById: async (id: string): Promise<ProductResponseDTO> => {
    const response = await apiClient.get<ProductResponseDTO>(`/products/public/${id}`);
    return response.data;
  },

  /**
   * Get product by ID (Admin) - includes all multi-language fields
   */
  getByIdAdmin: async (id: string): Promise<ProductResponseDTO> => {
    const response = await apiClient.get<ProductResponseDTO>(`/products/admin/${id}`);
    return response.data;
  },

  /**
   * Get products by category
   */
  getByCategory: async (category: ProductCategory): Promise<ProductResponseDTO[]> => {
    const response = await apiClient.get<ProductResponseDTO[]>(
      `/products/public/category/${category}`
    );
    return response.data;
  },

  /**
   * Get products by brand
   */
  getByBrand: async (brand: string): Promise<ProductResponseDTO[]> => {
    const response = await apiClient.get<ProductResponseDTO[]>(
      `/products/public/brand/${brand}`
    );
    return response.data;
  },

  /**
   * Create a new product (Admin)
   */
  create: async (data: ProductRequestDTO): Promise<ProductResponseDTO> => {
    const response = await apiClient.post<ProductResponseDTO>("/products/admin", data);
    return response.data;
  },

  /**
   * Update a product (Admin)
   */
  update: async (id: string, data: ProductRequestDTO): Promise<ProductResponseDTO> => {
    const response = await apiClient.put<ProductResponseDTO>(
      `/products/admin/${id}`,
      data
    );
    return response.data;
  },

  /**
   * Delete a product (Admin)
   */
  delete: async (id: string): Promise<string> => {
    const response = await apiClient.delete<string>(`/products/admin/${id}`);
    return response.data;
  },
};
