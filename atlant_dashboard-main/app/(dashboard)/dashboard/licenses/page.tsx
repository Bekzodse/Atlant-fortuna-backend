"use client";

import { useState } from "react";
import { Plus, Trash2, FileText } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { MultiLangInput } from "@/components/forms/MultiLangInput";
import { ImageUpload } from "@/components/forms/ImageUpload";
import { licensesApi, attachmentsApi } from "@/lib/api";
import { LicenseRequestDTO } from "@/types";
import { useLicenses } from "@/hooks/use-api";
import { toast } from "sonner";
import Image from "next/image";

export default function LicensesPage() {
  const { licenses, isLoading, mutate } = useLicenses();
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [isOpen, setIsOpen] = useState(false);
  const [formData, setFormData] = useState<LicenseRequestDTO>({ titleEn: "", titleRu: "", titleUz: "", imageId: "" });
  const [image, setImage] = useState<File[]>([]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (image.length === 0) {
        toast.error("Please upload an image");
        return;
      }
      const uploaded = await attachmentsApi.upload(image[0]);
      await licensesApi.create({ ...formData, imageId: uploaded.id });
      toast.success("License created successfully");
      setIsOpen(false);
      setFormData({ titleEn: "", titleRu: "", titleUz: "", imageId: "" });
      setImage([]);
      mutate(); // Revalidate cache
    } catch (error) {
      toast.error("Failed to create license");
    }
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      // Optimistically update UI
      mutate(
        licenses?.filter((license) => license.id !== deleteId),
        false
      );

      await licensesApi.delete(deleteId);
      toast.success("License deleted successfully");
    } catch (error) {
      toast.error("Failed to delete license");
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
          <h1 className="text-3xl font-bold">Licenses</h1>
          <p className="text-muted-foreground">Manage company licenses</p>
        </div>
        <Dialog open={isOpen} onOpenChange={setIsOpen}>
          <DialogTrigger asChild><Button><Plus className="h-4 w-4 mr-2" />Add License</Button></DialogTrigger>
          <DialogContent className="max-w-2xl">
            <DialogHeader><DialogTitle>Add New License</DialogTitle></DialogHeader>
            <form onSubmit={handleSubmit} className="space-y-4">
              <MultiLangInput label="Title" required
                valueEn={formData.titleEn} valueRu={formData.titleRu} valueUz={formData.titleUz}
                onChangeEn={(v) => setFormData({ ...formData, titleEn: v })}
                onChangeRu={(v) => setFormData({ ...formData, titleRu: v })}
                onChangeUz={(v) => setFormData({ ...formData, titleUz: v })}
              />
              <ImageUpload label="License Image" value={image} onChange={setImage} required />
              <Button type="submit">Create License</Button>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      {isLoading ? (
        <div>Loading...</div>
      ) : !licenses || licenses.length === 0 ? (
        <Card><CardContent className="flex flex-col items-center justify-center py-12"><FileText className="h-12 w-12 text-muted-foreground mb-4" /><p>No licenses found</p></CardContent></Card>
      ) : (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {licenses.map((license) => (
            <Card key={license.id}>
              <CardHeader>
                {license.image && (
                  <div className="h-48 relative rounded overflow-hidden mb-4">
                    <Image src={license.image.url} alt={license.title} fill className="object-cover" />
                  </div>
                )}
                <CardTitle>{license.title}</CardTitle>
              </CardHeader>
              <CardContent>
                <Button variant="outline" size="sm" className="w-full" onClick={() => setDeleteId(license.id)}>
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
            <AlertDialogDescription>This will permanently delete the license.</AlertDialogDescription>
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
