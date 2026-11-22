// ==================== Enums ====================

export enum ProductCategory {
  WATER_PURIFICATION_FILTERS = "WATER_PURIFICATION_FILTERS",
  CARBONATED_WATER_EQUIPMENT = "CARBONATED_WATER_EQUIPMENT",
  HOUSEHOLD_WATER_FILTERS = "HOUSEHOLD_WATER_FILTERS",
  WATER_DISPENSERS_COOLERS = "WATER_DISPENSERS_COOLERS",
  WATER_FILTER_SPARE_PARTS = "WATER_FILTER_SPARE_PARTS",
  OTHER_PRODUCTS_EQUIPMENT = "OTHER_PRODUCTS_EQUIPMENT",
}

export enum Language {
  EN = "en",
  RU = "ru",
  UZ = "uz",
}

// ==================== Multi-language Fields ====================

export interface MultiLangText {
  en: string;
  ru: string;
  uz: string;
}

// ==================== Authentication ====================

export interface AuthRequestDTO {
  name: string;
  email: string;
  password: string;
}

export interface LoginRequestDTO {
  email: string;
  password: string;
}

export interface LoginResponseDTO {
  token: string;
  user?: {
    id: string;
    name: string;
    email: string;
  };
}

// ==================== Team Member ====================

export interface TeamMemberRequestDTO {
  nameEn: string;
  nameRu: string;
  nameUz: string;
  roleEn: string;
  roleRu: string;
  roleUz: string;
  phoneNumber: string;
  imageId: string;
}

export interface TeamMemberResponseDTO {
  id: string;
  name?: string;
  nameEn: string;
  nameRu: string;
  nameUz: string;
  role?: string;
  roleEn: string;
  roleRu: string;
  roleUz: string;
  phoneNumber: string;
  image?: AttachDTO;
  createdAt?: string;
  updatedAt?: string;
}

// ==================== Product ====================

export interface ProductRequestDTO {
  nameUz: string;
  nameRu: string;
  nameEn: string;
  price: number;
  brand: string;
  articleNumber: string;
  category: ProductCategory;
  paymentMethodEn: string;
  paymentMethodRu: string;
  paymentMethodUz: string;
  deliveryEn: string;
  deliveryRu: string;
  deliveryUz: string;
  descriptionEn: string;
  descriptionRu: string;
  descriptionUz: string;
  discount: boolean;
  imageIds: string[];
}

export interface ProductResponseDTO {
  id: string;
  nameUz: string;
  nameRu: string;
  nameEn: string;
  price: number;
  brand: string;
  articleNumber: string;
  category: ProductCategory;
  paymentMethodEn: string;
  paymentMethodRu: string;
  paymentMethodUz: string;
  deliveryEn: string;
  deliveryRu: string;
  deliveryUz: string;
  descriptionEn: string;
  descriptionRu: string;
  descriptionUz: string;
  discount: boolean;
  images: Array<{
    id: string;
    attachId: string;
    path: string;
  }>;
  createdAt?: string;
  updatedAt?: string;
}

// ==================== License ====================

export interface LicenseRequestDTO {
  titleEn: string;
  titleRu: string;
  titleUz: string;
  imageId: string;
}

export interface LicenseResponseDTO {
  id: string;
  title: string;
  image: {
    id: string;
    url: string;
  };
  createdAt?: string;
}

// ==================== Gallery ====================

export interface GalleryPhotoDTO {
  id: string;
  attach: AttachDTO;
  createdAt: string;
}

export interface ReelDTO {
  id: string;
  attach: AttachDTO;
  createdAt: string;
}

// ==================== Carousel ====================

export interface CarouselRequestDTO {
  imageId: string;
  titleEn?: string;
  titleRu?: string;
  titleUz?: string;
  descriptionEn?: string;
  descriptionRu?: string;
  descriptionUz?: string;
  active?: boolean;
}

export interface CarouselResponseDTO {
  id: string;
  attachId: string;
  imageUrl: string;
  active: boolean;
  createdAt: string;
}

// ==================== Attachment ====================

export interface AttachDTO {
  id: string;
  originName: string;
  size: number;
  extension: string;
  createdData: string;
  url: string;
}

// ==================== API Response Wrappers ====================

export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
}

// ==================== Telegram ====================

export interface TelegramBotResponseDTO {
  id: string;
  label: string;
  maskedToken?: string;
  active: boolean;
  createdAt?: string;
}

export interface TelegramBotRequestDTO {
  label: string;
  token: string;
}

export interface TelegramChatResponseDTO {
  id: string;
  label: string;
  groupId: string;
  active: boolean;
  createdAt?: string;
}

export interface TelegramChatRequestDTO {
  label: string;
  groupId: string;
}

export interface TelegramChatUpdateDTO {
  label?: string;
  groupId?: string;
  active?: boolean;
}

// ==================== Form Types ====================

export interface ProductFormData extends Omit<ProductRequestDTO, "imageIds"> {
  images: File[];
  existingImageIds?: string[];
}

export interface TeamMemberFormData extends Omit<TeamMemberRequestDTO, "imageId"> {
  image?: File;
  existingImageId?: string;
}

export interface LicenseFormData extends Omit<LicenseRequestDTO, "imageId"> {
  image?: File;
  existingImageId?: string;
}

// ==================== Dashboard Stats ====================

export interface DashboardStats {
  totalProducts: number;
  totalTeamMembers: number;
  totalLicenses: number;
  totalGalleryImages: number;
  productsWithDiscount: number;
  productsByCategory: Record<ProductCategory, number>;
}
