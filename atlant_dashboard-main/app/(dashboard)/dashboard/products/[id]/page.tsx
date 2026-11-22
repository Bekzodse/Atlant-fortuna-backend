"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import { ArrowLeft, Save } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { MultiLangInput } from "@/components/forms/MultiLangInput";
import { MultiLangTextarea } from "@/components/forms/MultiLangTextarea";
import { ImageUpload } from "@/components/forms/ImageUpload";
import { productsApi, attachmentsApi } from "@/lib/api";
import { ProductCategory, ProductRequestDTO } from "@/types";
import { toast } from "sonner";
import Link from "next/link";
import { AxiosError } from "axios";

// Extend ProductRequestDTO to allow empty price during form input
interface ProductFormData extends Omit<ProductRequestDTO, 'price'> {
  price: number | "";
}

export default function ProductFormPage() {
  const router = useRouter();
  const params = useParams();
  const isNew = params.id === "new";

  const [isLoading, setIsLoading] = useState(false);
  const [isFetchingProduct, setIsFetchingProduct] = useState(false);
  const [formData, setFormData] = useState<ProductFormData>({
    nameEn: "",
    nameRu: "",
    nameUz: "",
    price: "",
    brand: "",
    articleNumber: "",
    category: ProductCategory.WATER_PURIFICATION_FILTERS,
    paymentMethodEn: "",
    paymentMethodRu: "",
    paymentMethodUz: "",
    deliveryEn: "",
    deliveryRu: "",
    deliveryUz: "",
    descriptionEn: "",
    descriptionRu: "",
    descriptionUz: "",
    discount: false,
    imageIds: [],
  });
  const [images, setImages] = useState<File[]>([]);

  // Fetch product data when editing
  useEffect(() => {
    if (!isNew) {
      fetchProduct();
    }
  }, [params.id]);

  const fetchProduct = async () => {
    setIsFetchingProduct(true);
    try {
      const product = await productsApi.getByIdAdmin(params.id as string);
      setFormData({
        nameEn: product.nameEn,
        nameRu: product.nameRu,
        nameUz: product.nameUz,
        price: product.price,
        brand: product.brand,
        articleNumber: product.articleNumber,
        category: product.category,
        paymentMethodEn: product.paymentMethodEn,
        paymentMethodRu: product.paymentMethodRu,
        paymentMethodUz: product.paymentMethodUz,
        deliveryEn: product.deliveryEn,
        deliveryRu: product.deliveryRu,
        deliveryUz: product.deliveryUz,
        descriptionEn: product.descriptionEn,
        descriptionRu: product.descriptionRu,
        descriptionUz: product.descriptionUz,
        discount: product.discount,
        imageIds: product.images?.map(img => img.attachId) || [],
      });
    } catch (error) {
      const axiosError = error as AxiosError<{ message?: string }>;
      toast.error(axiosError.response?.data?.message || "Failed to fetch product");
      console.error("Product fetch error:", error);
    } finally {
      setIsFetchingProduct(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      // Upload images first
      let imageIds = [...formData.imageIds];
      if (images.length > 0) {
        const uploadPromises = images.map((file) => attachmentsApi.upload(file));
        const uploadedFiles = await Promise.all(uploadPromises);
        imageIds = [...imageIds, ...uploadedFiles.map((f) => f.id)];
      }

      const productData: ProductRequestDTO = {
        ...formData,
        price: typeof formData.price === "number" ? formData.price : parseFloat(formData.price) || 0,
        imageIds,
      };

      if (isNew) {
        await productsApi.create(productData);
        toast.success("Product created successfully");
      } else {
        await productsApi.update(params.id as string, productData);
        toast.success("Product updated successfully");
      }

      router.push("/dashboard/products");
    } catch (error) {
      const axiosError = error as AxiosError<{ message?: string }>;
      toast.error(axiosError.response?.data?.message || "Failed to save product");
      console.error("Product save error:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const formatCategoryName = (category: string) => {
    return category
      .split("_")
      .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
      .join(" ");
  };

  // Show loading while fetching product for edit mode
  if (!isNew && isFetchingProduct) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <Link href="/dashboard/products">
            <Button variant="ghost" size="icon">
              <ArrowLeft className="h-4 w-4" />
            </Button>
          </Link>
          <div>
            <h1 className="text-3xl font-bold">Edit Product</h1>
            <p className="text-muted-foreground">Loading product data...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link href="/dashboard/products">
          <Button variant="ghost" size="icon">
            <ArrowLeft className="h-4 w-4" />
          </Button>
        </Link>
        <div>
          <h1 className="text-3xl font-bold">
            {isNew ? "Add New Product" : "Edit Product"}
          </h1>
          <p className="text-muted-foreground">
            {isNew ? "Create a new product" : "Update product information"}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="grid gap-6">
          <Card>
            <CardHeader>
              <CardTitle>Basic Information</CardTitle>
              <CardDescription>Product name and identification</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <MultiLangInput
                label="Product Name"
                valueEn={formData.nameEn}
                valueRu={formData.nameRu}
                valueUz={formData.nameUz}
                onChangeEn={(v) => setFormData({ ...formData, nameEn: v })}
                onChangeRu={(v) => setFormData({ ...formData, nameRu: v })}
                onChangeUz={(v) => setFormData({ ...formData, nameUz: v })}
                placeholder="Enter product name"
                required
              />

              <div className="grid gap-4 md:grid-cols-3">
                <div className="space-y-2">
                  <Label htmlFor="brand">
                    Brand <span className="text-destructive">*</span>
                  </Label>
                  <Input
                    id="brand"
                    value={formData.brand}
                    onChange={(e) =>
                      setFormData({ ...formData, brand: e.target.value })
                    }
                    placeholder="Enter brand name"
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="articleNumber">
                    Article Number <span className="text-destructive">*</span>
                  </Label>
                  <Input
                    id="articleNumber"
                    value={formData.articleNumber}
                    onChange={(e) =>
                      setFormData({ ...formData, articleNumber: e.target.value })
                    }
                    placeholder="e.g. ATL-001"
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="price">
                    Price (sum) <span className="text-destructive">*</span>
                  </Label>
                  <Input
                    id="price"
                    type="number"
                    step="0.01"
                    min="0"
                    value={formData.price}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        price: e.target.value === "" ? "" : parseFloat(e.target.value),
                      })
                    }
                    placeholder="Enter price"
                    required
                  />
                </div>
              </div>

              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="category">
                    Category <span className="text-destructive">*</span>
                  </Label>
                  <Select
                    value={formData.category}
                    onValueChange={(value) =>
                      setFormData({
                        ...formData,
                        category: value as ProductCategory,
                      })
                    }
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {Object.values(ProductCategory).map((category) => (
                        <SelectItem key={category} value={category}>
                          {formatCategoryName(category)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="flex items-center space-x-2">
                  <Switch
                    id="discount"
                    checked={formData.discount}
                    onCheckedChange={(checked) =>
                      setFormData({ ...formData, discount: checked })
                    }
                  />
                  <Label htmlFor="discount">Product has discount</Label>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Product Details</CardTitle>
              <CardDescription>
                Detailed description and specifications
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <MultiLangTextarea
                label="Description"
                valueEn={formData.descriptionEn}
                valueRu={formData.descriptionRu}
                valueUz={formData.descriptionUz}
                onChangeEn={(v) => setFormData({ ...formData, descriptionEn: v })}
                onChangeRu={(v) => setFormData({ ...formData, descriptionRu: v })}
                onChangeUz={(v) => setFormData({ ...formData, descriptionUz: v })}
                placeholder="Enter product description"
                required
                rows={6}
              />

              <MultiLangTextarea
                label="Payment Method"
                valueEn={formData.paymentMethodEn}
                valueRu={formData.paymentMethodRu}
                valueUz={formData.paymentMethodUz}
                onChangeEn={(v) =>
                  setFormData({ ...formData, paymentMethodEn: v })
                }
                onChangeRu={(v) =>
                  setFormData({ ...formData, paymentMethodRu: v })
                }
                onChangeUz={(v) =>
                  setFormData({ ...formData, paymentMethodUz: v })
                }
                placeholder="Enter payment method details"
                required
              />

              <MultiLangTextarea
                label="Delivery Information"
                valueEn={formData.deliveryEn}
                valueRu={formData.deliveryRu}
                valueUz={formData.deliveryUz}
                onChangeEn={(v) => setFormData({ ...formData, deliveryEn: v })}
                onChangeRu={(v) => setFormData({ ...formData, deliveryRu: v })}
                onChangeUz={(v) => setFormData({ ...formData, deliveryUz: v })}
                placeholder="Enter delivery information"
                required
              />
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Product Images</CardTitle>
              <CardDescription>
                Upload product images (maximum 5 images)
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ImageUpload
                label="Images"
                value={images}
                onChange={setImages}
                multiple
                maxFiles={5}
                required={isNew}
              />
            </CardContent>
          </Card>

          <div className="flex gap-4">
            <Button type="submit" disabled={isLoading}>
              <Save className="h-4 w-4 mr-2" />
              {isLoading ? "Saving..." : "Save Product"}
            </Button>
            <Link href="/dashboard/products">
              <Button type="button" variant="outline">
                Cancel
              </Button>
            </Link>
          </div>
        </div>
      </form>
    </div>
  );
}
