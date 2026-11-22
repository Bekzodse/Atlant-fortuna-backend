"use client";

import { useCallback, useState } from "react";
import { Upload, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import Image from "next/image";

interface ImageUploadProps {
  label: string;
  value: File[];
  onChange: (files: File[]) => void;
  multiple?: boolean;
  maxFiles?: number;
  required?: boolean;
}

export function ImageUpload({
  label,
  value,
  onChange,
  multiple = false,
  maxFiles = 5,
  required = false,
}: ImageUploadProps) {
  const [previews, setPreviews] = useState<string[]>([]);

  const handleFileChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const files = Array.from(e.target.files || []);
      if (files.length === 0) return;

      const validFiles = files.filter((file) => file.type.startsWith("image/"));

      if (multiple) {
        const newFiles = [...value, ...validFiles].slice(0, maxFiles);
        onChange(newFiles);

        // Generate previews
        const newPreviews: string[] = [];
        validFiles.forEach((file) => {
          const reader = new FileReader();
          reader.onloadend = () => {
            newPreviews.push(reader.result as string);
            if (newPreviews.length === validFiles.length) {
              setPreviews((prev) => [...prev, ...newPreviews].slice(0, maxFiles));
            }
          };
          reader.readAsDataURL(file);
        });
      } else {
        onChange(validFiles.slice(0, 1));

        // Generate preview
        const reader = new FileReader();
        reader.onloadend = () => {
          setPreviews([reader.result as string]);
        };
        reader.readAsDataURL(validFiles[0]);
      }
    },
    [value, onChange, multiple, maxFiles]
  );

  const removeFile = (index: number) => {
    const newFiles = value.filter((_, i) => i !== index);
    const newPreviews = previews.filter((_, i) => i !== index);
    onChange(newFiles);
    setPreviews(newPreviews);
  };

  return (
    <div className="space-y-2">
      <Label>
        {label}
        {required && <span className="text-destructive ml-1">*</span>}
      </Label>

      <div className="space-y-4">
        {/* Upload Area */}
        <div className="border-2 border-dashed rounded-lg p-6 text-center hover:border-primary transition-colors">
          <input
            type="file"
            accept="image/*"
            multiple={multiple}
            onChange={handleFileChange}
            className="hidden"
            id="image-upload"
          />
          <label htmlFor="image-upload" className="cursor-pointer">
            <Upload className="h-8 w-8 mx-auto mb-2 text-muted-foreground" />
            <p className="text-sm text-muted-foreground">
              Click to upload {multiple ? "images" : "an image"}
            </p>
            {multiple && (
              <p className="text-xs text-muted-foreground mt-1">
                Maximum {maxFiles} files
              </p>
            )}
          </label>
        </div>

        {/* Image Previews */}
        {previews.length > 0 && (
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {previews.map((preview, index) => (
              <div key={index} className="relative group">
                <div className="aspect-square relative overflow-hidden rounded-lg border">
                  <Image
                    src={preview}
                    alt={`Preview ${index + 1}`}
                    fill
                    className="object-cover"
                  />
                </div>
                <Button
                  type="button"
                  variant="destructive"
                  size="icon"
                  className="absolute top-2 right-2 h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity"
                  onClick={() => removeFile(index)}
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
