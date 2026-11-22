"use client";

import { useState } from "react";
import Link from "next/link";
import { Plus, Pencil, Trash2, Users, Phone } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { teamApi } from "@/lib/api";
import { useTeamMembers } from "@/hooks/use-api";
import { toast } from "sonner";
import Image from "next/image";

export default function TeamPage() {
  const { members, isLoading, mutate } = useTeamMembers();
  const [deleteId, setDeleteId] = useState<string | null>(null);

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await teamApi.delete(deleteId);
      toast.success("Team member deleted successfully");
      mutate(); // Revalidate the cache
    } catch (error) {
      toast.error("Failed to delete team member");
    } finally {
      setDeleteId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Team Members</h1>
          <p className="text-muted-foreground">Manage your team</p>
        </div>
        <Link href="/dashboard/team/new">
          <Button><Plus className="h-4 w-4 mr-2" />Add Member</Button>
        </Link>
      </div>

      {isLoading ? (
        <div>Loading...</div>
      ) : !members || members.length === 0 ? (
        <Card><CardContent className="flex flex-col items-center justify-center py-12"><Users className="h-12 w-12 text-muted-foreground mb-4" /><p>No team members found</p></CardContent></Card>
      ) : (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {members.map((member) => (
            <Card key={member.id}>
              <CardHeader>
                {member.image && (
                  <div className="h-32 w-32 relative rounded-full overflow-hidden mx-auto mb-4">
                    <Image src={member.image.url} alt={member.nameEn} fill className="object-cover" />
                  </div>
                )}
                <CardTitle className="text-center">{member.nameEn}</CardTitle>
                <p className="text-center text-sm text-muted-foreground">{member.roleEn}</p>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-center gap-2 text-sm text-muted-foreground mb-4">
                  <Phone className="h-4 w-4" />
                  <span>{member.phoneNumber}</span>
                </div>
                <div className="flex justify-center gap-2">
                  <Link href={`/dashboard/team/${member.id}`}>
                    <Button variant="outline" size="sm"><Pencil className="h-4 w-4" /></Button>
                  </Link>
                  <Button variant="outline" size="sm" onClick={() => setDeleteId(member.id)}>
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <AlertDialog open={!!deleteId} onOpenChange={() => setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>This will permanently delete the team member.</AlertDialogDescription>
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
