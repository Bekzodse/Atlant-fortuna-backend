"use client";

import { useState } from "react";
import { Upload, Trash2, Image as ImageIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { galleryApi } from "@/lib/api";
import { useGalleryPhotos } from "@/hooks/use-api";
import { toast } from "sonner";
import Image from "next/image";

export default function GalleryPage() {
  const { photos, isLoading, mutate } = useGalleryPhotos();
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (files.length === 0) return;

    setUploading(true);
    try {
      const uploadPromises = files.map((file) => galleryApi.upload(file));
      await Promise.all(uploadPromises);
      toast.success("Photos uploaded successfully");
      mutate(); // Revalidate cache
      // Clear the file input
      e.target.value = "";
    } catch (error) {
      toast.error("Failed to upload photos");
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      // Optimistically update UI
      mutate(
        photos?.filter((photo) => photo.id !== deleteId),
        false
      );

      await galleryApi.delete(deleteId);
      toast.success("Photo deleted successfully");
    } catch (error) {
      toast.error("Failed to delete photo");
      // Revert on error
      mutate();
    } finally {
      setDeleteId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Gallery</h1>
          <p className="text-muted-foreground">Manage gallery photos</p>
        </div>
        <div>
          <input type="file" accept="image/*" multiple onChange={handleUpload} className="hidden" id="gallery-upload" />
          <label htmlFor="gallery-upload">
            <Button disabled={uploading} asChild>
              <span><Upload className="h-4 w-4 mr-2" />{uploading ? "Uploading..." : "Upload Photos"}</span>
            </Button>
          </label>
        </div>
      </div>

      {isLoading ? (
        <div>Loading...</div>
      ) : !photos || photos.length === 0 ? (
        <Card><CardContent className="flex flex-col items-center justify-center py-12"><ImageIcon className="h-12 w-12 text-muted-foreground mb-4" /><p>No photos in gallery</p></CardContent></Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {photos.map((photo) => (
            <Card key={photo.id} className="overflow-hidden p-0 group relative">
              <div className="aspect-square relative">
                <Image src={photo.attach.url} alt="Gallery photo" fill className="object-cover" />
                <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                  <Button variant="destructive" size="icon" onClick={() => setDeleteId(photo.id)}>
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>This will permanently delete the photo.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete}>Delete</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
