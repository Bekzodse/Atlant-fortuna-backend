"use client";

import { Settings as SettingsIcon } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

export default function SettingsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Settings</h1>
        <p className="text-muted-foreground">Manage your dashboard settings</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Application Settings</CardTitle>
          <CardDescription>Configure your dashboard preferences</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col items-center justify-center py-12">
          <SettingsIcon className="h-12 w-12 text-muted-foreground mb-4" />
          <p className="text-muted-foreground">Settings page coming soon</p>
        </CardContent>
      </Card>
    </div>
  );
}
