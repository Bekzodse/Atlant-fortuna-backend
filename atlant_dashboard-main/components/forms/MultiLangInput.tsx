"use client";

import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

interface MultiLangInputProps {
  label: string;
  valueEn: string;
  valueRu: string;
  valueUz: string;
  onChangeEn: (value: string) => void;
  onChangeRu: (value: string) => void;
  onChangeUz: (value: string) => void;
  placeholder?: string;
  required?: boolean;
  type?: string;
  error?: {
    en?: string;
    ru?: string;
    uz?: string;
  };
}

export function MultiLangInput({
  label,
  valueEn,
  valueRu,
  valueUz,
  onChangeEn,
  onChangeRu,
  onChangeUz,
  placeholder = "",
  required = false,
  type = "text",
  error,
}: MultiLangInputProps) {
  const [activeTab, setActiveTab] = useState("en");

  return (
    <div className="space-y-2">
      <Label>
        {label}
        {required && <span className="text-destructive ml-1">*</span>}
      </Label>
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="en">EN</TabsTrigger>
          <TabsTrigger value="ru">RU</TabsTrigger>
          <TabsTrigger value="uz">UZ</TabsTrigger>
        </TabsList>
        <TabsContent value="en" className="space-y-2">
          <Input
            type={type}
            value={valueEn}
            onChange={(e) => onChangeEn(e.target.value)}
            placeholder={`${placeholder} (English)`}
            required={required}
          />
          {error?.en && (
            <p className="text-sm text-destructive">{error.en}</p>
          )}
        </TabsContent>
        <TabsContent value="ru" className="space-y-2">
          <Input
            type={type}
            value={valueRu}
            onChange={(e) => onChangeRu(e.target.value)}
            placeholder={`${placeholder} (Russian)`}
            required={required}
          />
          {error?.ru && (
            <p className="text-sm text-destructive">{error.ru}</p>
          )}
        </TabsContent>
        <TabsContent value="uz" className="space-y-2">
          <Input
            type={type}
            value={valueUz}
            onChange={(e) => onChangeUz(e.target.value)}
            placeholder={`${placeholder} (Uzbek)`}
            required={required}
          />
          {error?.uz && (
            <p className="text-sm text-destructive">{error.uz}</p>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
