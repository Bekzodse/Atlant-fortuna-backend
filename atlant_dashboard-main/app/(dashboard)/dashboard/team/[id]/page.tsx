"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import { ArrowLeft, Save } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { MultiLangInput } from "@/components/forms/MultiLangInput";
import { ImageUpload } from "@/components/forms/ImageUpload";
import { PhoneInput } from "@/components/forms/PhoneInput";
import { teamApi, attachmentsApi } from "@/lib/api";
import { TeamMemberRequestDTO } from "@/types";
import { toast } from "sonner";
import Link from "next/link";
import { AxiosError } from "axios";
import { isValidPhoneNumber } from "react-phone-number-input";

export default function TeamFormPage() {
  const router = useRouter();
  const params = useParams();
  const isNew = params.id === "new";

  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState<TeamMemberRequestDTO>({
    nameEn: "", nameRu: "", nameUz: "",
    roleEn: "", roleRu: "", roleUz: "",
    phoneNumber: "", imageId: "",
  });
  const [image, setImage] = useState<File[]>([]);
  const [phoneError, setPhoneError] = useState("");

  useEffect(() => {
    if (!isNew) {
      fetchMember();
    }
  }, [params.id]);

  const fetchMember = async () => {
    try {
      const members = await teamApi.getAllAdmin();
      const member = members.find((m) => m.id === params.id);
      if (member) {
        setFormData({
          nameEn: member.nameEn || "",
          nameRu: member.nameRu || "",
          nameUz: member.nameUz || "",
          roleEn: member.roleEn || "",
          roleRu: member.roleRu || "",
          roleUz: member.roleUz || "",
          phoneNumber: member.phoneNumber || "",
          imageId: member.image?.id || "",
        });
      }
    } catch (error) {
      toast.error("Failed to fetch team member");
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!isValidPhoneNumber(formData.phoneNumber)) {
      setPhoneError("Enter a valid phone number including country code");
      return;
    }
    setPhoneError("");
    setIsLoading(true);

    try {
      let imageId = formData.imageId;
      if (image.length > 0) {
        const uploaded = await attachmentsApi.upload(image[0]);
        imageId = uploaded.id;
      }

      const memberData: TeamMemberRequestDTO = { ...formData, imageId };

      if (isNew) {
        await teamApi.create(memberData);
        toast.success("Team member created successfully");
      } else {
        await teamApi.update(params.id as string, memberData);
        toast.success("Team member updated successfully");
      }

      router.push("/dashboard/team");
    } catch (error) {
      const axiosError = error as AxiosError<{ message?: string }>;
      toast.error(axiosError.response?.data?.message || "Failed to save team member");
      console.error("Team member save error:", error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link href="/dashboard/team"><Button variant="ghost" size="icon"><ArrowLeft className="h-4 w-4" /></Button></Link>
        <div>
          <h1 className="text-3xl font-bold">{isNew ? "Add Team Member" : "Edit Team Member"}</h1>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="grid gap-6">
          <Card>
            <CardHeader><CardTitle>Member Information</CardTitle></CardHeader>
            <CardContent className="space-y-4">
              <MultiLangInput
                label="Name" required
                valueEn={formData.nameEn} valueRu={formData.nameRu} valueUz={formData.nameUz}
                onChangeEn={(v) => setFormData({ ...formData, nameEn: v })}
                onChangeRu={(v) => setFormData({ ...formData, nameRu: v })}
                onChangeUz={(v) => setFormData({ ...formData, nameUz: v })}
              />
              <MultiLangInput
                label="Role" required
                valueEn={formData.roleEn} valueRu={formData.roleRu} valueUz={formData.roleUz}
                onChangeEn={(v) => setFormData({ ...formData, roleEn: v })}
                onChangeRu={(v) => setFormData({ ...formData, roleRu: v })}
                onChangeUz={(v) => setFormData({ ...formData, roleUz: v })}
              />
              <div className="space-y-2">
                <Label htmlFor="phone">Phone Number <span className="text-destructive">*</span></Label>
                <PhoneInput
                  id="phone"
                  value={formData.phoneNumber}
                  onChange={(v) => { setFormData({ ...formData, phoneNumber: v }); setPhoneError(""); }}
                  required
                  error={phoneError}
                />
              </div>
              <ImageUpload label="Photo" value={image} onChange={setImage} required={isNew} />
            </CardContent>
          </Card>

          <div className="flex gap-4">
            <Button type="submit" disabled={isLoading}><Save className="h-4 w-4 mr-2" />{isLoading ? "Saving..." : "Save"}</Button>
            <Link href="/dashboard/team"><Button type="button" variant="outline">Cancel</Button></Link>
          </div>
        </div>
      </form>
    </div>
  );
}
