import useSWR from "swr";
import {
  productsApi,
  teamApi,
  licensesApi,
  galleryApi,
  reelsApi,
  carouselApi,
  telegramApi,
} from "@/lib/api";
import type {
  ProductResponseDTO,
  TeamMemberResponseDTO,
  LicenseResponseDTO,
  GalleryPhotoDTO,
  ReelDTO,
  CarouselResponseDTO,
  ProductCategory,
  TelegramBotResponseDTO,
  TelegramChatResponseDTO,
} from "@/types";

// SWR configuration
const swrConfig = {
  revalidateOnFocus: false, // Don't revalidate when window gains focus
  revalidateOnReconnect: true, // Revalidate when reconnecting
  dedupingInterval: 5000, // Prevent duplicate requests within 5 seconds
  shouldRetryOnError: false, // Don't retry on error
};

// ==================== Products Hooks ====================

export function useProducts() {
  const { data, error, isLoading, mutate } = useSWR<ProductResponseDTO[]>(
    "/api/v1/products/admin",
    () => productsApi.getAllAdmin(),
    swrConfig
  );

  return {
    products: data,
    isLoading,
    isError: error,
    mutate, // Use this to manually revalidate/update cache
  };
}

export function useProduct(id: string | null) {
  const { data, error, isLoading, mutate } = useSWR<ProductResponseDTO>(
    id ? `/api/v1/products/public/${id}` : null,
    id ? () => productsApi.getById(id) : null,
    swrConfig
  );

  return {
    product: data,
    isLoading,
    isError: error,
    mutate,
  };
}

export function useProductsByCategory(category: ProductCategory | null) {
  const { data, error, isLoading, mutate } = useSWR<ProductResponseDTO[]>(
    category ? `/api/v1/products/public/category/${category}` : null,
    category ? () => productsApi.getByCategory(category) : null,
    swrConfig
  );

  return {
    products: data,
    isLoading,
    isError: error,
    mutate,
  };
}

// ==================== Team Members Hooks ====================

export function useTeamMembers() {
  const { data, error, isLoading, mutate } = useSWR<TeamMemberResponseDTO[]>(
    "/api/v1/team/admin",
    () => teamApi.getAllAdmin(),
    swrConfig
  );

  return {
    members: data,
    isLoading,
    isError: error,
    mutate,
  };
}

// ==================== Licenses Hooks ====================

export function useLicenses() {
  const { data, error, isLoading, mutate} = useSWR<LicenseResponseDTO[]>(
    "/api/v1/licenses/public",
    () => licensesApi.getAll(),
    swrConfig
  );

  return {
    licenses: data,
    isLoading,
    isError: error,
    mutate,
  };
}

// ==================== Telegram Hooks ====================

export function useTelegramBots() {
  const { data, error, isLoading, mutate } = useSWR<TelegramBotResponseDTO[]>(
    "/api/v1/telegram/admin/bots",
    () => telegramApi.getBots(),
    swrConfig
  );

  return {
    bots: data,
    isLoading,
    isError: error,
    mutate,
  };
}

export function useTelegramChats() {
  const { data, error, isLoading, mutate } = useSWR<TelegramChatResponseDTO[]>(
    "/api/v1/telegram/admin/chats",
    () => telegramApi.getChats(),
    swrConfig
  );

  return {
    chats: data,
    isLoading,
    isError: error,
    mutate,
  };
}

// ==================== Gallery Hooks ====================

export function useGalleryPhotos() {
  const { data, error, isLoading, mutate } = useSWR<GalleryPhotoDTO[]>(
    "/api/v1/gallery/public",
    () => galleryApi.getAll(),
    swrConfig
  );

  return {
    photos: data,
    isLoading,
    isError: error,
    mutate,
  };
}

export function useReels() {
  const { data, error, isLoading, mutate } = useSWR<ReelDTO[]>(
    "/api/v1/reels/public",
    () => reelsApi.getAll(),
    swrConfig
  );

  return { reels: data, isLoading, isError: error, mutate };
}

// ==================== Carousel Hooks ====================

export function useCarouselItems() {
  const { data, error, isLoading, mutate } = useSWR<CarouselResponseDTO[]>(
    "/api/v1/carousel/admin",
    () => carouselApi.getAll(),
    swrConfig
  );

  return {
    items: data,
    isLoading,
    isError: error,
    mutate,
  };
}

// ==================== Dashboard Stats Hook ====================

export function useDashboardStats() {
  const { products, isLoading: productsLoading } = useProducts();
  const { members, isLoading: membersLoading } = useTeamMembers();
  const { licenses, isLoading: licensesLoading } = useLicenses();
  const { photos, isLoading: photosLoading } = useGalleryPhotos();

  const isLoading =
    productsLoading || membersLoading || licensesLoading || photosLoading;

  const stats = {
    totalProducts: products?.length || 0,
    totalTeamMembers: members?.length || 0,
    totalLicenses: licenses?.length || 0,
    totalGalleryImages: photos?.length || 0,
    productsWithDiscount: products?.filter((p) => p.discount).length || 0,
    productsByCategory:
      products?.reduce(
        (acc, product) => {
          acc[product.category] = (acc[product.category] || 0) + 1;
          return acc;
        },
        {} as Record<ProductCategory, number>
      ) || {},
  };

  return {
    stats,
    isLoading,
    products,
    members,
    licenses,
    photos,
  };
}
