"use client";

import { useState } from "react";
import { Trash2, Upload, Video } from "lucide-react";
import { toast } from "sonner";
import { reelsApi } from "@/lib/api";
import { useReels } from "@/hooks/use-api";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

export default function ReelsPage() {
  const { reels, isLoading, mutate } = useReels();
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);

  const handleUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files || []);
    if (!files.length) return;
    setUploading(true);
    try {
      await Promise.all(files.map((file) => reelsApi.upload(file)));
      toast.success("Videos uploaded successfully");
      await mutate();
      event.target.value = "";
    } catch {
      toast.error("Failed to upload videos");
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await reelsApi.delete(deleteId);
      toast.success("Video deleted successfully");
      await mutate();
    } catch {
      toast.error("Failed to delete video");
    } finally {
      setDeleteId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Reels</h1>
          <p className="text-muted-foreground">Manage videos shown in the public works section</p>
        </div>
        <label htmlFor="reels-upload">
          <Button disabled={uploading} asChild>
            <span><Upload className="mr-2 h-4 w-4" />{uploading ? "Uploading..." : "Upload Videos"}</span>
          </Button>
          <input id="reels-upload" type="file" accept="video/*" multiple onChange={handleUpload} className="hidden" />
        </label>
      </div>

      {isLoading ? <div>Loading...</div> : !reels?.length ? (
        <Card><CardContent className="flex flex-col items-center justify-center py-12"><Video className="mb-4 h-12 w-12 text-muted-foreground" /><p>No videos in reels</p></CardContent></Card>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {reels.map((reel) => (
            <Card key={reel.id} className="group relative overflow-hidden p-0">
              <video src={reel.attach.url} controls playsInline preload="metadata" className="aspect-[9/16] w-full object-cover" />
              <div className="absolute right-3 top-3 opacity-0 transition-opacity group-hover:opacity-100">
                <Button variant="destructive" size="icon" onClick={() => setDeleteId(reel.id)}>
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete this video?</AlertDialogTitle>
            <AlertDialogDescription>This will permanently remove the reel from the public website.</AlertDialogDescription>
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
