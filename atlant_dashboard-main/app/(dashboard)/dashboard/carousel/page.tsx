"use client";

import { useState } from "react";
import { Plus, Trash2, Presentation } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";
import { ImageUpload } from "@/components/forms/ImageUpload";
import { carouselApi } from "@/lib/api";
import { useCarouselItems } from "@/hooks/use-api";
import { toast } from "sonner";
import Image from "next/image";

export default function CarouselPage() {
  const { items, isLoading, mutate } = useCarouselItems();
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [isOpen, setIsOpen] = useState(false);
  const [image, setImage] = useState<File[]>([]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (image.length === 0) {
        toast.error("Please upload an image");
        return;
      }
      await carouselApi.create(image[0]);
      toast.success("Carousel item created successfully");
      setIsOpen(false);
      setImage([]);
      mutate(); // Revalidate cache
    } catch (error) {
      toast.error("Failed to create carousel item");
    }
  };

  const handleToggle = async (id: string) => {
    // Optimistically update the UI
    mutate(
      items?.map((item) =>
        item.id === id ? { ...item, active: !item.active } : item
      ),
      false // Don't revalidate immediately
    );

    try {
      await carouselApi.toggleActive(id);
      toast.success("Status updated");
    } catch (error) {
      toast.error("Failed to update status");
      // Revert on error
      mutate();
    }
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await carouselApi.delete(deleteId);
      toast.success("Carousel item deleted");
      mutate(); // Revalidate cache
    } catch (error) {
      toast.error("Failed to delete item");
    } finally {
      setDeleteId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Carousel</h1>
          <p className="text-muted-foreground">Manage homepage carousel</p>
        </div>
        <Dialog open={isOpen} onOpenChange={setIsOpen}>
          <DialogTrigger asChild><Button><Plus className="h-4 w-4 mr-2" />Add Slide</Button></DialogTrigger>
          <DialogContent className="max-w-2xl">
            <DialogHeader><DialogTitle>Add Carousel Slide</DialogTitle></DialogHeader>
            <form onSubmit={handleSubmit} className="space-y-4">
              <p className="text-sm text-muted-foreground">
                Upload an image for the carousel. The slide will be inactive by default.
              </p>
              <ImageUpload label="Slide Image" value={image} onChange={setImage} required />
              <Button type="submit">Upload Slide</Button>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      {isLoading ? (
        <div>Loading...</div>
      ) : !items || items.length === 0 ? (
        <Card><CardContent className="flex flex-col items-center justify-center py-12"><Presentation className="h-12 w-12 text-muted-foreground mb-4" /><p>No carousel items</p></CardContent></Card>
      ) : (
        <div className="grid gap-6 md:grid-cols-2">
          {items.map((item) => (
            <Card key={item.id} className="pt-0 overflow-hidden">
              {item.imageUrl && (
                <div className="h-70 relative">
                  <Image src={item.imageUrl} alt="Carousel Slide" fill />
                </div>
              )}
              <CardHeader>
                <CardTitle>Carousel Slide</CardTitle>
                <p className="text-xs text-muted-foreground">ID: {item.id}</p>
              </CardHeader>
              <CardContent className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label>Active</Label>
                  <Switch checked={item.active} onCheckedChange={() => handleToggle(item.id)} />
                </div>
                <Button variant="outline" size="sm" className="w-full" onClick={() => setDeleteId(item.id)}>
                  <Trash2 className="h-4 w-4 mr-2" />Delete
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>This will permanently delete the carousel item.</AlertDialogDescription>
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
